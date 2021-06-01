package cn.pinming.data.sync.config;

/**
 * @Author yangpg
 * @Date 2021/5/18 09:47
 * @Version 1.0
 */

import cn.pinming.data.sync.entity.DataSourceConfig;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源配置
 */
@Slf4j
@Component
public class DataSourceConfiguration implements InitializingBean, BeanFactoryAware, EnvironmentAware {
    /**
     * 数据源配置
     */
    Map<String, DataSourceConfig> currentDatasource = new ConcurrentHashMap<>();

    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, DataSourceConfig> dynamic = dataSourceProperties.getDynamic();
        // 数据源的集合
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dynamic.forEach((k, v) -> {
            currentDatasource.put(k, v);
            try {
                DataSource dataSource = createDataSource(v);
                dataSourceMap.put(k, dataSource);
            } catch (Exception e) {
                log.error("创建数据源key[{}]数据源异常，异常信息:[]", k, e);
                return;
            }
        });

        DataSource dataSource = dynamicDataSource(dataSourceMap);
        defaultListableBeanFactory.registerSingleton("dataSource", dataSource);

        SqlSessionFactoryBean sqlSessionFactoryBean = sqlSessionFactoryBean(dataSource);
        defaultListableBeanFactory.registerSingleton("sqlSessionFactoryBean", sqlSessionFactoryBean);
    }

    /**
     * 创建数据源
     *
     * @param config
     * @return
     */
    public DataSource createDataSource(DataSourceConfig config) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(config.getDriverclassname());
        dataSourceBuilder.url(config.getUrl());
        dataSourceBuilder.username(config.getUsername());
        dataSourceBuilder.password(config.getPassword());
        return dataSourceBuilder.build();
    }

    /**
     * 创建动态数据源
     *
     * @param dataSourceMap
     * @return
     */
    public DataSource dynamicDataSource(Map<Object, Object> dataSourceMap) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        Object firstKey = dataSourceMap.entrySet().stream().findFirst().get().getKey();
        Object firstValue = dataSourceMap.get(firstKey);
        //设置默认数据源
        dynamicRoutingDataSource.setDefaultTargetDataSource(firstValue);
        //设置动态数据源集合
        dynamicRoutingDataSource.setTargetDataSources(dataSourceMap);
        dynamicRoutingDataSource.afterPropertiesSet();

        DynamicDataSourceContextHolder.setDataSourceKey(String.valueOf(firstKey));
        DynamicDataSourceContextHolder.getDataSourceKeys().addAll(dataSourceMap.keySet());

        return dynamicRoutingDataSource;
    }

    /**
     * 创建sqlSessionFactoryBean
     *
     * @param dataSource
     * @return
     */
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return sqlSessionFactoryBean;
    }

    /**
     * 监听nacos配置刷新
     *
     * @param dynamic
     */
    //@NacosConfigListener(dataId = "${nacos.listener.data-id}", groupId = "${nacos.listener.group-id}", converter = DataSourceConfigConverter.class)
    public void refreshConfig(Map<String, DataSourceConfig> dynamic) {
        //新增数据源
        Map<String, DataSourceConfig> addDatasource = new HashMap<>();
        //修改数据源
        Map<String, DataSourceConfig> modifyDatasource = new HashMap<>();
        //删除数据源
        Map<String, DataSourceConfig> delDatasource = new HashMap<>();
        dynamic.forEach((k, v) -> {
            DataSourceConfig dataSourceConfig = currentDatasource.get(k);
            //新增数据源
            if (null == dataSourceConfig) {
                currentDatasource.put(k, v);
                addDatasource.put(k, v);
            }
            //新增数据源，并且删除原有的配置
            if (null != dataSourceConfig && !dataSourceConfig.equals(v)) {
                currentDatasource.put(k, v);
                modifyDatasource.put(k, v);
            }
        });
        currentDatasource.forEach((k, v) -> {
            DataSourceConfig dataSourceConfig = dynamic.get(k);
            if (null == dataSourceConfig) {
                delDatasource.put(k, v);
                currentDatasource.remove(k);
            }
        });
        dataSourceChangeDeal(addDatasource, modifyDatasource, delDatasource);
    }

    /**
     * 数据源变更处理
     *
     * @param addDynamic
     * @param modifyDynamic
     * @param delDynamic
     */
    public void dataSourceChangeDeal(Map<String, DataSourceConfig> addDynamic, Map<String, DataSourceConfig> modifyDynamic, Map<String, DataSourceConfig> delDynamic) {
        DynamicRoutingDataSource dynamicDataSource = (DynamicRoutingDataSource) defaultListableBeanFactory.getBean("dataSource");
        Map<Object, Object> resolvedDataSources = dynamicDataSource.getTargetDataSources();
        addDynamic.forEach((k, v) -> {
            DataSource dataSource = createDataSource(v);
            resolvedDataSources.put(k, dataSource);
        });
        modifyDynamic.forEach((k, v) -> {
            DataSource dataSource = createDataSource(v);
            resolvedDataSources.put(k, dataSource);
        });
        delDynamic.forEach((k, v) -> {
            resolvedDataSources.remove(k);
        });
        dynamicDataSource.setTargetDataSources(resolvedDataSources);
        dynamicDataSource.afterPropertiesSet();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Map<String, DataSourceConfig> dynamic = dataSourceProperties.getDynamic();
        dynamic.forEach((k, v) -> {
            v.setDriverclassname(environment.resolvePlaceholders(v.getDriverclassname()));
            v.setUsername(environment.resolvePlaceholders(v.getUsername()));
            v.setPassword(environment.resolvePlaceholders(v.getPassword()));
            v.setUrl(environment.resolvePlaceholders(v.getUrl()));
        });
        dataSourceProperties.setDynamic(dynamic);
    }

}
