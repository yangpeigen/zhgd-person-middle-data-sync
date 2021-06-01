package cn.pinming.data.sync.entity;

import cn.pinming.data.sync.util.FingerprintField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author yangpg
 * @Date 2021/5/20 10:16
 * @Version 1.0
 */
@Data
public class ProjectDeptRelationCheckField implements Serializable {
    @FingerprintField
    private Integer departmentId;
    @FingerprintField
    private String departmentCode;
    @FingerprintField
    private Byte status;
}
