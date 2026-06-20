package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.UserFriend;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系Mapper接口
 */
@Mapper
public interface UserFriendMapper extends BaseMapper<UserFriend> {
}
