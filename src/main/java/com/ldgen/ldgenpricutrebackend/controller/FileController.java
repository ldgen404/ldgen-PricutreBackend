package com.ldgen.ldgenpricutrebackend.controller;

import com.ldgen.ldgenpricutrebackend.annotation.AuthCheck;
import com.ldgen.ldgenpricutrebackend.common.BaseResponse;
import com.ldgen.ldgenpricutrebackend.common.ResultUtils;
import com.ldgen.ldgenpricutrebackend.constant.UserConstant;
import com.ldgen.ldgenpricutrebackend.exception.BusinessException;
import com.ldgen.ldgenpricutrebackend.exception.ErrorCode;
import com.ldgen.ldgenpricutrebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * @class: com.ldgen.ldgenpricutrebackend.controller.FileController
 * @author: LBigGen
 * @create: 2025-03-07 12:58
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);

        // 创建临时文件
        File file = null;
        try {
            Path tempFilePath = Files.createTempFile("upload_", ".tmp");
            file = tempFilePath.toFile();

            // 传输文件
            multipartFile.transferTo(file);

            // 上传到对象存储
            cosManager.putObject(filepath, file);

            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("File upload error, filepath = {}, error = {}", filepath, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.error("Temp file delete error, temp file path = {}", file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }



}
