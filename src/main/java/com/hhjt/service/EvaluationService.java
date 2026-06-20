package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.StudentEvaluation;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生评价服务接口
 */
public interface EvaluationService {
    
    /**
     * 保存评价（草稿）
     */
    boolean saveEvaluation(StudentEvaluation evaluation);
    
    /**
     * 发布评价
     */
    boolean publishEvaluation(Long evaluationId);
    
    /**
     * 查询学生评价列表
     */
    List<StudentEvaluation> getStudentEvaluations(Long studentId);
    
    /**
     * 分页查询评价列表
     * @param page 页码
     * @param size 每页大小
     * @param teacherId 教师ID（用于过滤该教师的评价）
     * @param period 评价周期（可选）
     * @param classIds 班级ID列表（用于过滤教师负责班级的学生评价）
     * @param isAdmin 是否为管理员（true=查看所有，false=只看教师负责班级）
     */
    IPage<StudentEvaluation> getEvaluationPage(Integer page, Integer size, Long teacherId, 
                                               String period, List<Long> classIds, boolean isAdmin);
    
    /**
     * 计算综合得分
     */
    BigDecimal calculateTotalScore(StudentEvaluation evaluation);
    
    /**
     * 确定等级
     */
    String determineGradeLevel(BigDecimal totalScore);
    
    /**
     * 根据ID查询评价
     */
    StudentEvaluation getEvaluationById(Long id);
}
