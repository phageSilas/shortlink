package com.ggg456.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ggg456.shortlink.admin.common.convention.result.Result;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import com.ggg456.shortlink.admin.dto.resp.UserActualRespDTO;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;
import com.ggg456.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor // 自动注入,效果类似于 @Autowired
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     *手机号脱敏
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserInfo(@PathVariable("username") String username) {
        UserRespDTO result = userService.getUserInfo(username);
/*        if (result == null) {
            return new Result<UserRespDTO>().setCode(UserErrorCodeEnum.USER_NULL.code()).setMessage(UserErrorCodeEnum.USER_NULL.message());
        }else  {
            return new Result<UserRespDTO>().setData(userService.getUserInfo(username));
        }*/

/*        if (result == null) {
            return Results.failure(UserErrorCodeEnum.USER_NULL.code(), UserErrorCodeEnum.USER_NULL.message());
        }else  {
            return  Results.success(result);
        }*/

        return  Results.success(result);
    }

    /**
     * 获取用户手机号无脱敏信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserInfo(@PathVariable("username") String username) {
       // UserActualRespDTO result = userService.getUserInfo(username);
        return  Results.success(BeanUtil.toBean(userService.getUserInfo(username), UserActualRespDTO.class));
    }
}
