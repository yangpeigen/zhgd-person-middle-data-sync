package cn.pinming.data.sync.config;

import cn.pinming.data.sync.entity.DataSourceConfig;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author yangpg
 * @Date 2021/5/18 16:31
 * @Version 1.0
 */
@NacosConfigurationProperties(prefix = "application.datasource", dataId = "${nacos.listener.data-id}", groupId = "${nacos.listener.group-id}")
@Component
@Data
public class DataSourceProperties {
    private Map<String, DataSourceConfig> dynamic;
}
