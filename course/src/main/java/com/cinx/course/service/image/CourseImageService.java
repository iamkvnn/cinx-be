package com.cinx.course.service.image;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseImage;
import com.cinx.course.repository.CourseImageRepository;
import com.cinx.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseImageService implements ICourseImageService{
    private final CourseImageRepository courseImageRepository;
    private final CloudinaryImageSaver cloudinaryImageSaver;
    private final CourseRepository courseRepository;

    @Override
    public void saveCourseImages(String courseId, List<MultipartFile> files) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        for (MultipartFile file : files) {
            String publicId = generatePublicId(course);
            cloudinaryImageSaver.saveImage(file, publicId);
            String imageUrl = cloudinaryImageSaver.generateImageUrl(publicId);
            course.getImages().add(
                    CourseImage.builder()
                            .course(course)
                            .imageUrl(imageUrl)
                            .publicId(publicId)
                            .build()
            );
        }
        courseImageRepository.saveAll(course.getImages());
    }

    @Override
    public void updateCourseImages(String imageId, MultipartFile file) {
        courseImageRepository.findById(imageId).ifPresent(image -> {
            cloudinaryImageSaver.saveImage(file, image.getPublicId());
            courseImageRepository.save(image);
        });
    }

    @Override
    public void deleteImage(String imageId) {
        courseImageRepository.findById(imageId).ifPresent(image -> {
            cloudinaryImageSaver.deleteImage(image.getPublicId());
            courseImageRepository.delete(image);
        });
    }

    private String generatePublicId(Course course) {
        return "course_images/" + course.getId() + "/" + course.getTitle().replaceAll("\\s+", "_") + "_" + (course.getImages().size() + 1);
    }
}
