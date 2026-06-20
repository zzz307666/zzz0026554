package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhjt.entity.SportType;
import com.hhjt.mapper.SportTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 运动类型管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/sport")
public class AdminSportController {

    @Autowired
    private SportTypeMapper sportTypeMapper;

    /**
     * 运动类型管理页面
     */
    @GetMapping("/type/manage")
    public String typeManage(Model model) {
        List<SportType> sportTypes = sportTypeMapper.selectList(
            new LambdaQueryWrapper<SportType>()
                .orderByAsc(SportType::getSortOrder)
        );
        model.addAttribute("sportTypes", sportTypes);
        return "admin/sport_type_manage";
    }

    /**
     * 添加运动类型
     */
    @PostMapping("/type/add")
    @ResponseBody
    public Map<String, Object> addType(@RequestBody SportType sportType) {
        Map<String, Object> result = new HashMap<>();
        try {
            sportType.setStatus(1);
            int rows = sportTypeMapper.insert(sportType);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            log.error("添加运动类型异常", e);
        }
        return result;
    }

    /**
     * 更新运动类型
     */
    @PostMapping("/type/update")
    @ResponseBody
    public Map<String, Object> updateType(@RequestBody SportType sportType) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = sportTypeMapper.updateById(sportType);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("更新运动类型异常", e);
        }
        return result;
    }

    /**
     * 删除运动类型
     */
    @PostMapping("/type/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteType(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = sportTypeMapper.deleteById(id);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
            log.error("删除运动类型异常", e);
        }
        return result;
    }

    /**
     * 启用/禁用运动类型
     */
    @PostMapping("/type/toggle-status/{id}")
    @ResponseBody
    public Map<String, Object> toggleStatus(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            SportType sportType = sportTypeMapper.selectById(id);
            if (sportType != null) {
                sportType.setStatus(sportType.getStatus() == 1 ? 0 : 1);
                int rows = sportTypeMapper.updateById(sportType);
                result.put("success", rows > 0);
                result.put("message", rows > 0 ? "操作成功" : "操作失败");
            } else {
                result.put("success", false);
                result.put("message", "运动类型不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
            log.error("切换状态异常", e);
        }
        return result;
    }
}
