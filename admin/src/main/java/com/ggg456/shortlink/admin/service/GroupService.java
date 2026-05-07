package com.ggg456.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ggg456.shortlink.admin.dao.entity.GroupDO;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ggg456.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 分组服务
 * @author ggg456
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 保存分组
     * @param groupName 分组名称
     * @return 保存结果
     */
    void saveGroup(String groupName);

    /**
     * 获取分组列表
     * @return 分组列表
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改分组
     * @param reqParam 修改分组参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO reqParam);

    /**
     * 删除分组
     * @param gid 分组id
     */
    void deleteGroup(String gid);

    /**
     * 排序分组
     */
    void sortGroupSort(List<ShortLinkGroupSortReqDTO> groupList);
}
