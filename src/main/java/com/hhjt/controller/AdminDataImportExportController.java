package com.hhjt.controller;

import com.hhjt.service.DataImportExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 管理员 - 数据导入导出控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/data")
public class AdminDataImportExportController {

    @Autowired
    private DataImportExportService dataImportExportService;

    /**
     * 数据导入导出页面（兼容旧路径 /admin/data/data-import-export）
     */
    @GetMapping("/data-import-export")
    public String dataImportExportPageOld() {
        return "admin/data_import_export";
    }

    /**
     * 数据导入导出页面（新路径 /admin/data/import-export）
     */
    @GetMapping("/import-export")
    public String importExportPage() {
        return "admin/data_import_export";
    }

    /**
     * 导入学生信息
     */
    @PostMapping("/import/students")
    @ResponseBody
    public Map<String, Object> importStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请选择文件");
            return result;
        }

        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请上传Excel文件（.xlsx或.xls）");
            return result;
        }

        return dataImportExportService.importStudents(file);
    }

    /**
     * 导入教师信息
     */
    @PostMapping("/import/teachers")
    @ResponseBody
    public Map<String, Object> importTeachers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请选择文件");
            return result;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请上传Excel文件（.xlsx或.xls）");
            return result;
        }

        return dataImportExportService.importTeachers(file);
    }

    /**
     * 下载学生导入模板
     */
    @GetMapping("/template/students")
    public void downloadStudentTemplate(HttpServletResponse response) {
        dataImportExportService.downloadStudentTemplate(response);
    }

    /**
     * 下载教师导入模板
     */
    @GetMapping("/template/teachers")
    public void downloadTeacherTemplate(HttpServletResponse response) {
        dataImportExportService.downloadTeacherTemplate(response);
    }

    /**
     * 导出运动记录
     */
    @GetMapping("/export/sport-records")
    public void exportSportRecords(@RequestParam(required = false) Long classId,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    HttpServletResponse response) {
        dataImportExportService.exportSportRecords(response, classId, startDate, endDate);
    }

    /**
     * 导出积分明细
     */
    @GetMapping("/export/points")
    public void exportPointsDetails(@RequestParam(required = false) Long studentId,
                                     HttpServletResponse response) {
        dataImportExportService.exportPointsDetails(response, studentId);
    }

    /**
     * 导出评价结果
     */
    @GetMapping("/export/evaluations")
    public void exportEvaluationResults(@RequestParam(required = false) Long classId,
                                         @RequestParam(required = false) String semester,
                                         HttpServletResponse response) {
        dataImportExportService.exportEvaluationResults(response, classId, semester);
    }
}