package com.cinx.course.dto.request;

public record CreateInstructorRequest (
        String name,
        String email,
        String bio
){
}
