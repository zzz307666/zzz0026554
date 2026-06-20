package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.HealthKnowledge;

/**
 * 健康知识服务接口
 */
public interface HealthKnowledgeService {
    
    /**
     * 分页查询文章列表
     */
    IPage<HealthKnowledge> getArticlePage(Integer page, Integer size, String category);
    
    /**
     * 根据ID查询文章详情
     */
    HealthKnowledge getArticleById(Long id);
    
    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);
    
    /**
     * 获取热门文章（按浏览量）
     */
    java.util.List<HealthKnowledge> getHotArticles(Integer limit);
}
