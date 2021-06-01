package cn.pinming.data.sync.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.pinming.core.common.model.PageList;
import cn.pinming.data.sync.config.DynamicDataSourceContextHolder;
import cn.pinming.data.sync.entity.ProjectDeptRelation;
import cn.pinming.data.sync.entity.ProjectDeptRelationCheckField;
import cn.pinming.data.sync.mapper.ProjectDeptRelationMapper;
import cn.pinming.data.sync.service.ProjectDeptRelationService;
import cn.pinming.data.sync.util.FingerprintUtils;
import cn.pinming.v2.company.api.dto.department.DepartmentDto;
import cn.pinming.v2.company.api.service.DepartmentService;
import cn.pinming.v2.project.api.dto.ConstructionProjectDto;
import cn.pinming.v2.project.api.dto.ConstructionProjectQueryDto;
import cn.pinming.v2.project.api.service.ConstructionProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author yangpg
 * @Date 2021/5/18 19:59
 * @Version 1.0
 */
@Slf4j
@Service
public class ProjectDeptRelationServiceImpl implements ProjectDeptRelationService {

    @Autowired
    private ProjectDeptRelationMapper projectDeptRelationMapper;

    @Reference
    private ConstructionProjectService constructionProjectService;

    @Reference
    private DepartmentService departmentService;


    @Override
    public void syncFullAllDataSource() {
        List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
        dataSourceKeys.forEach(key -> {
            log.info("全量同步数据，同步所有数据源，数据源key[{}]", key);
            syncData(String.valueOf(key), false);
        });

    }

    @Override
    public void syncFullAssignDataSource(String key) {
        log.info("全量同步数据，同步指定数据源，数据源key[{}]", key);
        syncData(key, false);
    }

    @Override
    public void syncIncrAssignDataSource(String key) {
        log.info("增量同步数据，同步指定数据源，数据源key[{}]", key);
        syncData(key, true);
    }

    @Override
    public void syncUpdateAssignDataSource(String key, ProjectDeptRelation relation) {
        //切换数据源
        DynamicDataSourceContextHolder.setDataSourceKey(key);

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
            projectDeptRelationMapper.updateRelation(dbRelation);
        }
    }

    private void syncData(String key, Boolean flag) {
        //切换数据源
        DynamicDataSourceContextHolder.setDataSourceKey(key);

        ConstructionProjectQueryDto query = new ConstructionProjectQueryDto();
        query.setPageSize(100);
        PageList<ConstructionProjectDto> projects = constructionProjectService.findProjects(query);
        ArrayList<ConstructionProjectDto> dataList = projects.getDataList();
        syncDataDeal(dataList, flag);
        while (!projects.isLastPage()) {
            query.setPage(query.getPage() + 1);
            projects = constructionProjectService.findProjects(query);
            dataList = projects.getDataList();
            syncDataDeal(dataList, flag);
        }
    }

    private void syncDataDeal(List<ConstructionProjectDto> list, Boolean flag) {
        List<ProjectDeptRelation> relations = new ArrayList<>();
        list.forEach(e -> {
            ProjectDeptRelation relation = new ProjectDeptRelation();
            if (null != e.getDepartmentId()) {
                DepartmentDto department = departmentService.findDepartmentById(e.getDepartmentId());
                if (null != department) {
                    relation.setDepartmentCode(department.getCode());
                }
            }
            relation.setCompanyId(e.getCompanyId());
            relation.setProjectId(e.getProjectId());
            relation.setDepartmentId(e.getDepartmentId());
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
