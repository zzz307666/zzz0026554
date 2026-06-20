package com.hhjt.service.impl;

import com.hhjt.entity.User;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserSessionServiceImpl implements UserSessionService {

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void invalidateUserSession(Long userId) {
        if (sessionRegistry == null) {
            log.warn("SessionRegistry not configured, cannot invalidate user session");
            return;
        }

        try {
            List<Object> principals = sessionRegistry.getAllPrincipals();
            for (Object principal : principals) {
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    User user = userMapper.selectByUsername(userDetails.getUsername());
                    if (user != null && user.getId().equals(userId)) {
                        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                        for (SessionInformation session : sessions) {
                            session.expireNow();
                            log.info("Invalidated session for user: {}", userId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to invalidate user session", e);
        }
    }

    @Override
    public void invalidateRoleSessions(Long roleId) {
        if (sessionRegistry == null) {
            log.warn("SessionRegistry not configured, cannot invalidate role sessions");
            return;
        }

        try {
            List<User> users = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                    .eq("role_id", roleId)
            );

            for (User user : users) {
                invalidateUserSession(user.getId());
            }
            log.info("Invalidated {} sessions for role: {}", users.size(), roleId);
        } catch (Exception e) {
            log.error("Failed to invalidate role sessions", e);
        }
    }

    @Override
    public List<Map<String, Object>> getOnlineUsers() {
        List<Map<String, Object>> result = new ArrayList<>();
        if (sessionRegistry == null) {
            return result;
        }

        try {
            List<Object> principals = sessionRegistry.getAllPrincipals();
            for (Object principal : principals) {
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    User user = userMapper.selectByUsername(userDetails.getUsername());
                    if (user != null) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("userId", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("roleId", user.getRoleId());
                        result.add(userInfo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get online users", e);
        }
        return result;
    }

    @Override
    public int getOnlineUserCount() {
        if (sessionRegistry == null) {
            return 0;
        }
        return (int) sessionRegistry.getAllPrincipals().stream()
            .filter(p -> p instanceof UserDetails)
            .count();
    }

    @Override
    public int getOnlineUserCountByRole(String roleCode) {
        if (sessionRegistry == null) {
            return 0;
        }

        try {
            List<Object> principals = sessionRegistry.getAllPrincipals();
            int count = 0;
            for (Object principal : principals) {
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    User user = userMapper.selectByUsername(userDetails.getUsername());
                    if (user != null) {
                        Long roleId = user.getRoleId();
                        if ("ADMIN".equals(roleCode) && roleId == 1) {
                            count++;
                        } else if ("TEACHER".equals(roleCode) && roleId == 2) {
                            count++;
                        } else if ("STUDENT".equals(roleCode) && roleId == 3) {
                            count++;
                        }
                    }
                }
            }
            return count;
        } catch (Exception e) {
            log.error("Failed to get online user count by role", e);
            return 0;
        }
    }

    @Override
    public int getPeakOnlineCountToday() {
        return getOnlineUserCount();
    }

    @Override
    public String getPeakOnlineTimeToday() {
        return "N/A";
    }
}