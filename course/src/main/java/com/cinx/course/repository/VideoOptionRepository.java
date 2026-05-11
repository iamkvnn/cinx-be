package com.cinx.course.repository;

import com.cinx.course.model.VideoOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoOptionRepository extends JpaRepository<VideoOption, String> {
}