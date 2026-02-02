package com.cinx.common.mapper;

public interface BaseMapper<M, D> {
    M toModel(D dto);
    D toDto(M model);
}
