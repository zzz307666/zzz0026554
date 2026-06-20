package com.hhjt.service.impl;

import com.hhjt.entity.SportChallenge;
import com.hhjt.entity.SportShare;
import com.hhjt.entity.User;
import com.hhjt.entity.UserFriend;
import com.hhjt.mapper.SportChallengeMapper;
import com.hhjt.mapper.SportShareMapper;
import com.hhjt.mapper.UserFriendMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.SocialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 同伴社交服务实现类
 */
@Slf4j
@Service
public class SocialServiceImpl implements SocialService {

    @Autowired
    private UserFriendMapper friendMapper;

    @Autowired
    private SportChallengeMapper challengeMapper;

    @Autowired
    private SportShareMapper shareMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFriend(Long userId, Long friendId, String remark) {
        try {
            // 检查是否已经是好友
            UserFriend existing = friendMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                    .eq(UserFriend::getUserId, userId)
                    .eq(UserFriend::getFriendId, friendId)
            );
            
            if (existing != null) {
                return false;
            }
            
            // 创建好友关系（双向）
            UserFriend friend1 = new UserFriend();
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setRemark(remark);
            friend1.setStatus(1); // 直接确认为好友
            friend1.setCreateTime(LocalDateTime.now());
            friendMapper.insert(friend1);
            
            UserFriend friend2 = new UserFriend();
            friend2.setUserId(friendId);
            friend2.setFriendId(userId);
            friend2.setRemark("");
            friend2.setStatus(1);
            friend2.setCreateTime(LocalDateTime.now());
            friendMapper.insert(friend2);
            
            return true;
        } catch (Exception e) {
            log.error("添加好友失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询用户的好友列表
            List<UserFriend> friends = friendMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                    .eq(UserFriend::getUserId, userId)
                    .eq(UserFriend::getStatus, 1)
            );
            
            for (UserFriend friend : friends) {
                Map<String, Object> friendData = new HashMap<>();
                friendData.put("id", friend.getId());
                friendData.put("friendId", friend.getFriendId());
                friendData.put("remark", friend.getRemark() != null ? friend.getRemark() : "");
                friendData.put("status", friend.getStatus());
                
                // 查询好友的真实姓名
                User friendUser = userMapper.selectById(friend.getFriendId());
                friendData.put("friendName", friendUser != null ? friendUser.getRealName() : "用户" + friend.getFriendId());
                
                result.add(friendData);
            }
        } catch (Exception e) {
            log.error("获取好友列表失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFriend(Long userId, Long friendId) {
        try {
            // 删除双向好友关系
            friendMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                    .eq(UserFriend::getUserId, userId)
                    .eq(UserFriend::getFriendId, friendId)
            );
            
            friendMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                    .eq(UserFriend::getUserId, friendId)
                    .eq(UserFriend::getFriendId, userId)
            );
            
            return true;
        } catch (Exception e) {
            log.error("删除好友失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createChallenge(Long challengerId, Long challengedId, String challengeType,
                                  Double targetValue, String unit, Integer days) {
        try {
            SportChallenge challenge = new SportChallenge();
            challenge.setChallengerId(challengerId);
            challenge.setChallengedId(challengedId);
            challenge.setChallengeType(challengeType);
            challenge.setTargetValue(targetValue);
            challenge.setUnit(unit);
            challenge.setStartDate(LocalDateTime.now());
            challenge.setEndDate(LocalDateTime.now().plusDays(days));
            challenge.setStatus(0); // 进行中
            challenge.setCreateTime(LocalDateTime.now());
            
            challengeMapper.insert(challenge);
            return true;
        } catch (Exception e) {
            log.error("创建挑战失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getChallengeList(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询用户相关的挑战（发起或接受）
            List<SportChallenge> challenges = challengeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportChallenge>()
                    .and(wrapper -> wrapper
                        .eq(SportChallenge::getChallengerId, userId)
                        .or()
                        .eq(SportChallenge::getChallengedId, userId)
                    )
                    .orderByDesc(SportChallenge::getCreateTime)
            );
            
            for (SportChallenge challenge : challenges) {
                Map<String, Object> challengeData = new HashMap<>();
                challengeData.put("id", challenge.getId());
                challengeData.put("challengerId", challenge.getChallengerId());
                challengeData.put("challengedId", challenge.getChallengedId());
                challengeData.put("challengeType", challenge.getChallengeType());
                challengeData.put("targetValue", challenge.getTargetValue());
                challengeData.put("unit", challenge.getUnit());
                challengeData.put("status", challenge.getStatus());
                challengeData.put("startDate", challenge.getStartDate());
                challengeData.put("endDate", challenge.getEndDate());
                challengeData.put("winnerId", challenge.getWinnerId());
                
                // 查询用户的真实姓名
                User challengerUser = userMapper.selectById(challenge.getChallengerId());
                User challengedUser = userMapper.selectById(challenge.getChallengedId());
                challengeData.put("challengerName", challengerUser != null ? challengerUser.getRealName() : "用户" + challenge.getChallengerId());
                challengeData.put("challengedName", challengedUser != null ? challengedUser.getRealName() : "用户" + challenge.getChallengedId());
                
                result.add(challengeData);
            }
        } catch (Exception e) {
            log.error("获取挑战列表失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeChallenge(Long challengeId, Long winnerId) {
        try {
            SportChallenge challenge = challengeMapper.selectById(challengeId);
            if (challenge != null) {
                challenge.setStatus(1); // 已完成
                challenge.setWinnerId(winnerId);
                challengeMapper.updateById(challenge);
            }
            return true;
        } catch (Exception e) {
            log.error("完成挑战失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishShare(Long userId, String userName, String content, String imageUrl) {
        try {
            SportShare share = new SportShare();
            share.setUserId(userId);
            share.setUserName(userName);
            share.setContent(content);
            share.setImageUrl(imageUrl);
            share.setLikeCount(0);
            share.setCommentCount(0);
            share.setCreateTime(LocalDateTime.now());
            
            shareMapper.insert(share);
            return true;
        } catch (Exception e) {
            log.error("发布分享失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getShareWall(Integer page, Integer size) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 分页查询分享列表
            com.baomidou.mybatisplus.core.metadata.IPage<SportShare> sharePage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportShare> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.orderByDesc(SportShare::getCreateTime);
            
            sharePage = shareMapper.selectPage(sharePage, wrapper);
            
            for (SportShare share : sharePage.getRecords()) {
                Map<String, Object> shareData = new HashMap<>();
                shareData.put("id", share.getId());
                shareData.put("userId", share.getUserId());
                shareData.put("userName", share.getUserName());
                shareData.put("content", share.getContent());
                shareData.put("imageUrl", share.getImageUrl());
                shareData.put("likeCount", share.getLikeCount());
                shareData.put("commentCount", share.getCommentCount());
                shareData.put("createTime", share.getCreateTime());
                
                result.add(shareData);
            }
        } catch (Exception e) {
            log.error("获取分享墙失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeShare(Long shareId, Long userId) {
        try {
            SportShare share = shareMapper.selectById(shareId);
            if (share != null) {
                share.setLikeCount(share.getLikeCount() + 1);
                shareMapper.updateById(share);
            }
            return true;
        } catch (Exception e) {
            log.error("点赞失败", e);
            return false;
        }
    }
}
