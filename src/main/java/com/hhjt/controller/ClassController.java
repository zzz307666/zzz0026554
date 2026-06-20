package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SysClass;
import com.hhjt.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/class")
public class ClassController {

    @Autowired
    private ClassService classService;

    // 班级管理列表
    @GetMapping("/manage")
    public String classManage(Model model,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                              @RequestParam(value = "className", required = false) String className,
                              @RequestParam(value = "grade", required = false) String grade) {
        IPage<SysClass> classPage = classService.getClassPage(page, size, className, grade);
        model.addAttribute("page", classPage);
        model.addAttribute("className", className);
        model.addAttribute("grade", grade);
        return "admin/class_manage";
    }

    // 新增班级页面
    @GetMapping("/add")
    public String addClassPage() {
        return "admin/class_add";
    }

    // 新增班级提交
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addClass(@RequestBody SysClass sysClass) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = classService.addClass(sysClass);
            result.put("success", success);
            result.put("message", success ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }
        return result;
    }

    // 编辑班级页面
    @GetMapping("/edit/{id}")
    public String editClassPage(@PathVariable("id") Long id, Model model) {
        SysClass sysClass = classService.getClassById(id);
        model.addAttribute("sysClass", sysClass);
        return "admin/class_edit";
    }

    // 编辑班级提交
    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> editClass(@RequestBody SysClass sysClass) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = classService.updateClass(sysClass);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    // 删除班级
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteClass(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = classService.deleteClass(id);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // 获取所有班级（下拉框接口）
    @GetMapping("/list")
    @ResponseBody
    public List<SysClass> getAllClass() {
        return classService.getAllClass();
    }

    // 根据教师ID获取任职班级
    @GetMapping("/list/teacher/{teacherId}")
    @ResponseBody
    public List<SysClass> getClassByTeacherId(@PathVariable("teacherId") Long teacherId) {
        return classService.getClassByTeacherId(teacherId);
    }
}