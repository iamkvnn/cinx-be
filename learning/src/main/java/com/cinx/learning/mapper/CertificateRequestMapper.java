package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.CertificateRequestResponse;
import com.cinx.learning.model.CertificateRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CertificateRequestMapper extends BaseMapper<CertificateRequest, CertificateRequestResponse> {
}