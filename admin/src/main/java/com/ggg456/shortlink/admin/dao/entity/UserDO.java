package com.ggg456.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ggg456.shortlink.admin.common.database.BaseDO;
import lombok.Data;

@Data
@TableName("t_user") //用于显式指定实体类对应的数据库表名,覆盖默认规则
/*MyBatis-Plus 在没有指定表名时,会使用默认的命名策略:
实体类名 UserDO → 驼峰转下划线 → user_d_o
实体类名 UserInfo → 驼峰转下划线 → user_info
这就是你之前遇到错误的原因:UserDO 被自动转换成了 user_d_o,但数据库中实际表名是 t_user。*/
public class UserDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 注销时间戳
     */
    private Long deletionTime;


}
