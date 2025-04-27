package com.ldgen.ldgenpricutrebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询空间成员请求，可以不用分页：
 */
@Data
public class SpaceUserQueryRequest implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
