package cn.pinming.data.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author yangpg
 * @Date 2021/5/18 19:35
 * @Version 1.0
 */
@Data
@TableName("f_project_dept_relation")
public class ProjectDeptRelation implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer companyId;
    private Integer projectId;
    private Integer departmentId;
    private String departmentCode;
    private Byte status;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;
}
