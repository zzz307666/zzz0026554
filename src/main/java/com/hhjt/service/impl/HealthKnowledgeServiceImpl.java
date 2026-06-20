package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.HealthKnowledge;
import com.hhjt.mapper.HealthKnowledgeMapper;
import com.hhjt.service.HealthKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 健康知识服务实现类
 */
@Slf4j
@Service
public class HealthKnowledgeServiceImpl implements HealthKnowledgeService {

    @Autowired
    private HealthKnowledgeMapper knowledgeMapper;

    @Override
    public IPage<HealthKnowledge> getArticlePage(Integer page, Integer size, String category) {
        Page<HealthKnowledge> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<HealthKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthKnowledge::getStatus, 1); // 只查询已发布的文章
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(HealthKnowledge::getCategory, category);
        }
        
        wrapper.orderByDesc(HealthKnowledge::getCreateTime);
        
        return knowledgeMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public HealthKnowledge getArticleById(Long id) {
        return knowledgeMapper.selectById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        HealthKnowledge article = knowledgeMapper.selectById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            knowledgeMapper.updateById(article);
        }
    }

    @Override
    public List<HealthKnowledge> getHotArticles(Integer limit) {
        LambdaQueryWrapper<HealthKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthKnowledge::getStatus, 1)
               .orderByDesc(HealthKnowledge::getViewCount)
               .last("LIMIT " + limit);
        
        return knowledgeMapper.selectList(wrapper);
    }
}
