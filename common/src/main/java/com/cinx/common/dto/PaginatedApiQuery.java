package com.cinx.common.dto;

import com.cinx.common.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PaginatedApiQuery {
    @Min(1)
    private int page = 1;
    @Min(1) @Max(1000)
    private int size = 10;
    private String query;
    private String sort;

    public static PaginatedApiQuery of(int page, int size, String query, String sort) {
        PaginatedApiQuery apiQuery = new PaginatedApiQuery();
        apiQuery.setPage(page);
        apiQuery.setSize(size);
        apiQuery.setQuery(query);
        apiQuery.setSort(sort);
        return apiQuery;
    }

    public Pageable toPageable() {
        Sort s = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> sortMap;
            try {
                sortMap = mapper.readValue(sort, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid sort parameter format");
            }
            s = sortMap.entrySet().stream()
                    .map(e -> new Sort.Order(Sort.Direction.fromString(e.getValue()), e.getKey()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));
        }

        return PageRequest.of(page - 1, size, s);
    }
}
