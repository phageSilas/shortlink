package com.ggg456.shortlink.admin.controller;

import com.ggg456.shortlink.admin.common.convention.result.Result;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.ggg456.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
