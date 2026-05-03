package com.ggg456.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ggg456.shortlink.admin.common.convention.result.Result;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import com.ggg456.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ggg456.shortlink.admin.dto.resp.UserActualRespDTO;
import com.ggg456.shortlink.admin.dto.resp.UserRespDTO;
import com.ggg456.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor // 自动注入,效果类似于 @Autowired
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     * 手机号脱敏
     *
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

        return Results.success(result);
    }

    /**
     * 获取用户手机号无脱敏信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserInfo(@PathVariable("username") String username) {
        // UserActualRespDTO result = userService.getUserInfo(username);
        return Results.success(BeanUtil.toBean(userService.getUserInfo(username), UserActualRespDTO.class));
        /*@JsonSerialize 仅仅是在“离开后端”时才生效
        @JsonSerialize（通常来自 Jackson 库）是一个序列化层的注解。它不会改变 Java 对象在内存中的真实值。
        当你调用 userService.getUserInfo(username) 时，从数据库查出来的是什么，这个对象在 JVM 内存里存的就是什么（也就是未脱敏的明文）。
        只有当这个对象被 Controller 层返回给前端，SpringBoot 调用 Jackson 库将 Java 对象转换成 JSON 字符串的那一瞬间，@JsonSerialize 指定的脱敏逻辑才会执行。

        BeanUtil.toBean 是基于内存的物理拷贝
        Hutool 的 BeanUtil.toBean(source, targetClass) 是一个内存级别的属性拷贝工具。
        它的底层原理是通过 Java 的反射机制（Reflection），调用原对象的 getter 方法获取值，然后调用目标对象的 setter 方法赋值。
        在拷贝的过程中，BeanUtil 根本不知道也不关心 @JsonSerialize 注解的存在。它拿到的直接是内存里未经任何处理的原始明文数据。*/
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 用户注册
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO reqParam) {
        userService.register(reqParam);
        return Results.success();
    }

}