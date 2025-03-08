package com.ldgen.ldgenpricutrebackend.controller;

import com.ldgen.ldgenpricutrebackend.annotation.AuthCheck;
import com.ldgen.ldgenpricutrebackend.common.BaseResponse;
import com.ldgen.ldgenpricutrebackend.common.ResultUtils;
import com.ldgen.ldgenpricutrebackend.constant.UserConstant;
import com.ldgen.ldgenpricutrebackend.manager.CosManager;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureUploadRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureVO;
import com.ldgen.ldgenpricutrebackend.service.PictureService;
import com.ldgen.ldgenpricutrebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @class: com.ldgen.ldgenpricutrebackend.controller.FileController
 * @author: LBigGen
 * @create: 2025-03-07 12:58
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class PictureController {
    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;
    @Resource
    private PictureService pictureService;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

}
