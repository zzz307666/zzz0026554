package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Announcement;
import com.hhjt.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 学生/教师 - 公告查看控制器
 */
@Slf4j
@Controller
@RequestMapping("/announcement")
public class AnnouncementViewController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 公告列表页面（学生/教师通用）
     */
    @GetMapping("/list")
    public String announcementList(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    Model model) {
        IPage<Announcement> announcementPage = announcementService.getActiveAnnouncements(page, size);
        model.addAttribute("page", announcementPage);
        return "announcement/list";
    }

    /**
     * 公告详情页面
     */
    @GetMapping("/detail/{id}")
    public String announcementDetail(@PathVariable Long id, Model model) {
        // 增加浏览次数
        announcementService.increaseViewCount(id);
        
        Announcement announcement = announcementService.getAnnouncementById(id);
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
    }
}
