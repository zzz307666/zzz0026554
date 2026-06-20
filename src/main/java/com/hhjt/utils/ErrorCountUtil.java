package com.hhjt.utils;

import com.hhjt.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 错误次数统计工具类（登录/重置密码）
 * 错误次数持久化到数据库，单日超8次禁用账号
 */
@Component
public class ErrorCountUtil {
    private static final int ERROR_LIMIT = 8;
    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_RESET_PWD = "resetPwd";

    /**
     * 累加错误次数并判断是否超限
     * @param user 目标用户（直接操作数据库实体，修改后需调用userMapper.updateById(user)）
     * @param type 错误类型
     * @return true=超限（需禁用账号）
     */
    public boolean addErrorCount(User user, String type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstErrorTime = user.getFirstErrorTime();

        // 跨天/首次错误→重置计数
        if (firstErrorTime == null || ChronoUnit.DAYS.between(firstErrorTime, now) >= 1) {
            user.setLoginErrorCount(0);
            user.setResetPwdErrorCount(0);
            user.setFirstErrorTime(now);
        }

        // 累加对应类型错误次数
        if (TYPE_LOGIN.equals(type)) {
            user.setLoginErrorCount(user.getLoginErrorCount() == null ? 1 : user.getLoginErrorCount() + 1);
        } else if (TYPE_RESET_PWD.equals(type)) {
            user.setResetPwdErrorCount(user.getResetPwdErrorCount() == null ? 1 : user.getResetPwdErrorCount() + 1);
        }

        // 判断是否超限
        int loginCount = user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount();
        int resetCount = user.getResetPwdErrorCount() == null ? 0 : user.getResetPwdErrorCount();
        return loginCount >= ERROR_LIMIT || resetCount >= ERROR_LIMIT;
    }

    /**
     * 重置错误计数（登录/重置密码成功时调用）
     * @param user 目标用户（修改后需调用userMapper.updateById(user)）
     */
    public void resetErrorCount(User user) {
        user.setLoginErrorCount(0);
        user.setResetPwdErrorCount(0);
        user.setFirstErrorTime(null);
    }
}