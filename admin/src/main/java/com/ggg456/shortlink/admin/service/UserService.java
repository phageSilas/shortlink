package com.ggg456.shortlink.admin.service;

import com.ggg456.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService {


    UserRespDTO getUserInfo(String username);

    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO reqParam);
}
