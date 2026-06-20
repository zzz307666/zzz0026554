package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知 Mapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
    
    /**
     * 标记消息为已读
     */
    @Update("UPDATE sys_message SET is_read = 1, read_time = NOW() WHERE id = #{id}")
    int markAsRead(Long id);
    
    /**
     * 批量标记为已读
     */
    @Update("UPDATE sys_message SET is_read = 1, read_time = NOW() WHERE receiver_id = #{receiverId} AND is_read = 0")
    int markAllAsRead(Long receiverId);
}
