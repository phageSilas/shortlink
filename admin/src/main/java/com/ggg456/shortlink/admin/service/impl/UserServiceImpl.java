package com.ggg456.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.admin.common.convention.exception.ClientException;
import com.ggg456.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ggg456.shortlink.admin.dao.entity.UserDO;
import com.ggg456.shortlink.admin.dao.mapper.UserMapper;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;
import com.ggg456.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Override
    public UserRespDTO getUserInfo(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);//注意UserDO加上@Data
        if (userDO == null) { //若为空,BeanUtils.copyProperties会抛出异常报错
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO, userRespDTO);//注意导入的包是springframework.beans.BeanUtils;并且UserDO类上要加@Data
        return userRespDTO;

    }
}
