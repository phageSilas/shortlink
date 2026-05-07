package com.ggg456.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链分组保存请求DTO
 * @author ggg456
 */
@Data
public class ShortLinkGroupSortReqDTO {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 排序
     */
    private Integer sortOrder;
}
