package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康知识文章实体
 */
@Data
@TableName("health_knowledge")
public class HealthKnowledge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;           // 文章标题
    private String category;        // 分类（运动科普/健康饮食/损伤预防/专家问答）
    private String content;         // 文章内容（HTML格式）
    private String summary;         // 文章摘要
    private String coverImage;      // 封面图片
    private Integer viewCount;      // 浏览次数
    private Integer status;         // 状态（0:草稿 1:发布）
    private Long authorId;          // 作者ID
    private String authorName;      // 作者姓名
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
