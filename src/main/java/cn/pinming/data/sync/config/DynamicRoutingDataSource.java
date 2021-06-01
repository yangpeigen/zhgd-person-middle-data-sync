package cn.pinming.data.sync.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * @Author yangpg
 * @Date 2021/5/18 10:16
 * @Version 1.0
 */

/**
 * 动态数据源切换
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private Map<Object, Object> targetDataSources;

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
        super.setTargetDataSources(targetDataSources);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        log.info("当前数据源[{}]", DynamicDataSourceContextHolder.getDataSourceKey());
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }

    public Map<Object, Object> getTargetDataSources() {
        return targetDataSources;
    }

}
