package com.ggg456.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ggg456.shortlink.admin.dao.entity.GroupDO;

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
}
