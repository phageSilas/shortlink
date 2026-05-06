package com.ggg456.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.admin.dao.entity.GroupDO;
import com.ggg456.shortlink.admin.dao.mapper.GroupMapper;
import com.ggg456.shortlink.admin.service.GroupService;
import com.ggg456.shortlink.admin.util.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {


    /**
     * 新建分组
     * @param groupName 分组名称
     */
    @Override
    public void saveGroup(String groupName) {

        String gid;
        do {
            gid = RandomCodeGenerator.generate();
        } while (!hasGid(gid));

        GroupDO group = GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .build();

        baseMapper.insert(group);
    }

    Boolean hasGid(String gid) {
        Wrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, null);//TODO: 获取当前用户名
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }

}
