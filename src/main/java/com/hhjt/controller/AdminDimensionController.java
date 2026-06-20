package com.hhjt.controller;

import com.hhjt.annotation.OperationLog;
import com.hhjt.entity.EvaluationDimension;
import com.hhjt.mapper.EvaluationDimensionMapper;
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
 * 管理员 - 评价维度配置控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/dimension")
public class AdminDimensionController {

    @Autowired
    private EvaluationDimensionMapper dimensionMapper;

    /**
     * 维度配置页面
     */
    @GetMapping("/config")
    public String dimensionConfig(Model model) {
        List<EvaluationDimension> dimensions = dimensionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationDimension>()
                .orderByAsc(EvaluationDimension::getSortOrder)
        );
        model.addAttribute("dimensions", dimensions);
        return "admin/dimension_config";
    }

    /**
     * 更新维度权重
     */
    @PostMapping("/update")
    @ResponseBody
    @OperationLog(module = "维度配置", operation = "更新权重")
    public Map<String, Object> updateDimension(@RequestBody EvaluationDimension dimension) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证权重总和是否为1
            List<EvaluationDimension> allDimensions = dimensionMapper.selectList(null);
            BigDecimal totalWeight = BigDecimal.ZERO;
            for (EvaluationDimension dim : allDimensions) {
                if (dim.getId().equals(dimension.getId())) {
                    totalWeight = totalWeight.add(dimension.getWeight());
                } else {
                    totalWeight = totalWeight.add(dim.getWeight());
                }
            }
            
            // 允许误差0.01
            if (totalWeight.compareTo(new BigDecimal("1.01")) > 0 || 
                totalWeight.compareTo(new BigDecimal("0.99")) < 0) {
                result.put("success", false);
                result.put("message", "权重总和必须为1.0（当前：" + totalWeight + "）");
                return result;
            }

            int rows = dimensionMapper.updateById(dimension);
            result.put("success", rows > 0);
            result.put("message", rows > 0 ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("更新维度异常", e);
        }
        return result;
    }

    /**
     * 批量更新权重
     */
    @PostMapping("/batch-update")
    @ResponseBody
    @OperationLog(module = "维度配置", operation = "批量更新")
    public Map<String, Object> batchUpdateWeights(@RequestBody List<EvaluationDimension> dimensions) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证权重总和
            BigDecimal totalWeight = dimensions.stream()
                .map(EvaluationDimension::getWeight)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            if (totalWeight.compareTo(new BigDecimal("1.01")) > 0 || 
                totalWeight.compareTo(new BigDecimal("0.99")) < 0) {
                result.put("success", false);
                result.put("message", "权重总和必须为1.0（当前：" + totalWeight + "）");
                return result;
            }

            int successCount = 0;
            for (EvaluationDimension dimension : dimensions) {
                int rows = dimensionMapper.updateById(dimension);
                if (rows > 0) {
                    successCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "成功更新" + successCount + "个维度");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量更新失败：" + e.getMessage());
            log.error("批量更新维度异常", e);
        }
        return result;
    }
}
