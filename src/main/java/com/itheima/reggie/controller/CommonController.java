package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传与下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String bastPath;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    //必须要与前台name的名称保持一致
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件,需要转存到指定位置,否则本次请求完成后临时文件会删除
        log.info(file.toString());

        //获得原始文件名
        String originalFilename = file.getOriginalFilename();

        //得到文件后缀
        assert originalFilename != null;
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用uuid重新生成文件名,防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID() + substring;

        //创建一个目录对象
        File dir = new File(bastPath);
        //判断当前目录是否存在
        if (!dir.exists()) {
            //目录不存在,需要创建一个新的
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(bastPath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        //文件输入流,通过输入流读取文件内容
        //输出流,通过输出流将文件写回浏览器,在浏览器展示图片
        try (FileInputStream fileInputStream = new FileInputStream(bastPath + name); ServletOutputStream servletOutputStream = response.getOutputStream()) {

            //设置内容类型
            response.setContentType("image/jpeg");

            int len;
            byte[] bytes = new byte[1024];

            while ((len = fileInputStream.read(bytes)) != -1) {
                servletOutputStream.write(bytes, 0, len);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
