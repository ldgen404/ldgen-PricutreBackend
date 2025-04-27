package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.SpaceUser;
import com.ldgen.ldgenpricutrebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 15385
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-04-27 14:18:23
 */
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    void validSpaceUser(SpaceUser spaceUser, boolean add);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
