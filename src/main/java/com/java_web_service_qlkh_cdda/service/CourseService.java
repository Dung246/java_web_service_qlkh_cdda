package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.CourseRequest;
import com.java_web_service_qlkh_cdda.dto.response.CourseResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;

public interface CourseService {
    CourseResponse createCourse(CourseRequest request);
    CourseResponse getCourseById(Long id);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
    PageResponse<CourseResponse> getAllCourses(int page, int size, String keyword, Boolean isActive);
    PageResponse<CourseResponse> getCoursesByLecturer(Long lecturerId, int page, int size);
}