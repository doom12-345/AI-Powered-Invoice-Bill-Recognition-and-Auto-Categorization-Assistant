package com.smartinvoice.invoice.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    // 上传图片保存的文件夹（在 application.yml 里配置，默认 ./uploads）
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public StoredFile store(MultipartFile file) {
        // 校验1：文件不能为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 校验2：只允许常见图片格式
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.lastIndexOf(".") >= 0) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IllegalArgumentException("只支持 jpg / jpeg / png / webp 格式的图片");
        }

        // 生成唯一 fileId
        String fileId = UUID.randomUUID().toString().replace("-", "");

        // 确保目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件到本地
        File dest = new File(dir, fileId + ext);
        try {
            file.transferTo(dest.toPath());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        return new StoredFile(fileId, dest.getAbsolutePath(), originalName);
    }
}
