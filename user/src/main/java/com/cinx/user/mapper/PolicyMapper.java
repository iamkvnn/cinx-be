package com.cinx.user.mapper;

import com.cinx.user.dto.response.PolicyDetailResponse;
import com.cinx.user.dto.response.PolicySectionResponse;
import com.cinx.user.dto.response.PolicySummaryResponse;
import com.cinx.user.model.PolicyDocument;
import com.cinx.user.model.PolicySection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {
    PolicySummaryResponse toSummaryResponse(PolicyDocument document);

    PolicyDetailResponse toDetailResponse(PolicyDocument document);

    PolicySectionResponse toSectionResponse(PolicySection section);
}
