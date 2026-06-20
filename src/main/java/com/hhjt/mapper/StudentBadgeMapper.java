package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.StudentBadge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生徽章记录 Mapper
 */
@Mapper
public interface StudentBadgeMapper extends BaseMapper<StudentBadge> {
}
