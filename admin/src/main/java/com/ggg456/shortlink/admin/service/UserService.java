package com.ggg456.shortlink.admin.service;

import com.ggg456.shortlink.admin.dto.req.UserLoginReqDTO;
import com.ggg456.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ggg456.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.ggg456.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService {


    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserRespDTO getUserInfo(String username);

    /**
     * 校验用户名是否存在
     *
     * @param username 用户名
     * @return true:存在 false:不存在
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param reqParam 注册参数
     */
    void register(UserRegisterReqDTO reqParam);

    /**
     * 根据用户名修改用户信息
     *
     * @param reqParam 修改参数
     */
    void update(UserUpdateReqDTO reqParam);

    /**
     * 用户登录
     *
     * @param reqParam 登录参数
     * @return 登录结果
     */
    UserLoginRespDTO login(UserLoginReqDTO reqParam);

    /**
     * 用户登出
     *
     * @param reqParam 登出参数
     * @return 登出结果
     */
    Void logout(UserLoginReqDTO reqParam);
}
