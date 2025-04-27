package com.ldgen.ldgenpricutrebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.ldgen.ldgenpricutrebackend.exception.BusinessException;
import com.ldgen.ldgenpricutrebackend.exception.ErrorCode;
import com.ldgen.ldgenpricutrebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.ldgen.ldgenpricutrebackend.model.entity.Picture;
import com.ldgen.ldgenpricutrebackend.model.entity.Space;
import com.ldgen.ldgenpricutrebackend.model.entity.SpaceUser;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.enums.SpaceRoleEnum;
import com.ldgen.ldgenpricutrebackend.model.enums.SpaceTypeEnum;
import com.ldgen.ldgenpricutrebackend.service.PictureService;
import com.ldgen.ldgenpricutrebackend.service.SpaceService;
import com.ldgen.ldgenpricutrebackend.service.SpaceUserService;
import com.ldgen.ldgenpricutrebackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.ldgen.ldgenpricutrebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @class: com.ldgen.ldgenpricutrebackend.manager.auth.StpInterFaceImpl
 * @author: LBigGen
 * @create: 2025-04-27 18:33
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

    /**
     * @param loginId
     * @param loginType
     * @return
     */
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员权限，表示权限校验通过
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        // 如果所有字段都为空，表示查询公共图库，可以通过
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }
        // 获取 userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        Long userId = loginUser.getId();
        // 优先从上下文中获取 SpaceUser 对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }
            // 取出当前登录用户对应的 spaceUser
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 这里会导致管理员在私有空间没有权限，可以再查一次库处理
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }
        // 如果没有 spaceUserId，尝试通过 spaceId 或 pictureId 获取 Space 对象并处理
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 如果没有 spaceId，通过 pictureId 获取 Picture 对象和 Space 对象
            Long pictureId = authContext.getPictureId();
            // 图片 id 也没有，则默认通过权限校验
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可操作
            if (spaceId == null) {
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 不是自己的图片，仅可查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取 Space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // 私有空间，仅本人或管理员有权限
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询 SpaceUser 并获取角色和权限
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
    }


    /**
     * 不使用
     *
     * @param o
     * @param s
     * @return
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        return null;
    }

    /**
     * 从请求中获取上下文对象
     * 此方法用于解析当前请求，提取并转换请求参数或主体内容为SpaceUserAuthContext对象
     * 它还根据请求路径中的模块名称，将id字段的值分配给SpaceUserAuthContext对象的相应属性
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        // 获取请求参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 获取请求头
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        // 判断请求头是否为json格式
        SpaceUserAuthContext authRequest;
        if (ContentType.JSON.getValue().
                equals(contentType)) {
            // 如果是JSON格式，从请求体中获取内容并转换为SpaceUserAuthContext对象
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            // 如果不是JSON格式，从请求参数中获取内容并转换为SpaceUserAuthContext对象
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            //获取业请求路径的业务前缀
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            // 根据模块名称，将id字段的值分配给SpaceUserAuthContext对象的相应属性
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        // 返回转换后的SpaceUserAuthContext对象
        return authRequest;
    }
}