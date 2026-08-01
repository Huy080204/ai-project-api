package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.rating.RatingDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.rating.CreateRatingForm;
import com.ai.api.form.rating.UpdateRatingForm;
import com.ai.api.mapper.RatingMapper;
import com.ai.api.model.Course;
import com.ai.api.model.Rating;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.RatingCriteria;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.RatingRepository;
import com.ai.api.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/rating")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class RatingController extends ABasicController {
    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RatingMapper ratingMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RAT_C')")
    @Transactional
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateRatingForm form, BindingResult bindingResult) {
        Course course = courseRepository.findById(form.getCourseId())
                .orElseThrow(() -> new NotFoundException("[Rating] Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        Student student = studentRepository.findById(form.getStudentId())
                .orElseThrow(() -> new NotFoundException("[Rating] Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));
        Rating rating = ratingMapper.fromFormToEntity(form);
        rating.setCourse(course);
        rating.setStudent(student);
        ratingRepository.save(rating);
        return makeSuccessResponse("Create rating success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RAT_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateRatingForm form, BindingResult bindingResult) {
        Rating rating = ratingRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Rating] Rating not found", ErrorCode.RATING_ERROR_NOT_FOUND));
        ratingMapper.updateEntityFromForm(form, rating);
        ratingRepository.save(rating);
        return makeSuccessResponse("Update rating success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RAT_V')")
    public ApiMessageDto<RatingDto> get(@PathVariable Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Rating] Rating not found", ErrorCode.RATING_ERROR_NOT_FOUND));
        return makeSuccessResponse(ratingMapper.fromEntityToRatingDto(rating), "Get rating success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RAT_L')")
    public ApiMessageDto<ResponseListDto<List<RatingDto>>> list(RatingCriteria criteria, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(ratings, ratingMapper::fromEntityToRatingDtoList), "List rating success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RAT_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Rating] Rating not found", ErrorCode.RATING_ERROR_NOT_FOUND));
        ratingRepository.deleteById(id);
        return makeSuccessResponse("Delete rating success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<RatingDto>>> publicList(RatingCriteria criteria, Pageable pageable) {
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Rating> ratings = ratingRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(ratings, ratingMapper::fromEntityToRatingDtoPublicList), "List rating success");
    }
}
