package com.hhjt.controller;

import com.hhjt.dto.StudentDTO;
import com.hhjt.dto.TeacherDTO;
import com.hhjt.entity.User;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.AdminService;
import com.hhjt.service.UserService;
import com.hhjt.utils.ErrorCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final UserService userService;
    private final AdminService adminService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ErrorCountUtil errorCountUtil;

    @GetMapping
    public String resetPasswordPage() {
        return "reset-password";
    }

    @PostMapping("/do-verify")
    public String doVerify(
            @RequestParam("roleType") String roleType,
            @RequestParam("username") String username,
            @RequestParam("realName") String realName,
            @RequestParam(value = "studentNo", required = false) String studentNo,
            @RequestParam(value = "teacherNo", required = false) String teacherNo) {
        try {
            if (!StringUtils.hasText(username) || !StringUtils.hasText(realName)) {
                String msg = URLEncoder.encode("用户名和真实姓名不能为空", "UTF-8");
                return "redirect:/reset-password?error=true&msg=" + msg;
            }

            User user = null;
            if ("student".equals(roleType)) {
                if (!StringUtils.hasText(studentNo)) {
                    String msg = URLEncoder.encode("学生学号不能为空", "UTF-8");
                    return "redirect:/reset-password?error=true&msg=" + msg;
                }
                List<StudentDTO> studentList = adminService.getAllStudentList(realName, studentNo, null);
                user = studentList.stream()
                        .filter(s -> username.equals(s.getUsername()))
                        .findFirst()
                        .map(s -> userService.getById(s.getUserId()))
                        .orElse(null);
            } else if ("teacher".equals(roleType)) {
                if (!StringUtils.hasText(teacherNo)) {
                    String msg = URLEncoder.encode("教师工号不能为空", "UTF-8");
                    return "redirect:/reset-password?error=true&msg=" + msg;
                }
                List<TeacherDTO> teacherList = adminService.getAllTeacherList(realName, teacherNo);
                user = teacherList.stream()
                        .filter(t -> username.equals(t.getUsername()))
                        .findFirst()
                        .map(t -> userService.getById(t.getUserId()))
                        .orElse(null);
            } else {
                String msg = URLEncoder.encode("角色类型错误", "UTF-8");
                return "redirect:/reset-password?error=true&msg=" + msg;
            }

            // 验证失败→统计错误次数
            if (user == null) {
                User dbUser = userMapper.selectByUsername(username);
                if (dbUser != null && dbUser.getStatus() == 1) {
                    boolean isOverLimit = errorCountUtil.addErrorCount(dbUser, ErrorCountUtil.TYPE_RESET_PWD);
                    if (isOverLimit) {
                        dbUser.setStatus(0);
                        log.error("用户{}单日重置密码错误超8次，已自动禁用", username);
                        String msg = URLEncoder.encode("单日登录/重置密码错误超8次，账号已禁用，请联系管理员", "UTF-8");
                        dbUser.setUpdateTime(LocalDateTime.now());
                        userMapper.updateById(dbUser);
                        return "redirect:/reset-password?error=true&msg=" + msg;
                    }
                    dbUser.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(dbUser);
                }
                String msg = URLEncoder.encode("用户名、姓名或学号/工号不匹配", "UTF-8");
                return "redirect:/reset-password?error=true&msg=" + msg;
            }

            // 验证成功→重置错误计数+重置密码（abc123）
            errorCountUtil.resetErrorCount(user);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            boolean resetSuccess = adminService.resetPassword(user.getId());
            if (resetSuccess) {
                log.info("用户{}（{}）密码重置成功", username, realName);
                return "redirect:/reset-password?success=true";
            } else {
                String msg = URLEncoder.encode("密码重置失败，请联系管理员", "UTF-8");
                return "redirect:/reset-password?error=true&msg=" + msg;
            }
        } catch (Exception e) {
            log.error("密码重置异常", e);
            return "redirect:/reset-password?error=true&msg=系统异常，请稍后重试";
        }
    }
}