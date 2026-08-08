package com.ai.api.mapper;

import com.ai.api.dto.classroomstudent.ClassroomStudentDto;
import com.ai.api.form.classroomstudent.RegisterClassroomStudentForm;
import com.ai.api.model.ClassroomStudent;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ClassroomMapper.class, StudentMapper.class, VoucherMapper.class})
public interface ClassroomStudentMapper {
    @BeanMapping(ignoreByDefault = true)
    ClassroomStudent fromFormToEntity(RegisterClassroomStudentForm form);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "classroom", target = "classroom", qualifiedByName = "fromEntityToClassroomDto")
    @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
    @Mapping(source = "dateRegistration", target = "dateRegistration")
    @Mapping(source = "dateDone", target = "dateDone")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "voucher", target = "voucher", qualifiedByName = "fromEntityToVoucherDto")
    @Mapping(source = "discountAmount", target = "discountAmount")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @Named("fromEntityToClassroomStudentDto")
    ClassroomStudentDto fromEntityToClassroomStudentDto(ClassroomStudent classroomStudent);

    @IterableMapping(elementTargetType = ClassroomStudentDto.class, qualifiedByName = "fromEntityToClassroomStudentDto")
    @Named("fromEntityToClassroomStudentDtoList")
    List<ClassroomStudentDto> fromEntityToClassroomStudentDtoList(List<ClassroomStudent> classroomStudents);
}
