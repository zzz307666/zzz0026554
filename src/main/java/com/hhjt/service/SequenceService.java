package com.hhjt.service;

/**
 * 自增序列服务
 */
public interface SequenceService {
    /**
     * 获取教师工号（自动生成：T+日期+3位流水号）
     * @return 教师工号（如T20260221001）
     */
    String getTeacherNo();

    /**
     * 获取学生学号（自动生成：S+日期+3位流水号）
     * @return 学生学号（如S20260221001）
     */
    String getStudentNo();
}