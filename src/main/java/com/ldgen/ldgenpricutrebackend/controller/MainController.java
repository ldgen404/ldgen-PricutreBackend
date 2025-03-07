package com.ldgen.ldgenpricutrebackend.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.ldgen.ldgenpricutrebackend.common.BaseResponse;
import com.ldgen.ldgenpricutrebackend.common.ResultUtils;
import org.apache.ibatis.io.ResolverUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: com.ldgen.ldgenpricutrebackend.controller.MainController
 * @author: LBigGen
 * @create: 2025-03-03 10:45
 */

@RestController
@RequestMapping("/")
public class MainController {

    /**
     *
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
