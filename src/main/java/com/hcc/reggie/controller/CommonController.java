package com.hcc.reggie.controller;

import com.hcc.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/** 处理文件的上传和下载 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    /** 文件上传 */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename(); // 原始文件名
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));  // 取出原始文件名后缀
        String fileName = UUID.randomUUID() + suffixName; // 使用 UUID 生成随机文件名

        // 判断配置文件中的目录是否存在，若不存在则创建
        File dir = new File(basePath);
        if (!dir.exists())
            dir.mkdirs();

        // 转存图片
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    /** 文件下载 */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try (FileInputStream fileInputStream = new FileInputStream(basePath + name);
             ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("image/jpeg"); // 设置响应文件的类型
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
