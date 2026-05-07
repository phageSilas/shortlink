package com.ggg456.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链分组保存请求DTO
 * @author ggg456
 */
@Data
public class ShortLinkGroupUpdateReqDTO {
    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组标识
     */
    private String gid;
}
