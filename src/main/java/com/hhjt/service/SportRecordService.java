package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SportRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运动打卡服务接口
 */
public interface SportRecordService {
    
    /**
     * 学生提交运动打卡
     */
    boolean submitRecord(SportRecord record, Long studentId);
    
    /**
     * 分页查询打卡记录
     * @param page 页码
     * @param size 每页大小
     * @param studentId 学生ID（可选）
     * @param status 状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param teacherId 教师ID（用于过滤该教师负责的班级）
     * @param isAdmin 是否为管理员（true=查看所有，false=只看教师负责班级）
     */
    IPage<SportRecord> getRecordPage(Integer page, Integer size, Long studentId, Integer status, 
                                     String startDate, String endDate, Long teacherId, boolean isAdmin);
    
    /**
     * 教师审核打卡记录
     */
    boolean auditRecord(Long recordId, Integer status, String remark, Long teacherId);
    
    /**
     * 批量审核
     */
    boolean batchAudit(List<Long> recordIds, Integer status, Long teacherId);
    
    /**
     * 计算积分
     */
    BigDecimal calculatePoints(SportRecord record);
    
    /**
     * 查询待审核数量
     * @param teacherId 教师ID
     * @param isAdmin 是否为管理员
     */
    long getPendingCount(Long teacherId, boolean isAdmin);
}
