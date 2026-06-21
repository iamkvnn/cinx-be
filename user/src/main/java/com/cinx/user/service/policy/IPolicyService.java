package com.cinx.user.service.policy;

import com.cinx.user.consts.PolicyStatus;
import com.cinx.user.consts.PolicyType;
import com.cinx.user.dto.request.CreatePolicyRequest;
import com.cinx.user.dto.request.UpdatePolicyRequest;
import com.cinx.user.dto.response.PolicyDetailResponse;
import com.cinx.user.dto.response.PolicySummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IPolicyService {
    List<PolicySummaryResponse> findPublishedPolicies();

    PolicyDetailResponse findPublishedPolicyBySlug(String slug);

    Page<PolicySummaryResponse> findAllVersions(
            int page,
            int size,
            PolicyStatus status,
            PolicyType policyType,
            String query,
            String sort
    );

    PolicyDetailResponse createDraft(CreatePolicyRequest request);

    PolicyDetailResponse updateDraft(String id, UpdatePolicyRequest request);

    PolicyDetailResponse publish(String id);

    PolicyDetailResponse archive(String id);
}
