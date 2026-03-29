package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateInstructorRequest;
import com.cinx.course.dto.request.UpdateInstructorRequest;
import com.cinx.course.dto.response.InstructorResponse;
import com.cinx.course.model.Instructor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstructorMapper extends
        BaseMapper<Instructor, InstructorResponse>,
        CreateMapper<Instructor, CreateInstructorRequest>,
        UpdateMapper<Instructor, UpdateInstructorRequest> {
}
