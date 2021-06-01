package cn.pinming.data.sync.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangpg
 * @Date 2021/5/18 10:22
 * @Version 1.0
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    private static List<Object> dataSourceKeys = new ArrayList<>();

    public static List<Object> getDataSourceKeys() {
        return dataSourceKeys;
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }
}
