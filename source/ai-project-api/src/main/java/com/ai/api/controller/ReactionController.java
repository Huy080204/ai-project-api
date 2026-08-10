package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.reaction.ReactionDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.reaction.CreateReactionForm;
import com.ai.api.form.reaction.UpdateReactionForm;
import com.ai.api.mapper.ReactionMapper;
import com.ai.api.model.Course;
import com.ai.api.model.Reaction;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.ReactionCriteria;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.ReactionRepository;
import com.ai.api.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/reaction")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ReactionController extends ABasicController {
    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private ReactionMapper reactionMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REA_C')")
    @Transactional
    public ApiMessageDto<ReactionDto> create(@Valid @RequestBody CreateReactionForm createReactionForm) {
        Course course = courseRepository.findById(createReactionForm.getCourseId())
                .orElseThrow(() -> new NotFoundException("Not found course!", ErrorCode.REACTION_ERROR_COURSE_NOT_FOUND));
        Student student = studentRepository.findById(createReactionForm.getStudentId())
                .orElseThrow(() -> new NotFoundException("Not found student!", ErrorCode.REACTION_ERROR_STUDENT_NOT_FOUND));

        Reaction existing = reactionRepository.findByCourseIdAndStudentIdAndStatus(
                createReactionForm.getCourseId(), createReactionForm.getStudentId(), AIConstant.STATUS_ACTIVE);
        if (existing != null) {
            throw new BadRequestException("Reaction already exists", ErrorCode.REACTION_ERROR_DUPLICATE);
        }

        Reaction reaction = reactionMapper.fromFormToEntity(createReactionForm);
        reaction.setCourse(course);
        reaction.setStudent(student);
        reaction = reactionRepository.save(reaction);
        return makeSuccessResponse(reactionMapper.fromEntityToReactionDto(reaction), "Create reaction success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REA_U')")
    @Transactional
    public ApiMessageDto<ReactionDto> update(@Valid @RequestBody UpdateReactionForm updateReactionForm) {
        Reaction reaction = reactionRepository.findById(updateReactionForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found reaction!", ErrorCode.REACTION_ERROR_NOT_FOUND));
        reactionMapper.updateEntityFromForm(updateReactionForm, reaction);
        reaction = reactionRepository.save(reaction);
        return makeSuccessResponse(reactionMapper.fromEntityToReactionDto(reaction), "Update reaction success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REA_V')")
    public ApiMessageDto<ReactionDto> get(@PathVariable Long id) {
        Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found reaction!", ErrorCode.REACTION_ERROR_NOT_FOUND));
        return makeSuccessResponse(reactionMapper.fromEntityToReactionDto(reaction), "Get reaction success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REA_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        reactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found reaction!", ErrorCode.REACTION_ERROR_NOT_FOUND));
        reactionRepository.deleteById(id);
        return makeSuccessResponse("Delete reaction success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REA_L')")
    public ApiMessageDto<ResponseListDto<List<ReactionDto>>> list(ReactionCriteria reactionCriteria, Pageable pageable) {
        Page<Reaction> page = reactionRepository.findAll(reactionCriteria.getCriteria(), pageable);
        ResponseListDto<List<ReactionDto>> responseListDto =
                makeResponseListDto(page, reactionMapper::fromEntityToReactionDtoList);
        return makeSuccessResponse(responseListDto, "Get list success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ReactionDto>>> publicList(ReactionCriteria reactionCriteria, Pageable pageable) {
        reactionCriteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Reaction> page = reactionRepository.findAll(reactionCriteria.getCriteria(), pageable);
        ResponseListDto<List<ReactionDto>> responseListDto =
                makeResponseListDto(page, reactionMapper::fromEntityToReactionDtoPublicList);
        return makeSuccessResponse(responseListDto, "Get public list success");
    }
}
