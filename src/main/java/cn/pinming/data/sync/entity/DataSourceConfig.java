package cn.pinming.data.sync.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author yangpg
 * @Date 2021/5/18 15:06
 * @Version 1.0
 */
@Data
@EqualsAndHashCode
public class DataSourceConfig implements Serializable {
    /**
     * 数据库用户名
     */
    private String username;
    /**
     * 数据库密码
     */
    private String password;
    /**
     * 数据库连接url
     */
    private String url;
    /**
     * 数据库驱动
     */
    private String driverclassname;
}
