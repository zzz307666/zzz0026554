package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 学生成长档案服务接口
 */
public interface StudentProfileService {
    
    /**
     * 获取学生基本信息
     * @param studentId 学生ID
     * @return 学生基本信息
     */
    Map<String, Object> getStudentBasicInfo(Long studentId);
    
    /**
     * 获取学生运动记录时间轴
     * @param studentId 学生ID
     * @return 运动记录列表
     */
    List<Map<String, Object>> getSportRecordTimeline(Long studentId);
    
    /**
     * 获取学生积分变化曲线
     * @param studentId 学生ID
     * @return 积分变化数据
     */
    List<Map<String, Object>> getPointsChangeCurve(Long studentId);
    
    /**
     * 获取学生评价历史雷达图数据
     * @param studentId 学生ID
     * @return 评价维度数据
     */
    Map<String, Object> getEvaluationRadarData(Long studentId);
    
    /**
     * 获取学生成长趋势分析
     * @param studentId 学生ID
     * @return 成长趋势数据
     */
    Map<String, Object> getGrowthTrendAnalysis(Long studentId);
}
