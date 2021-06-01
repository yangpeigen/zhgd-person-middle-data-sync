package cn.pinming.data.sync.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.pinming.core.common.model.PageList;
import cn.pinming.core.common.model.Pagination;
import cn.pinming.data.sync.config.DynamicDataSourceContextHolder;
import cn.pinming.data.sync.entity.ProjectDeptRelation;
import cn.pinming.data.sync.entity.ProjectDeptRelationCheckField;
import cn.pinming.data.sync.mapper.ProjectDeptRelationMapper;
import cn.pinming.data.sync.util.FingerprintUtils;
import cn.pinming.v2.project.api.dto.SimpleConstructionProjectDto;
import cn.pinming.v2.project.api.service.DataSyncService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author yangpg
 * @Date 2021/5/20 14:09
 * @Version 1.0
 */
public class DataSyncRunner implements Runnable {

    private String key;
    private Boolean flag;

    @Autowired
    private ProjectDeptRelationMapper projectDeptRelationMapper;

    @Reference
    private DataSyncService dataSyncService;

    public DataSyncRunner(String key, Boolean flag) {
        this.key = key;
        this.flag = flag;
    }

    @Override
    public void run() {
        syncData(key, flag);
    }

    private void syncData(String key, Boolean flag) {
        //切换数据源
        DynamicDataSourceContextHolder.setDataSourceKey(key);

        Pagination pagination = new Pagination();
        pagination.setPageSize(100);
        PageList<SimpleConstructionProjectDto> projectList = dataSyncService.syncSimpleProjects(pagination);
        ArrayList<SimpleConstructionProjectDto> dataList = projectList.getDataList();
        syncDataDeal(dataList, flag);
        while (!projectList.isLastPage()) {
            pagination.setPage(pagination.getPage() + 1);
            projectList = dataSyncService.syncSimpleProjects(pagination);
            dataList = projectList.getDataList();
            syncDataDeal(dataList, flag);
        }
    }

    private void syncDataDeal(List<SimpleConstructionProjectDto> list, Boolean flag) {
        List<ProjectDeptRelation> relations = new ArrayList<>();
        list.forEach(e -> {
            ProjectDeptRelation relation = new ProjectDeptRelation();
            relation.setCompanyId(e.getCompanyId());
            relation.setProjectId(e.getProjectId());
            relation.setDepartmentId(e.getDepartmentId());
            relation.setDepartmentCode(e.getDepartmentCode());
            relation.setStatus(e.getStatus());
            relations.add(relation);
        });

        //全量同步
        if (!flag) {
            projectDeptRelationMapper.saveRelationBatch(relations);
            return;
        }

        //增量更新
        relations.forEach(relation -> {
            ProjectDeptRelation dbRelation = projectDeptRelationMapper.selectRelationByCoIdAndpjId(relation.getCompanyId(), relation.getProjectId());
            if (null == dbRelation) {
                projectDeptRelationMapper.saveRelation(relation);
                return;
            }
            Boolean ret = dataCheck(relation, dbRelation);
            if (!ret) {
                dbRelation.setDepartmentId(relation.getDepartmentId());
                dbRelation.setDepartmentCode(relation.getDepartmentCode());
                dbRelation.setStatus(relation.getStatus());
                projectDeptRelationMapper.updateRelation(relation);
            }
        });
    }

    /**
     * 信息比对
     *
     * @param relation
     * @param dbRelation
     * @return
     */
    private Boolean dataCheck(ProjectDeptRelation relation, ProjectDeptRelation dbRelation) {
        ProjectDeptRelationCheckField newRelation = new ProjectDeptRelationCheckField();
        ProjectDeptRelationCheckField oldelation = new ProjectDeptRelationCheckField();
        BeanUtil.copyProperties(relation, newRelation, CopyOptions.create().setIgnoreNullValue(true));
        BeanUtil.copyProperties(dbRelation, oldelation, CopyOptions.create().setIgnoreNullValue(true));
        String newFingerprint = FingerprintUtils.getFingerprint(newRelation, null, true);
        String oldFingerprint = FingerprintUtils.getFingerprint(oldelation, null, true);
        if (Objects.equals(newFingerprint, oldFingerprint)) {
            return true;
        }
        return false;
    }
}
