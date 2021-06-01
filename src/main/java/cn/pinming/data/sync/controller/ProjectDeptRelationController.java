package cn.pinming.data.sync.controller;

import cn.pinming.data.sync.config.DynamicDataSourceContextHolder;
import cn.pinming.data.sync.config.SpringUtilConfig;
import cn.pinming.data.sync.task.DataSyncRunner;
import cn.pinming.data.sync.task.DataSyncTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author yangpg
 * @Date 2021/5/19 09:14
 * @Version 1.0
 */
@RestController()
@RequestMapping("/sync")
@Slf4j
public class ProjectDeptRelationController {

    @Autowired
    private DataSyncTask syncTask;

    /**
     * 全量同步所有数据源
     */
    @GetMapping("/full/all")
    public void syncFullAllDataSource() {
        List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
        if (CollectionUtils.isEmpty(dataSourceKeys)) {
            log.error("全量同步数据，同步所有数据源，数据源不存在...");
            return;
        }

        dataSourceKeys.forEach(key -> {
            DataSyncRunner runner = new DataSyncRunner(String.valueOf(key), false);
            SpringUtilConfig.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(runner);
            syncTask.executeTask(runner);
        });
    }

    /**
     * 全量同步指定数据源
     *
     * @param key
     */
    @GetMapping("/full/single/{key}")
    public void syncFullAssignDataSource(@PathVariable("key") String key) {
        List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
        if (!dataSourceKeys.contains(key)) {
            log.error("全量同步数据，同步指定数据源，key[{}]对应的数据员不存在", key);
        }
        DataSyncRunner runner = new DataSyncRunner(String.valueOf(key), false);
        SpringUtilConfig.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(runner);
        syncTask.executeTask(runner);
    }

    /**
     * 增量同步指定数据源
     *
     * @param key
     */
    @GetMapping("/incr/single/{key}")
    public void syncIncrAssignDataSource(@PathVariable("key") String key) {
        List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
        if (!dataSourceKeys.contains(key)) {
            log.error("增量同步数据，同步指定数据源，key[{}]对应的数据员不存在", key);
        }
        DataSyncRunner runner = new DataSyncRunner(String.valueOf(key), false);
        SpringUtilConfig.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(runner);
        syncTask.executeTask(runner);
    }
}
