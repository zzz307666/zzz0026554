package com.hhjt.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhjt.annotation.OperationLog;
import com.hhjt.entity.User;
import com.hhjt.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.hhjt.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        String module = annotation.module();
        String operation = annotation.operation();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = getIpAddress(request);

        // 获取当前用户
        Long userId = null;
        String username = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                userId = user.getId();
                username = user.getUsername();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败", e);
        }

        // 获取请求参数
        String params = "";
        try {
            params = objectMapper.writeValueAsString(joinPoint.getArgs());
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
        } catch (Exception e) {
            params = "参数序列化失败";
        }

        // 创建日志实体
        com.hhjt.entity.OperationLog logEntity = new com.hhjt.entity.OperationLog();
        logEntity.setModule(module);
        logEntity.setOperation(operation);
        logEntity.setMethod(methodName);
        logEntity.setParams(params);
        logEntity.setUserId(userId);
        logEntity.setUsername(username);
        logEntity.setIp(ip);
        logEntity.setCreateTime(LocalDateTime.now());

        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            logEntity.setResult("SUCCESS");
            logEntity.setDuration(duration);
            operationLogMapper.insert(logEntity);
            
            return result;
        } catch (Throwable throwable) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            logEntity.setResult("FAIL");
            logEntity.setDuration(duration);
            logEntity.setErrorMsg(throwable.getMessage());
            operationLogMapper.insert(logEntity);
            
            throw throwable;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
