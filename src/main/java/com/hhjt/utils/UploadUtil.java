package com.hhjt.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class UploadUtil {

    @Value("${upload.avatar-path}")
    private String avatarPath;

    @Value("${upload.access-path}")
    private String accessPath;

    /**
     * 上传头像（修复路径拼接逻辑）
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (!(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
            throw new RuntimeException("仅支持JPG、PNG、GIF格式的图片");
        }

        // 3. 校验文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        // 4. 创建根目录（先确保根目录存在）
        File rootDir = new File(avatarPath);
        if (!rootDir.exists()) {
            rootDir.mkdirs(); // 递归创建根目录
        }

        // 5. 按日期分目录（可选，简化路径先测试）
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File saveDir = new File(rootDir, dateDir);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // 6. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        // 7. 保存文件
        File saveFile = new File(saveDir, fileName);
        file.transferTo(saveFile);
        System.out.println("头像保存路径：" + saveFile.getAbsolutePath()); // 打印路径便于调试

        // 8. 修复访问路径拼接（关键！确保路径格式正确）
        String finalAccessPath = accessPath + dateDir + "/" + fileName;
        System.out.println("头像访问路径：" + finalAccessPath); // 打印访问路径
        return finalAccessPath;
    }
}