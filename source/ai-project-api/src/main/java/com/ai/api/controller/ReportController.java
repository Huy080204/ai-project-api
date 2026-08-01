package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.report.CourseReportDto;
import com.ai.api.dto.report.StudentReportDto;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/v1/report")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ReportController extends ABasicController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping(value = "/top-course", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RPT_L')")
    public ApiMessageDto<List<CourseReportDto>> topCourse() {
        List<CourseReportDto> reportList = courseRepository.findTopCourseReport(AIConstant.STATUS_ACTIVE, PageRequest.of(0, 20));
        return makeSuccessResponse(reportList, "Get top course report success");
    }

    @GetMapping(value = "/top-student", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('RPT_L')")
    public ApiMessageDto<List<StudentReportDto>> topStudent() {
        List<StudentReportDto> reportList = studentRepository.findTopStudentReport(AIConstant.STATUS_ACTIVE, PageRequest.of(0, 20));
        return makeSuccessResponse(reportList, "Get top student report success");
    }

    @GetMapping("/export-excel-top-course")
    @PreAuthorize("hasRole('RPT_L')")
    public ResponseEntity<byte[]> exportExcelTopCourse() throws IOException {
        List<CourseReportDto> reportList = courseRepository.findTopCourseReport(AIConstant.STATUS_ACTIVE, PageRequest.of(0, 20));

        try (InputStream inputStream = new ClassPathResource("templates/export_report_top_course_template.xlsx").getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            int rowIdx = 2;
            for (int i = 0; i < reportList.size(); i++) {
                CourseReportDto dto = reportList.get(i);
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                }
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(dto.getName());
                row.createCell(2).setCellValue(dto.getTotalStudents());
                rowIdx++;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"top-course-report.xlsx\"")
                    .body(outputStream.toByteArray());
        }
    }

    @GetMapping("/export-excel-top-student")
    @PreAuthorize("hasRole('RPT_L')")
    public ResponseEntity<byte[]> exportExcelTopStudent() throws IOException {
        List<StudentReportDto> reportList = studentRepository.findTopStudentReport(AIConstant.STATUS_ACTIVE, PageRequest.of(0, 20));

        try (InputStream inputStream = new ClassPathResource("templates/export_report_top_student_template.xlsx").getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            int rowIdx = 2;
            for (int i = 0; i < reportList.size(); i++) {
                StudentReportDto dto = reportList.get(i);
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                }
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(dto.getFullName());
                row.createCell(2).setCellValue(dto.getEmail());
                row.createCell(3).setCellValue(dto.getPhone());
                row.createCell(4).setCellValue(dto.getTotalEnrolledClasses());
                rowIdx++;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"top-student-report.xlsx\"")
                    .body(outputStream.toByteArray());
        }
    }
}
