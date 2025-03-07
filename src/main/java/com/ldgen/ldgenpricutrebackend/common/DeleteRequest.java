package com.ldgen.ldgenpricutrebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除
 * @author 15385
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    private static final long serialVersionUID = 1L;


}
