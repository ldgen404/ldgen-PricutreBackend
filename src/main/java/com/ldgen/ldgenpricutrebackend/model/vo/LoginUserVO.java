package com.ldgen.ldgenpricutrebackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已经登录用户后脱敏的视图
 *
 * @class: com.ldgen.ldgenpricutrebackend.model.vo.LoginUserVo
 * @author: LBigGen
 * @create: 2025-03-04 21:11
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;
    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;
}
