package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.EvaluationDimension;
import com.hhjt.entity.StudentEvaluation;
import com.hhjt.mapper.EvaluationDimensionMapper;
import com.hhjt.mapper.StudentEvaluationMapper;
import com.hhjt.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生评价服务实现类
 */
@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private StudentEvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationDimensionMapper dimensionMapper;
    
    @Autowired
    private com.hhjt.mapper.StudentMapper studentMapper;
    
    @Autowired
    private com.hhjt.mapper.UserMapper userMapper;
    
    @Autowired
    private com.hhjt.mapper.PointsRuleMapper pointsRuleMapper;
    
    @Autowired
    private com.hhjt.service.PointsService pointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveEvaluation(StudentEvaluation evaluation) {
        // 计算总分
        BigDecimal totalScore = calculateTotalScore(evaluation);
        evaluation.setTotalScore(totalScore);

        // 确定等级
        String gradeLevel = determineGradeLevel(totalScore);
        evaluation.setGradeLevel(gradeLevel);

        evaluation.setStatus(0); // 草稿
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setUpdateTime(LocalDateTime.now());

        if (evaluation.getId() == null) {
            return evaluationMapper.insert(evaluation) > 0;
        } else {
            return evaluationMapper.updateById(evaluation) > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishEvaluation(Long evaluationId) {
        StudentEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new RuntimeException("评价不存在");
        }

        evaluation.setStatus(1); // 已发布
        evaluation.setUpdateTime(LocalDateTime.now());
        
        boolean success = evaluationMapper.updateById(evaluation) > 0;
        
        // 发布成功后，根据等级自动加分
        if (success && evaluation.getGradeLevel() != null) {
            try {
                addPointsByGradeLevel(evaluation.getStudentId(), evaluation.getGradeLevel(), evaluation.getId());
            } catch (Exception e) {
                // 积分添加失败不影响评价发布，只记录日志
                System.err.println("添加评价积分失败: " + e.getMessage());
            }
        }

        return success;
    }

    @Override
    public List<StudentEvaluation> getStudentEvaluations(Long studentId) {
        LambdaQueryWrapper<StudentEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentEvaluation::getStudentId, studentId)
               .eq(StudentEvaluation::getStatus, 1) // 只查询已发布的
               .orderByDesc(StudentEvaluation::getCreateTime);

        return evaluationMapper.selectList(wrapper);
    }

    @Override
    public IPage<StudentEvaluation> getEvaluationPage(Integer page, Integer size, 
                                                      Long teacherId, String period,
                                                      List<Long> classIds, boolean isAdmin) {
        Page<StudentEvaluation> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<StudentEvaluation> wrapper = new LambdaQueryWrapper<>();

        // 如果不是管理员，需要过滤教师负责班级的学生评价
        if (!isAdmin && classIds != null && !classIds.isEmpty()) {
            // 子查询：只查询这些班级学生的评价
            wrapper.inSql(StudentEvaluation::getStudentId, 
                "SELECT id FROM sys_student WHERE class_id IN (" + 
                String.join(",", classIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
            );
        } else if (!isAdmin && (classIds == null || classIds.isEmpty())) {
            // 如果教师没有负责任何班级，返回空结果
            wrapper.eq(StudentEvaluation::getId, -1); // 不可能存在的ID
        }
        // 如果是管理员，不添加班级过滤条件
        
        if (period != null && !period.isEmpty()) {
            wrapper.eq(StudentEvaluation::getEvaluationPeriod, period);
        }

        wrapper.orderByDesc(StudentEvaluation::getCreateTime);

        IPage<StudentEvaluation> result = evaluationMapper.selectPage(pageInfo, wrapper);
        
        // 填充学生信息
        if (result.getRecords() != null) {
            for (StudentEvaluation eval : result.getRecords()) {
                if (eval.getStudentId() != null) {
                    com.hhjt.entity.Student student = studentMapper.selectById(eval.getStudentId());
                    if (student != null) {
                        eval.setStudentNo(student.getStudentNo());
                        // 查询学生姓名
                        com.hhjt.entity.User user = userMapper.selectById(student.getUserId());
                        if (user != null) {
                            eval.setStudentName(user.getRealName());
                        }
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    public BigDecimal calculateTotalScore(StudentEvaluation evaluation) {
        // 获取所有启用的维度
        List<EvaluationDimension> dimensions = dimensionMapper.selectList(
            new LambdaQueryWrapper<EvaluationDimension>()
                .eq(EvaluationDimension::getStatus, 1)
                .orderByAsc(EvaluationDimension::getSortOrder)
        );

        BigDecimal totalScore = BigDecimal.ZERO;

        for (EvaluationDimension dim : dimensions) {
            BigDecimal score = getDimensionScore(dim.getDimensionCode(), evaluation);
            if (score != null) {
                totalScore = totalScore.add(score.multiply(dim.getWeight()));
            }
        }

        return totalScore.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String determineGradeLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal(90)) >= 0) {
            return "优秀";
        } else if (totalScore.compareTo(new BigDecimal(80)) >= 0) {
            return "良好";
        } else if (totalScore.compareTo(new BigDecimal(70)) >= 0) {
            return "中等";
        } else if (totalScore.compareTo(new BigDecimal(60)) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    @Override
    public StudentEvaluation getEvaluationById(Long id) {
        return evaluationMapper.selectById(id);
    }

    /**
     * 获取维度评分
     */
    private BigDecimal getDimensionScore(String dimensionCode, StudentEvaluation evaluation) {
        switch (dimensionCode) {
            case "ENDURANCE":
                return evaluation.getEnduranceScore();
            case "STRENGTH":
                return evaluation.getStrengthScore();
            case "SPEED":
                return evaluation.getSpeedScore();
            case "FLEXIBILITY":
                return evaluation.getFlexibilityScore();
            case "COORDINATION":
                return evaluation.getCoordinationScore();
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * 根据评价等级自动加分
     */
    private void addPointsByGradeLevel(Long studentId, String gradeLevel, Long evaluationId) {
        // 根据等级查找对应的积分规则
        String ruleCode = null;
        switch (gradeLevel) {
            case "优秀":
                ruleCode = "EVALUATION_EXCELLENT";
                break;
            case "良好":
                ruleCode = "EVALUATION_GOOD";
                break;
            case "中等":
                ruleCode = "EVALUATION_MEDIUM";
                break;
            case "及格":
                ruleCode = "EVALUATION_PASS";
                break;
            default:
                // 不及格不加分
                return;
        }
        
        // 查询积分规则
        com.hhjt.entity.PointsRule rule = pointsRuleMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hhjt.entity.PointsRule>()
                .eq(com.hhjt.entity.PointsRule::getRuleCode, ruleCode)
                .eq(com.hhjt.entity.PointsRule::getStatus, 1)
        );
        
        if (rule != null && rule.getPointsValue() != null && rule.getPointsValue().compareTo(BigDecimal.ZERO) > 0) {
            // 调用积分服务添加积分
            pointsService.addPoints(studentId, "EVALUATION_REWARD", rule.getPointsValue(), 
                "评价获得" + gradeLevel + "等级，奖励" + rule.getPointsValue() + "积分", 
                evaluationId);
        }
    }
}
