package com.ggg456.shortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ggg456.shortlink.admin.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户返回参数响应(已脱敏)
 */
@Data //BeanUtils.copyProperties()内部调用了setter,getter方法,所以需要@Data方法
public class UserRespDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class) // 手机号脱敏
    private String phone;

    /**
     * 邮箱
     */
    private String mail;


}
