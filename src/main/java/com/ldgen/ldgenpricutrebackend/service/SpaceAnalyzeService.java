package com.ldgen.ldgenpricutrebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ldgen.ldgenpricutrebackend.model.dto.space.analyze.*;
import com.ldgen.ldgenpricutrebackend.model.entity.Space;
import com.ldgen.ldgenpricutrebackend.model.entity.User;
import com.ldgen.ldgenpricutrebackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * 权限校验
 *
 * @author 15385
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-03-15 22:52:53
 */
public interface SpaceAnalyzeService extends IService<Space> {


    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
