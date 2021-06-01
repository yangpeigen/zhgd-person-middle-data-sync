package cn.pinming.data.sync.config;

import cn.pinming.data.sync.entity.DataSourceConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.convert.NacosConfigConverter;
import com.alibaba.nacos.spring.util.parse.DefaultPropertiesConfigParse;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author yangpg
 * @Date 2021/5/19 10:48
 * @Version 1.0
 */
public class DataSourceConfigConverter implements NacosConfigConverter {

    public static final String DATA_SOURCE_TAG = "application.datasource.dynamic";

    @Override
    public boolean canConvert(Class targetType) {
        return false;
    }

    @Override
    public Object convert(String config) {
        Map<String, Map<String, String>> configs = new HashMap<>();
        Properties properties = new DefaultPropertiesConfigParse().parse(config);
        for (Object obj : properties.keySet()) {
            String key = String.valueOf(obj);
            if (key.startsWith(DATA_SOURCE_TAG)) {
                String sourceValue = properties.getProperty(key);
                String source = key.substring(DATA_SOURCE_TAG.length() + 1);
                String dataSourceKey = source.substring(0, source.indexOf("."));
                String sourceKey = source.substring(source.indexOf(".") + 1);
                Map<String, String> dataSourceMap = configs.get(dataSourceKey);
                if (null == dataSourceMap) {
                    dataSourceMap = new HashMap<>();
                }
                dataSourceMap.put(sourceKey, sourceValue);
                configs.put(dataSourceKey, dataSourceMap);
            }
        }
        Map<String, DataSourceConfig> dynamic = new HashMap<>();
        configs.forEach((sourceKey, sourceMap) -> {
            JSONObject jsonObject = new JSONObject();
            sourceMap.forEach((k, v) -> {
                jsonObject.put(k, v);
            });
            DataSourceConfig dataSourceConfig = JSON.toJavaObject(jsonObject, DataSourceConfig.class);
            dynamic.put(sourceKey, dataSourceConfig);
        });
        return dynamic;
    }
}
