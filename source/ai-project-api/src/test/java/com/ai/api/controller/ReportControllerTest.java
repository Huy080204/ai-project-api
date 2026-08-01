package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.report.CourseReportDto;
import com.ai.api.dto.report.StudentReportDto;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.StudentRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ReportController}.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ReportController reportController;

    // -------------------------------------------------------------- topCourse

    @Test
    void shouldReturnTopCourseReport() {
        // Arrange
        CourseReportDto dto = new CourseReportDto();
        dto.setId(1L);
        dto.setName("Java Basics");
        dto.setAvatar("avatar.png");
        dto.setTotalStudents(10L);
        List<CourseReportDto> reportList = Collections.singletonList(dto);

        when(courseRepository.findTopCourseReport(any(Integer.class), any(Pageable.class))).thenReturn(reportList);

        // Act
        ApiMessageDto<List<CourseReportDto>> result = reportController.topCourse();

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().get(0).getName()).isEqualTo("Java Basics");
        assertThat(result.getData().get(0).getAvatar()).isEqualTo("avatar.png");
        assertThat(result.getData().get(0).getTotalStudents()).isEqualTo(10L);
    }

    // ------------------------------------------------------------- topStudent

    @Test
    void shouldReturnTopStudentReport() {
        // Arrange
        StudentReportDto dto = new StudentReportDto();
        dto.setId(2L);
        dto.setFullName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("0123456789");
        dto.setAvatarPath("avatar-path.png");
        dto.setTotalEnrolledClasses(5L);
        List<StudentReportDto> reportList = Collections.singletonList(dto);

        when(studentRepository.findTopStudentReport(any(Integer.class), any(Pageable.class))).thenReturn(reportList);

        // Act
        ApiMessageDto<List<StudentReportDto>> result = reportController.topStudent();

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getId()).isEqualTo(2L);
        assertThat(result.getData().get(0).getFullName()).isEqualTo("John Doe");
        assertThat(result.getData().get(0).getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getData().get(0).getPhone()).isEqualTo("0123456789");
        assertThat(result.getData().get(0).getAvatarPath()).isEqualTo("avatar-path.png");
        assertThat(result.getData().get(0).getTotalEnrolledClasses()).isEqualTo(5L);
    }

    // ------------------------------------------------------ exportExcelTopCourse

    @Test
    void shouldExportTopCourseExcelWithAttachmentHeadersAndCorrectCells() throws Exception {
        // Arrange
        CourseReportDto dto1 = new CourseReportDto();
        dto1.setId(1L);
        dto1.setName("Java Basics");
        dto1.setAvatar("avatar1.png");
        dto1.setTotalStudents(10L);

        CourseReportDto dto2 = new CourseReportDto();
        dto2.setId(2L);
        dto2.setName("Spring Boot Advanced");
        dto2.setAvatar("avatar2.png");
        dto2.setTotalStudents(25L);

        List<CourseReportDto> reportList = Arrays.asList(dto1, dto2);
        when(courseRepository.findTopCourseReport(any(Integer.class), any(Pageable.class))).thenReturn(reportList);

        // Act
        ResponseEntity<byte[]> response = reportController.exportExcelTopCourse();

        // Assert - headers
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/octet-stream");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment");

        // Assert - cells
        DataFormatter dataFormatter = new DataFormatter();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row row3 = sheet.getRow(2);
            assertThat(dataFormatter.formatCellValue(row3.getCell(0))).isEqualTo("1");
            assertThat(dataFormatter.formatCellValue(row3.getCell(1))).isEqualTo("Java Basics");
            assertThat(dataFormatter.formatCellValue(row3.getCell(2))).isEqualTo("10");

            Row row4 = sheet.getRow(3);
            assertThat(dataFormatter.formatCellValue(row4.getCell(0))).isEqualTo("2");
            assertThat(dataFormatter.formatCellValue(row4.getCell(1))).isEqualTo("Spring Boot Advanced");
            assertThat(dataFormatter.formatCellValue(row4.getCell(2))).isEqualTo("25");
        }
    }

    // ----------------------------------------------------- exportExcelTopStudent

    @Test
    void shouldExportTopStudentExcelWithAttachmentHeadersAndCorrectCells() throws Exception {
        // Arrange
        StudentReportDto dto1 = new StudentReportDto();
        dto1.setId(1L);
        dto1.setFullName("John Doe");
        dto1.setEmail("john.doe@example.com");
        dto1.setPhone("0123456789");
        dto1.setAvatarPath("avatar-path1.png");
        dto1.setTotalEnrolledClasses(5L);

        StudentReportDto dto2 = new StudentReportDto();
        dto2.setId(2L);
        dto2.setFullName("Jane Smith");
        dto2.setEmail("jane.smith@example.com");
        dto2.setPhone("0987654321");
        dto2.setAvatarPath("avatar-path2.png");
        dto2.setTotalEnrolledClasses(8L);

        List<StudentReportDto> reportList = Arrays.asList(dto1, dto2);
        when(studentRepository.findTopStudentReport(any(Integer.class), any(Pageable.class))).thenReturn(reportList);

        // Act
        ResponseEntity<byte[]> response = reportController.exportExcelTopStudent();

        // Assert - headers
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/octet-stream");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment");

        // Assert - cells
        DataFormatter dataFormatter = new DataFormatter();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row row3 = sheet.getRow(2);
            assertThat(dataFormatter.formatCellValue(row3.getCell(0))).isEqualTo("1");
            assertThat(dataFormatter.formatCellValue(row3.getCell(1))).isEqualTo("John Doe");
            assertThat(dataFormatter.formatCellValue(row3.getCell(2))).isEqualTo("john.doe@example.com");
            assertThat(dataFormatter.formatCellValue(row3.getCell(3))).isEqualTo("0123456789");
            assertThat(dataFormatter.formatCellValue(row3.getCell(4))).isEqualTo("5");

            Row row4 = sheet.getRow(3);
            assertThat(dataFormatter.formatCellValue(row4.getCell(0))).isEqualTo("2");
            assertThat(dataFormatter.formatCellValue(row4.getCell(1))).isEqualTo("Jane Smith");
            assertThat(dataFormatter.formatCellValue(row4.getCell(2))).isEqualTo("jane.smith@example.com");
            assertThat(dataFormatter.formatCellValue(row4.getCell(3))).isEqualTo("0987654321");
            assertThat(dataFormatter.formatCellValue(row4.getCell(4))).isEqualTo("8");
        }
    }
}
