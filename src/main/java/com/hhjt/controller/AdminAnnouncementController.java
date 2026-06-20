package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Announcement;
import com.hhjt.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 - 公告管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/announcement")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 公告管理页面（兼容旧路径 /admin/announcements）
     */
    @GetMapping("/announcements")
    public String announcementsPageOld(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(required = false) Integer status,
                                        Model model) {
        IPage<Announcement> announcementPage = announcementService.getAnnouncementPage(page, size, type, status);
        model.addAttribute("page", announcementPage);
        return "admin/announcement";
    }

    /**
     * 公告管理页面（新路径 /admin/announcement/list）
     */
    @GetMapping("/list")
    public String announcementList(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) Integer status,
                                    Model model) {
        IPage<Announcement> announcementPage = announcementService.getAnnouncementPage(page, size, type, status);
        model.addAttribute("page", announcementPage);
        return "admin/announcement";
    }

    /**
     * 发布公告
     */
    @PostMapping("/publish")
    @ResponseBody
    public Map<String, Object> publishAnnouncement(@RequestBody Announcement announcement, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long publisherId = (Long) session.getAttribute("userId");
            if (publisherId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }
            
            boolean success = announcementService.publishAnnouncement(announcement, publisherId);
            result.put("success", success);
            result.put("message", success ? "发布成功" : "发布失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布失败: " + e.getMessage());
            log.error("发布公告异常", e);
        }
        return result;
    }

    /**
     * 更新公告
     */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateAnnouncement(@RequestBody Announcement announcement) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = announcementService.updateAnnouncement(announcement);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            log.error("更新公告异常", e);
        }
        return result;
    }

    /**
     * 下架公告
     */
    @PostMapping("/offline/{id}")
    @ResponseBody
    public Map<String, Object> offlineAnnouncement(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = announcementService.offlineAnnouncement(id);
            result.put("success", success);
            result.put("message", success ? "下架成功" : "下架失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "下架失败: " + e.getMessage());
            log.error("下架公告异常", e);
        }
        return result;
    }

    /**
     * 删除公告
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteAnnouncement(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = announcementService.deleteAnnouncement(id);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            log.error("删除公告异常", e);
        }
        return result;
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Announcement getDetail(@PathVariable Long id) {
        // 增加浏览次数
        announcementService.increaseViewCount(id);
        return announcementService.getAnnouncementById(id);
    }
}
