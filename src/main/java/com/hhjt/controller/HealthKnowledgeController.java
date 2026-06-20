package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.HealthKnowledge;
import com.hhjt.service.HealthKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端 - 健康知识控制器
 */
@Slf4j
@Controller
@RequestMapping("/student/health")
public class HealthKnowledgeController {

    @Autowired
    private HealthKnowledgeService knowledgeService;

    /**
     * 健康知识列表页面
     */
    @GetMapping("/knowledge")
    public String knowledgeList(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "12") Integer size,
                                 @RequestParam(required = false) String category,
                                 Model model) {
        IPage<HealthKnowledge> articlePage = knowledgeService.getArticlePage(page, size, category);
        List<HealthKnowledge> hotArticles = knowledgeService.getHotArticles(5);
        
        model.addAttribute("page", articlePage);
        model.addAttribute("hotArticles", hotArticles);
        model.addAttribute("currentCategory", category);
        
        return "student/health_knowledge";
    }

    /**
     * 文章详情页面
     */
    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id, Model model) {
        HealthKnowledge article = knowledgeService.getArticleById(id);
        
        if (article == null || article.getStatus() != 1) {
            return "redirect:/student/health/knowledge";
        }
        
        // 增加浏览次数
        knowledgeService.incrementViewCount(id);
        
        model.addAttribute("article", article);
        
        return "student/article_detail";
    }
}
