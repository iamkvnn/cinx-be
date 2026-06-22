package com.cinx.user.service.policy;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.user.consts.PolicyStatus;
import com.cinx.user.consts.PolicyType;
import com.cinx.user.dto.request.CreatePolicyRequest;
import com.cinx.user.dto.request.PolicySectionRequest;
import com.cinx.user.dto.request.UpdatePolicyRequest;
import com.cinx.user.dto.response.PolicyDetailResponse;
import com.cinx.user.dto.response.PolicySummaryResponse;
import com.cinx.user.mapper.PolicyMapper;
import com.cinx.user.messaging.UserEventProducer;
import com.cinx.user.model.PolicyDocument;
import com.cinx.user.model.PolicySection;
import com.cinx.user.repository.PolicyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PolicyService implements IPolicyService {
    private static final String POLICY_SOURCE_TYPE = "POLICY";
    private static final String RESERVED_VERSIONS_SLUG = "versions";
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");

    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyMapper policyMapper;
    private final UserEventProducer userEventProducer;

    @Override
    @Transactional(readOnly = true)
    public List<PolicySummaryResponse> findPublishedPolicies() {
        return policyDocumentRepository.findAllByStatusOrderByDisplayOrderAscPublishedAtDesc(PolicyStatus.PUBLISHED)
                .stream()
                .map(policyMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailResponse findPublishedPolicyBySlug(String slug) {
        return policyMapper.toDetailResponse(findPublishedOrThrow(normalizeSlug(slug)));
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailResponse findById(String id) {
        return policyMapper.toDetailResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PolicySummaryResponse> findAllVersions(
            int page,
            int size,
            PolicyStatus status,
            PolicyType policyType,
            String query,
            String sort
    ) {
        return policyDocumentRepository.findAllForManagement(
                status,
                policyType,
                normalizeQuery(query),
                PageRequest.of(page - 1, size, SortConverter.toSort(sort))
        ).map(policyMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public PolicyDetailResponse createDraft(CreatePolicyRequest request) {
        String slug = normalizeSlug(request.slug());
        ensureAllowedSlug(slug);

        PolicyDocument document = new PolicyDocument();
        document.setPolicyType(request.policyType());
        document.setSlug(slug);
        document.setTitle(request.title().trim());
        document.setSummary(trimToNull(request.summary()));
        document.setStatus(PolicyStatus.DRAFT);
        document.setVersionNumber(policyDocumentRepository.findMaxVersionNumberBySlug(slug) + 1);
        document.setEffectiveAt(request.effectiveAt());
        document.setDisplayOrder(request.displayOrder());
        replaceSections(document, request.sections());

        return policyMapper.toDetailResponse(policyDocumentRepository.save(document));
    }

    @Override
    @Transactional
    public PolicyDetailResponse updateDraft(String id, UpdatePolicyRequest request) {
        PolicyDocument document = findByIdOrThrow(id);
        if (document.getStatus() != PolicyStatus.DRAFT) {
            throw new BadRequestException("Only draft policies can be updated");
        }

        if (request.policyType() != null) {
            document.setPolicyType(request.policyType());
        }
        document.setTitle(request.title().trim());
        document.setSummary(trimToNull(request.summary()));
        document.setEffectiveAt(request.effectiveAt());
        document.setDisplayOrder(request.displayOrder());
        replaceSections(document, request.sections());

        return policyMapper.toDetailResponse(policyDocumentRepository.save(document));
    }

    @Override
    @Transactional
    public PolicyDetailResponse publish(String id) {
        PolicyDocument document = findByIdOrThrow(id);
        if (document.getStatus() != PolicyStatus.DRAFT) {
            throw new BadRequestException("Only draft policies can be published");
        }

        List<PolicyDocument> oldPublishedDocuments = policyDocumentRepository.findAllBySlugAndStatus(
                document.getSlug(),
                PolicyStatus.PUBLISHED
        );
        oldPublishedDocuments.stream()
                .filter(oldDocument -> !oldDocument.getId().equals(document.getId()))
                .forEach(oldDocument -> oldDocument.setStatus(PolicyStatus.ARCHIVED));

        LocalDateTime now = LocalDateTime.now();
        document.setStatus(PolicyStatus.PUBLISHED);
        document.setPublishedAt(now);
        policyDocumentRepository.saveAll(oldPublishedDocuments);
        PolicyDocument saved = policyDocumentRepository.save(document);
        publishAfterCommit(() -> publishPolicyPublished(saved));
        return policyMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    public PolicyDetailResponse archive(String id) {
        PolicyDocument document = findByIdOrThrow(id);
        if (document.getStatus() == PolicyStatus.ARCHIVED) {
            return policyMapper.toDetailResponse(document);
        }

        boolean wasPublished = document.getStatus() == PolicyStatus.PUBLISHED;
        document.setStatus(PolicyStatus.ARCHIVED);
        PolicyDocument saved = policyDocumentRepository.save(document);
        if (wasPublished) {
            publishAfterCommit(() -> publishPolicyArchived(saved));
        }
        return policyMapper.toDetailResponse(saved);
    }

    private PolicyDocument findPublishedOrThrow(String slug) {
        return policyDocumentRepository.findFirstBySlugAndStatusOrderByVersionNumberDesc(slug, PolicyStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published policy not found"));
    }

    private PolicyDocument findByIdOrThrow(String id) {
        return policyDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
    }

    private void replaceSections(PolicyDocument document, List<PolicySectionRequest> requests) {
        document.getSections().clear();
        Set<String> anchors = new HashSet<>();
        requests.stream()
                .sorted(Comparator.comparing(PolicySectionRequest::orderIndex))
                .forEach(request -> {
                    PolicySection section = new PolicySection();
                    section.setDocument(document);
                    section.setHeading(request.heading().trim());
                    section.setAnchor(uniqueAnchor(request, anchors));
                    section.setBodyMarkdown(request.bodyMarkdown().trim());
                    section.setOrderIndex(request.orderIndex());
                    document.getSections().add(section);
                });
    }

    private String uniqueAnchor(PolicySectionRequest request, Set<String> existingAnchors) {
        String base = trimToNull(request.anchor());
        if (base == null) {
            base = slugify(request.heading());
        } else {
            base = slugify(base);
        }
        if (base == null || base.isBlank()) {
            base = "section";
        }

        String candidate = base;
        int counter = 2;
        while (!existingAnchors.add(candidate)) {
            candidate = base + "-" + counter;
            counter++;
        }
        return candidate;
    }

    private String normalizeSlug(String value) {
        String slug = slugify(value);
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException("Policy slug is required");
        }
        return slug;
    }

    private String slugify(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replace("đ", "d")
                .replace("Đ", "D");
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String slug = NON_SLUG_CHARS.matcher(withoutDiacritics.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.isBlank() ? null : slug;
    }

    private void ensureAllowedSlug(String slug) {
        if (RESERVED_VERSIONS_SLUG.equals(slug)) {
            throw new BadRequestException("Policy slug is reserved");
        }
    }

    private String normalizeQuery(String query) {
        return trimToNull(query);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void publishAfterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }

    private void publishPolicyPublished(PolicyDocument document) {
        userEventProducer.publishPolicyPublished(
                policyDocumentId(document),
                document.getTitle(),
                policySourceUrl(document),
                flattenForRag(document),
                document.getVersionNumber(),
                document.getPublishedAt()
        );
    }

    private void publishPolicyArchived(PolicyDocument document) {
        userEventProducer.publishPolicyArchived(
                policyDocumentId(document),
                policySourceUrl(document),
                document.getPublishedAt()
        );
    }

    private String policyDocumentId(PolicyDocument document) {
        return "policy-" + document.getSlug();
    }

    private String policySourceUrl(PolicyDocument document) {
        return "/policies/" + document.getSlug();
    }

    private String flattenForRag(PolicyDocument document) {
        StringBuilder builder = new StringBuilder();
        builder.append(document.getTitle()).append("\n\n");
        if (document.getSummary() != null) {
            builder.append(document.getSummary()).append("\n\n");
        }
        document.getSections().stream()
                .sorted(Comparator.comparing(PolicySection::getOrderIndex))
                .forEach(section -> builder
                        .append(section.getHeading())
                        .append("\n")
                        .append(section.getBodyMarkdown())
                        .append("\n\n"));
        return builder.toString().trim();
    }
}
