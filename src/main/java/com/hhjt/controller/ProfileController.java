package com.hhjt.controller;

import com.hhjt.entity.Student;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.ClassService;
import com.hhjt.utils.ErrorCountUtil;
import com.hhjt.utils.PasswordCheckUtil;
import com.hhjt.utils.UploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 统一个人信息控制器（BCrypt加密+密码复杂度校验）
 */
@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final UploadUtil uploadUtil;
    private final ErrorCountUtil errorCountUtil;
    private final ClassService classService;

    // 核心修改：补充长度校验规则（至少6位）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)(?![^0-9a-zA-Z]+$).{6,}$");
    // 核心修改：更新提示文案，明确长度+复杂度要求
    private static final String PASSWORD_RULE_TIP = PasswordCheckUtil.PASSWORD_TOO_SHORT_MSG + "，且包含字母、数字、特殊字符中至少2种组合";

    // ==================== 以下为原有方法（无修改，仅保证代码完整性） ====================
    @GetMapping
    public String profile(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);

        if (user.getRoleId() == 2L) {
            Teacher teacher = teacherMapper.selectByUserId(user.getId());
            model.addAttribute("teacher", teacher);
        } else if (user.getRoleId() == 3L) { // 学生
            Student student = studentMapper.selectByUserId(user.getId());
            if (student != null && student.getClassId() != null) {
                SysClass sysClass = classService.getClassById(student.getClassId());
                student.setSysClass(sysClass);
            }
            model.addAttribute("student", student);
        }

        return "profile/profile";
    }

    @PostMapping("/upload-avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("avatarFile") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String avatarUrl = uploadUtil.uploadAvatar(file);
            User user = getCurrentUser();
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            result.put("success", true);
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");
        } catch (Exception e) {
            log.error("头像上传失败", e);
            result.put("success", false);
            result.put("message", "头像上传失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/update-base")
    public String updateBaseInfo(User user, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            currentUser.setUsername(user.getUsername());
            currentUser.setRealName(user.getRealName());
            currentUser.setPhone(user.getPhone());
            currentUser.setEmail(user.getEmail());
            currentUser.setSignature(user.getSignature());
            currentUser.setUpdateTime(LocalDateTime.now());

            userMapper.updateById(currentUser);
            redirectAttributes.addFlashAttribute("successMsg", "基础信息更新成功");
        } catch (Exception e) {
            log.error("更新基础信息失败", e);
            redirectAttributes.addFlashAttribute("errorMsg", "更新失败：" + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-password")
    @ResponseBody
    public Map<String, Object> updatePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 两次密码一致性校验
            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "两次新密码输入不一致");
                return result;
            }

            // 2. 密码规则校验（长度+复杂度）
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                result.put("success", false);
                result.put("message", PASSWORD_RULE_TIP);
                return result;
            }

            // 3. 原密码校验
            User user = getCurrentUser();
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                result.put("success", false);
                result.put("message", "原密码输入错误");
                return result;
            }

            // 4. BCrypt加密更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            // 5. 重置错误计数
            errorCountUtil.resetErrorCount(user);
            userMapper.updateById(user);

            result.put("success", true);
            result.put("message", "密码修改成功，请重新登录");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            result.put("success", false);
            result.put("message", "修改失败：" + e.getMessage());
        }
        return result;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}