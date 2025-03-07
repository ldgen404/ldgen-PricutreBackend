package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ldgen.ldgenpricutrebackend.model.dto.user.UserQueryRequest;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.vo.LoginUserVO;
import com.ldgen.ldgenpricutrebackend.model.vo.UserVO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 15385
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-03-04 16:45:34
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密算法(获取加密后的算法)
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获得脱敏用户信息列表
     *
     * @param user
     * @return 脱敏后的用户列表
     */
    UserVO getUserVO(User user);


    /**
     * 获得脱敏用户信息列表
     *
     * @param userList
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserListVO(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
