package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.SportReminder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 运动提醒Mapper
 */
@Mapper
public interface SportReminderMapper extends BaseMapper<SportReminder> {
    
    /**
     * 查询所有启用的提醒
     */
    default List<SportReminder> selectActiveReminders() {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportReminder>()
                .eq(SportReminder::getIsActive, 1)
        );
    }
}
