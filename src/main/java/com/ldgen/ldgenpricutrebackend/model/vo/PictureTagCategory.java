package com.ldgen.ldgenpricutrebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @class: com.ldgen.ldgenpricutrebackend.model.vo.PictureTagCategory
 * @author: LBigGen
 * @create: 2025-03-08 21:17
 */

@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;


    /**
     * 分页列表
     */
    private List<String> categoryList;
}
