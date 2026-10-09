package com.ai.api.mapper;

import com.ai.api.dto.student.StudentDto;
import com.ai.api.form.student.CreateStudentForm;
import com.ai.api.form.student.UpdateStudentForm;
import com.ai.api.model.Account;
import com.ai.api.model.Student;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AccountMapper.class})
public interface StudentMapper {
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @BeanMapping(ignoreByDefault = true)
    Account fromFormToAccount(CreateStudentForm form);

    @Mapping(source = "address", target = "address")
    @BeanMapping(ignoreByDefault = true)
    Student fromFormToEntity(CreateStudentForm form);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "account", target = "account", qualifiedByName = "fromAccountToDtoShort")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToStudentDto")
    StudentDto fromEntityToStudentDto(Student student);

    @IterableMapping(elementTargetType = StudentDto.class, qualifiedByName = "fromEntityToStudentDto")
    @Named("fromEntityToStudentDtoList")
    List<StudentDto> fromEntityToStudentDtoList(List<Student> students);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "account", target = "account", qualifiedByName = "fromAccountToAutoCompleteDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToStudentAutoCompleteDto")
    StudentDto fromEntityToStudentAutoCompleteDto(Student student);

    @IterableMapping(elementTargetType = StudentDto.class, qualifiedByName = "fromEntityToStudentAutoCompleteDto")
    @Named("fromEntityToStudentAutoCompleteDtoList")
    List<StudentDto> fromEntityToStudentAutoCompleteDtoList(List<Student> students);

    @Mapping(source = "address", target = "address")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateStudentForm form, @MappingTarget Student student);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToStudentIdDto")
    StudentDto fromEntityToStudentIdDto(Student student);
}
