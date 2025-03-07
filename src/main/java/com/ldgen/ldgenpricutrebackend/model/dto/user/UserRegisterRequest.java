package com.ldgen.ldgenpricutrebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author 15385
 */
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 检查序列化后对象是否一致
     */
    private static final long serialVersionUID = -2373528947092624923L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
