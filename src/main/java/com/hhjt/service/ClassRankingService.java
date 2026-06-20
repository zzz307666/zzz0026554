package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 班级排行榜服务接口
 */
public interface ClassRankingService {
    
    /**
     * 获取班级积分排行榜
     * @param classId 班级ID
     * @param period 时间段（month/semester）
     * @return 排行榜数据
     */
    List<Map<String, Object>> getPointsRanking(Long classId, String period);
    
    /**
     * 获取班级运动次数排行榜
     * @param classId 班级ID
     * @param period 时间段（month/semester）
     * @return 排行榜数据
     */
    List<Map<String, Object>> getSportCountRanking(Long classId, String period);
    
    /**
     * 获取最佳进步奖
     * @param classId 班级ID
     * @param period 时间段
     * @return 进步学生列表
     */
    List<Map<String, Object>> getMostImprovedStudents(Long classId, String period);
    
    /**
     * 导出排行榜数据
     * @param classId 班级ID
     * @param rankingType 排行榜类型（points/sport_count）
     * @param period 时间段
     * @return CSV格式数据
     */
    String exportRanking(Long classId, String rankingType, String period);
}
