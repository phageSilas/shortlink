package com.ggg456.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ggg456.shortlink.admin.common.constant.RedisCacheConstant;
import com.ggg456.shortlink.admin.common.convention.exception.ClientException;
import com.ggg456.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ggg456.shortlink.admin.dao.entity.UserDO;
import com.ggg456.shortlink.admin.dao.mapper.UserMapper;
import com.ggg456.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;
import com.ggg456.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCacheBloomFilter;
    private final  RedissonClient redissonClient;

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
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

    /**
     * 校验用户名是否存在
     *
     * @param username 用户名
     * @return true:存在 false:不存在
     */
    public Boolean hasUsername(String username) {
        return !userRegisterCacheBloomFilter.contains(username);
    }

    /**
     * 用户注册
     *
     * @param reqParam 注册参数
     */
    public void register(UserRegisterReqDTO reqParam) {
        if (userRegisterCacheBloomFilter.contains(reqParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY + reqParam.getUsername());

        try {
            if (lock.tryLock()) {
/*              lock()：阻塞等待，直到获取锁（可能导致大量线程等待）
                tryLock()：立即返回，不等待（适合高并发场景）  */
                int insert = baseMapper.insert(BeanUtil.toBean(reqParam, UserDO.class));//返回值为受影响的行数,mybatis-plus生成的主键是依据雪花算法生成的
                if (insert != 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_FAIL);
                }
                userRegisterCacheBloomFilter.add(reqParam.getUsername());//添加用户名到布隆过滤器中
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }


    }
}
