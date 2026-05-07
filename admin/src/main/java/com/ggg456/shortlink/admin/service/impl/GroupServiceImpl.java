package com.ggg456.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.admin.dao.entity.GroupDO;
import com.ggg456.shortlink.admin.dao.mapper.GroupMapper;
import com.ggg456.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ggg456.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ggg456.shortlink.admin.service.GroupService;
import com.ggg456.shortlink.admin.util.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 获取分组列表
     * @return 分组列表
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {

        Wrapper<GroupDO> queryWrapper =
                Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, "南波万")//暂时写一个固定值 //TODO: 获取当前用户名
                        .eq(GroupDO::getDelFlag, 0)
                        .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);//根据sortOrder和更新时间两个属性排序

        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);

        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }

    /**
     * 修改分组
     * @param reqParam 修改分组参数
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO reqParam) {

    }


    Boolean hasGid(String gid) {
        Wrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, null);//TODO: 获取当前用户名
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }

}
