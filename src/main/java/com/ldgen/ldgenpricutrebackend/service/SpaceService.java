package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldgen.ldgenpricutrebackend.model.dto.space.SpaceAddRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.space.SpaceQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 15385
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-03-15 22:52:53
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间服务
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /***
     * 检验空间
     *
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);


    /**
     * 根据空间级别填充空间对象
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 获取空间包装类
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    void checkSpaceAuth(User loginUser, Space space);
}
