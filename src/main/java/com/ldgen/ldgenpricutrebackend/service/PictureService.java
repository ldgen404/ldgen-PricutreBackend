package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.picture.PictureUploadRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.user.UserQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.Picture;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.PictureVO;
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
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


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
}
