package com.ggg456.shortlink.admin.dto.resp;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ggg456.shortlink.admin.common.database.BaseDO;
import lombok.Data;

@Data
public class ShortLinkGroupRespDTO extends BaseDO {
    @TableId(type = IdType.AUTO)
    /*
      ID
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;

}
