package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.api.aliyunAI.model.CreateOutPaintingTaskResponse;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.*;
import com.ldgen.ldgenpricutrebackend.model.entity.Picture;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图片服务接口，提供对图片的增删改查、审核、上传等功能
 * 适用于管理系统或存储系统的图片操作
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          图片输入源（可以是文件流、URL 等）
     * @param pictureUploadRequest 图片上传请求参数
     * @param loginUser            当前登录用户
     * @return 图片的封装对象
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest 图片批量上传请求参数
     * @param loginUser                   当前登录用户
     * @return 成功创建的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 填充审核参数（例如自动标记图片的审核状态）
     *
     * @param picture   需要填充参数的图片对象
     * @param loginUser 当前登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 获取查询条件对象，用于动态查询
     *
     * @param pictureQueryRequest 查询请求参数
     * @return 查询条件封装对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取单个图片的封装对象（用于前端展示）
     *
     * @param picture 图片实体
     * @param request HTTP 请求信息
     * @return 图片封装对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 获取图片的分页封装对象（用于前端分页展示）
     *
     * @param picturePage 分页查询结果
     * @param request     HTTP 请求信息
     * @return 分页封装的图片对象
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片是否合法，例如检查格式、大小、权限等
     *
     * @param picture 需要校验的图片对象
     */
    void validPicture(Picture picture);

    /**
     * 审核图片
     *
     * @param pictureReviewRequest 图片审核请求参数
     * @param loginUser            当前审核用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 异步清理旧图片（例如删除本地文件或云存储文件）
     *
     * @param oldPicture 需要清理的图片对象
     */
    @Async
    void cleanPictureFile(Picture oldPicture);

    /**
     * 已经使用Sa-Token注解鉴权方法废弃
     * 统一的权限校验，防止未授权操作
     *
     * @param loginUser 当前用户
     * @param picture   需要检查的图片对象
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 编辑图片信息（如修改图片名称、标签等）
     *
     * @param pictureEditRequest 图片编辑请求参数
     * @param loginUser          当前操作用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 删除图片
     *
     * @param pictureId 需要删除的图片 ID
     * @param loginUser 当前操作用户
     */
    void deletePicture(long pictureId, User loginUser);

    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
