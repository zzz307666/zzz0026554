package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhjt.entity.PointsRule;
import com.hhjt.mapper.PointsRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 积分规则配置控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/points")
public class AdminPointsRuleController {

    @Autowired
    private PointsRuleMapper pointsRuleMapper;

    /**
     * 积分规则配置页面
     */
    @GetMapping("/rule")
    public String pointsRule(Model model) {
        List<PointsRule> rules = pointsRuleMapper.selectList(
            new LambdaQueryWrapper<PointsRule>()
                .orderByAsc(PointsRule::getId)
        );
        model.addAttribute("rules", rules);
        return "admin/points_rule";
    }

    /**
     * 添加积分规则
     */
    @PostMapping("/rule/add")
    @ResponseBody
    public Map<String, Object> addRule(@RequestBody PointsRule rule) {
        Map<String, Object> result = new HashMap<>();
        try {
            rule.setStatus(1);
            int rows = pointsRuleMapper.insert(rule);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            log.error("添加积分规则异常", e);
        }
        return result;
    }

    /**
     * 更新积分规则
     */
    @PostMapping("/rule/update")
    @ResponseBody
    public Map<String, Object> updateRule(@RequestBody PointsRule rule) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = pointsRuleMapper.updateById(rule);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("更新积分规则异常", e);
        }
        return result;
    }

    /**
     * 删除积分规则
     */
    @PostMapping("/rule/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteRule(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = pointsRuleMapper.deleteById(id);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
            log.error("删除积分规则异常", e);
        }
        return result;
    }

    /**
     * 启用/禁用规则
     */
    @PostMapping("/rule/toggle-status/{id}")
    @ResponseBody
    public Map<String, Object> toggleStatus(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            PointsRule rule = pointsRuleMapper.selectById(id);
            if (rule != null) {
                rule.setStatus(rule.getStatus() == 1 ? 0 : 1);
                int rows = pointsRuleMapper.updateById(rule);
                result.put("success", rows > 0);
                result.put("message", rows > 0 ? "操作成功" : "操作失败");
            } else {
                result.put("success", false);
                result.put("message", "规则不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
            log.error("切换状态异常", e);
        }
        return result;
    }
}
