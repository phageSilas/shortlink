package com.ggg456.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.ggg456.shortlink.admin.common.constant.RedisCacheConstant;
import com.ggg456.shortlink.admin.common.convention.exception.ClientException;
import com.ggg456.shortlink.admin.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.ggg456.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;

/**
 * 用户信息传输过滤器
 */

/**
 * 注意: 全局异常拦截器 @RestControllerAdvice 不能直接捕获 Filter 也就是该类throw 抛出的异常
 */
@RequiredArgsConstructor
@Slf4j
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 不需要他验证token的URL
     */
    private static final List<String> IGNORE_URL = List.of("/api/short-link/admin/v1/user/login", "/api/short-link/admin/v1/user/has-username");

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest; // 强转
        String requestURI = httpServletRequest.getRequestURI(); // 获取请求URI


        if (!IGNORE_URL.contains(requestURI)) {
            String method = httpServletRequest.getMethod(); // 因为同名URL有多个,所以需要获取请求方法类型, 比如GET POST
            if(!(Objects.equals(method, "POST")&&Objects.equals(requestURI, "/api/short-link/admin/v1/user"))){ // 非POST请求，并且是/api/short-link/admin/v1/user,也就是除了注册请求，其他请求都需要验证token
                String token = httpServletRequest.getHeader("token"); // 获取请求头中的token
                String username = httpServletRequest.getHeader("username"); // 获取请求头中的username
                if(!StrUtil.isAllNotBlank(username,token)){//都不为null或空对象或空白符""
                    returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL)))); // 返回JSON格式的错误信息
                    //throw new ClientException(USER_TOKEN_FAIL);
                    return;
                }
                Object userInfoJsonStr = null;
                try {
                    userInfoJsonStr = stringRedisTemplate.opsForHash().get(
                            RedisCacheConstant.USER_LOGIN_KEY +username, token);
                } catch (Exception e) {
                    //throw new ClientException(USER_TOKEN_FAIL);
                    returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL)))); // 返回JSON格式的错误信息
                    return;
                }


                    UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                    UserContext.setUser(userInfoDTO);
            }
        }



        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    /**
     * 返回JSON响应
     * 暂时仅用于测试,网关层会进一步完善
     * @param response
     * @param json
     * @throws Exception
     */
    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try{
            writer = response.getWriter();
            writer.print(json);
        }catch (IOException e) {
            log.error("返回JSON响应失败", e);
        }   finally {
            if (writer != null) {
                writer.close();
            }
        }
    }


}
