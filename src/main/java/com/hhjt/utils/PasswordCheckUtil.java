package com.hhjt.utils;

import org.springframework.util.StringUtils;

/**
 * 密码校验工具类（集中管理密码规则）
 */
public class PasswordCheckUtil {

    // 密码最小长度（可配置，当前设为6位）
    private static final int MIN_PASSWORD_LENGTH = 6;
    // 密码校验失败提示
    public static final String PASSWORD_TOO_SHORT_MSG = "密码长度不能少于" + MIN_PASSWORD_LENGTH + "位";

    /**
     * 校验密码长度合法性
     * @param rawPassword 明文密码
     * @return true-合法，false-非法
     */
    public static boolean checkPasswordLength(String rawPassword) {
        // 空密码（由服务层兜底默认密码）直接放行，后续加密时统一处理
        if (!StringUtils.hasText(rawPassword)) {
            return true;
        }
        return rawPassword.length() >= MIN_PASSWORD_LENGTH;
    }
}