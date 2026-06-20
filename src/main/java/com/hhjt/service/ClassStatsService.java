package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 班级统计服务接口
 */
public interface ClassStatsService {
    
    /**
     * 获取全校班级运动参与率
     * @return 班级参与率列表
     */
    List<Map<String, Object>> getClassParticipationRate();
    
    /**
     * 获取各班级平均积分排名
     * @return 班级积分排名列表
     */
    List<Map<String, Object>> getClassPointsRanking();
    
    /**
     * 获取运动类型分布统计
     * @return 运动类型分布数据
     */
    Map<String, Object> getSportTypeDistribution();
    
    /**
     * 获取时间段趋势分析
     * @param period 时间段（week/month/semester）
     * @return 趋势数据
     */
    List<Map<String, Object>> getTimeTrendAnalysis(String period);
    
    /**
     * 获取班级详细统计数据
     * @param classId 班级ID
     * @return 班级统计数据
     */
    Map<String, Object> getClassDetailStats(Long classId);
}
