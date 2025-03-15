package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureReviewRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureUploadRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.Picture;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureVO;
import io.swagger.models.auth.In;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 15385
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-03-07 13:32:33
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类(单条)
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类(多条)
     *
     * @param picturePage 分页参数
     * @param request     请求
     * @return 返回图片包装类(多条)
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核实体类
     * @param loginUser            用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );


    /**
     * 清理图片
     *
     * @param oldPicture
     */
    void cleanPictureFile(Picture oldPicture);

}
