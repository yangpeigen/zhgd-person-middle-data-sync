package cn.pinming.data.sync.service;

import cn.pinming.data.sync.entity.ProjectDeptRelation;

/**
 * @Author yangpg
 * @Date 2021/5/18 19:54
 * @Version 1.0
 */
public interface ProjectDeptRelationService {

    /**
     * 全量同步所有数据源的数据
     */
    void syncFullAllDataSource();

    /**
     * 全量同步指定数据源的数据
     *
     * @param key 数据源key
     */
    void syncFullAssignDataSource(String key);

    /**
     * 增量同步指定数据源的数据
     *
     * @param key 数据源key
     */
    void syncIncrAssignDataSource(String key);

    /**
     * 同步更新指定数据源的信息
     * @param key
     * @param relation
     */
    void syncUpdateAssignDataSource(String key, ProjectDeptRelation relation);
}
