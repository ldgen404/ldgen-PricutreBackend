package com.ldgen.ldgenpricutrebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ldgen.ldgenpricutrebackend.annotation.AuthCheck;
import com.ldgen.ldgenpricutrebackend.api.imagesearch.ImageSearchApiFacade;
import com.ldgen.ldgenpricutrebackend.api.imagesearch.model.ImageSearchResult;
import com.ldgen.ldgenpricutrebackend.common.BaseResponse;
import com.ldgen.ldgenpricutrebackend.common.DeleteRequest;
import com.ldgen.ldgenpricutrebackend.common.ResultUtils;
import com.ldgen.ldgenpricutrebackend.constant.UserConstant;
import com.ldgen.ldgenpricutrebackend.exception.BusinessException;
import com.ldgen.ldgenpricutrebackend.exception.ErrorCode;
import com.ldgen.ldgenpricutrebackend.exception.ThrowUtils;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.*;
import com.ldgen.ldgenpricutrebackend.model.entity.Picture;
import com.ldgen.ldgenpricutrebackend.model.entity.Space;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.enums.PictureReviewStatusEnum;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureTagCategory;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureVO;
import com.ldgen.ldgenpricutrebackend.service.PictureService;
import com.ldgen.ldgenpricutrebackend.service.SpaceService;
import com.ldgen.ldgenpricutrebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 图片管理控制器
 * 提供图片上传、删除、更新、查询等功能
 *
 * @author LBigGen
 * @date 2025-03-07
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class PictureController {
    // 用户服务
    @Resource
    private UserService userService;
    // 图片服务
    @Resource
    private PictureService pictureService;

    // 空间服务
    @Resource
    private SpaceService spaceService;

    // Redis模板
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存（Caffeine实现）
     * 初始容量1024，最大10,000条，写入5分钟后过期
     */
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    // ------------------------ 核心API ------------------------

    /**
     * 上传图片（支持文件重新上传）
     *
     * @param multipartFile        上传的文件
     * @param pictureUploadRequest 上传请求参数
     * @param request              HTTP请求
     * @return 图片VO对象
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 仅管理员可访问
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        // 1. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 2. 调用服务层上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过URL上传图片
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求（包含图片ID）
     * @param request       HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        // 参数校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（管理员专用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(
            @RequestBody PictureUpdateRequest pictureUpdateRequest,
            HttpServletRequest request) {
        // 参数校验
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // DTO转Entity
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 标签列表转为JSON字符串
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        // 数据校验
        pictureService.validPicture(picture);

        // 检查图片是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 更新数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 补充审核信息
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);

        return ResultUtils.success(true);
    }

    // ------------------------ 查询API ------------------------

    /**
     * 根据ID获取图片原始数据（管理员专用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(
            long id,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 根据ID获取图片VO（包含处理后的数据）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(
            long id,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询图片
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        // 检查空间权限（如果是私有空间）
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }

        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    // ------------------------ 列表API ------------------------

    /**
     * 分页查询图片列表（管理员专用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(
            @RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(
                new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页查询图片VO列表（带权限检查）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(
            @RequestBody PictureQueryRequest pictureQueryRequest,
            HttpServletRequest request) {
        // 参数校验
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);  // 防爬虫

        // 设置默认审核状态为"已通过"
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 空间权限处理
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 公开图库：只查已过审数据
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间：检查权限
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }

        // 查询并返回结果
        Page<Picture> picturePage = pictureService.page(
                new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    // ------------------------ 其他功能 ------------------------

    /**
     * 获取图片标签和分类列表
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory category = new PictureTagCategory();
        category.setTagList(Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意"));
        category.setCategoryList(Arrays.asList("模板", "电商", "表情包", "素材", "海报"));
        return ResultUtils.success(category);
    }

    /**
     * 审核图片（管理员专用）
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(
            @RequestBody PictureReviewRequest pictureReviewRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量上传图片（管理员专用）
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

//    以图搜图

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        return ResultUtils.success(resultList);
    }

    /**
     *
     * @param searchPictureByColorRequest
     * @param request
     * @return
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }


}