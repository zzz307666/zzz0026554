package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 同伴社交服务接口
 */
public interface SocialService {
    
    /**
     * 添加好友
     */
    boolean addFriend(Long userId, Long friendId, String remark);
    
    /**
     * 获取好友列表
     */
    List<Map<String, Object>> getFriendList(Long userId);
    
    /**
     * 删除好友
     */
    boolean deleteFriend(Long userId, Long friendId);
    
    /**
     * 发起挑战
     */
    boolean createChallenge(Long challengerId, Long challengedId, String challengeType, 
                           Double targetValue, String unit, Integer days);
    
    /**
     * 获取挑战列表
     */
    List<Map<String, Object>> getChallengeList(Long userId);
    
    /**
     * 完成挑战
     */
    boolean completeChallenge(Long challengeId, Long winnerId);
    
    /**
     * 发布运动分享
     */
    boolean publishShare(Long userId, String userName, String content, String imageUrl);
    
    /**
     * 获取分享墙
     */
    List<Map<String, Object>> getShareWall(Integer page, Integer size);
    
    /**
     * 点赞分享
     */
    boolean likeShare(Long shareId, Long userId);
}
