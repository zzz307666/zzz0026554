package com.hhjt.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassDTO {
    private Long id;
    private String className;
    private String grade;
    private LocalDateTime createTime;
}