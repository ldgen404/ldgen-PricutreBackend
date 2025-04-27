package com.ldgen.ldgenpricutrebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldgen.ldgenpricutrebackend.exception.BusinessException;
import com.ldgen.ldgenpricutrebackend.exception.ErrorCode;
import com.ldgen.ldgenpricutrebackend.exception.ThrowUtils;
import com.ldgen.ldgenpricutrebackend.mapper.SpaceMapper;
import com.ldgen.ldgenpricutrebackend.model.dto.space.SpaceAddRequest;
import com.ldgen.ldgenpricutrebackend.model.dto.space.SpaceQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.Space;
import com.ldgen.ldgenpricutrebackend.model.entity.SpaceUser;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.enums.SpaceLevelEnum;
import com.ldgen.ldgenpricutrebackend.model.enums.SpaceRoleEnum;
import com.ldgen.ldgenpricutrebackend.model.enums.SpaceTypeEnum;
import com.ldgen.ldgenpricutrebackend.model.vo.SpaceVO;
import com.ldgen.ldgenpricutrebackend.model.vo.UserVO;
import com.ldgen.ldgenpricutrebackend.service.SpaceService;
import com.ldgen.ldgenpricutrebackend.service.SpaceUserService;
import com.ldgen.ldgenpricutrebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 李大根
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2024-12-18 19:53:34
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;


    @Resource
    private TransactionTemplate transactionTemplate;

//    /**
//     * 分布式锁
//     */
//    @Resource
//    private RedissonClient redissonClient;

    /**
     * 本地锁
     */
    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 添加空间的方法
     *
     * @param spaceAddRequest 包含空间添加所需信息的请求对象
     * @param loginUser       当前登录的用户信息，用于权限校验或记录操作者
     * @return 返回添加空间操作的结果，通常可能是布尔值、结果状态码或具体的结果数据
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填充数据
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }

        // 针对用户进行加锁 -一个用户只能创建一个空间 -一个用户只能创建一个团队孔空间
//        String lock = String.valueOf(userId).intern();
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
        try {
            synchronized (lock) {
                Long newSpaceId = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, spaceAddRequest.getSpaceType())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
                    // 写入数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                    // 如果是团队空间，关联新增团队成员记录
                    if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                    }
                    // 返回新写入的数据 id
                    return space.getId();
                });
                // 返回结果是包装类，可以做一些处理
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            }
        } finally {
            // 防止内存泄漏
            lockMap.remove(userId);

        }
    }


    /**
     * 校验空间对象的合法性
     *
     * @param space 空间对象，包含空间的详细信息
     * @param add   表示是否是添加操作，true为添加，false为修改
     *              <p>
     *              该方法主要用于校验空间对象的合法性和合理性，包括空间名称、空间级别和空间类别的校验
     *              在添加操作时，确保这些信息不为空，在任何操作时，确保这些信息在允许的范围内
     */
    @Override
    public void validSpace(Space space, boolean add) {
        // 校验空间对象是否为空
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);

        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);

        // 要创建
        if (add) {
            // 在添加操作时，校验空间名称、空间级别和空间类别不能为空
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不能为空");
            }
        }

        // 修改数据时，如果要改空间级别，校验空间级别是否存在
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }

        // 校验空间名称长度不超过30
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }

        // 校验空间类别是否存在
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }
    }

    /**
     * @param space
     * @param request
     * @return
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 根据查询请求获取查询包装器
     * 用于构建Space对象的查询条件，根据SpaceQueryRequest中的参数进行条件筛选和排序
     *
     * @param spaceQueryRequest 查询请求对象，包含了一系列查询条件和排序信息
     * @return 返回一个QueryWrapper对象，用于执行后续的数据库查询操作
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        // 初始化查询包装器
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，则直接返回空的查询包装器
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        // 拼接查询条件
        // 根据spaceType是否存在及不为空，添加等于条件
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 根据id是否存在及不为空，添加等于条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        // 根据userId是否存在及不为空，添加等于条件
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        // 根据spaceName是否不为空，添加模糊查询条件
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        // 根据spaceLevel是否存在及不为空，添加等于条件
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);

        // 排序
        // 根据sortField是否不为空及sortOrder为"ascend"，进行升序排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        // 返回构建好的查询包装器
        return queryWrapper;
    }


    /**
     * 根据空间级别自动填充空间的限额
     * 当空间的最大大小或最大数量未设置时，根据空间级别自动填充这些值
     *
     * @param space 空间对象，其中包含空间级别和其他属性
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        // 检查是否找到了对应的空间级别枚举
        if (spaceLevelEnum != null) {
            // 获取该空间级别对应的最大大小
            long maxSize = spaceLevelEnum.getMaxSize();
            // 如果空间的最大大小未设置，则设置为该空间级别对应的最大大小
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            // 获取该空间级别对应的最大数量
            long maxCount = spaceLevelEnum.getMaxCount();
            // 如果空间的最大数量未设置，则设置为该空间级别对应的最大数量
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }


    /**
     * 空间权限校验
     *
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可访问
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}


