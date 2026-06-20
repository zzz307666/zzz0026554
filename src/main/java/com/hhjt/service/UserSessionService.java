package com.hhjt.service;

import java.util.List;
import java.util.Map;

public interface UserSessionService {

    void invalidateUserSession(Long userId);

    void invalidateRoleSessions(Long roleId);

    List<Map<String, Object>> getOnlineUsers();

    int getOnlineUserCount();

    int getOnlineUserCountByRole(String roleCode);

    int getPeakOnlineCountToday();

    String getPeakOnlineTimeToday();
}