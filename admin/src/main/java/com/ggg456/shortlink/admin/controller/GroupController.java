package com.ggg456.shortlink.admin.controller;

import com.ggg456.shortlink.admin.common.convention.result.Result;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ggg456.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ggg456.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新建分组
     * @param reqParam 新建分组参数
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO reqParam) {
        groupService.saveGroup(reqParam.getName());
         return Results.success();
    }

    /**
     * 获取分组列表
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改分组
     * @param reqParam 修改分组参数
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO reqParam) {
        groupService.updateGroup(reqParam);
        return Results.success();
    }

}
