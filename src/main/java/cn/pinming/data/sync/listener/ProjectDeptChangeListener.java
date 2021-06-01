package cn.pinming.data.sync.listener;

import cn.pinming.data.sync.config.DynamicDataSourceContextHolder;
import cn.pinming.data.sync.entity.ProjectDeptRelation;
import cn.pinming.data.sync.enums.MsgTagsEnum;
import cn.pinming.data.sync.service.ProjectDeptRelationService;
import cn.pinming.v2.project.api.dto.SimpleConstructionProjectDto;
import cn.pinming.v2.project.api.service.ConstructionProjectService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Author yangpg
 * @Date 2021/5/31 15:42
 * @Version 1.0
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "${project.dept.change.topic}", selectorExpression = "*", consumerGroup = "${project.dept.change.consumer.group}-consumer")
public class ProjectDeptChangeListener implements RocketMQListener<MessageExt> {

    @Reference
    private ConstructionProjectService constructionProjectService;

    @Autowired
    private ProjectDeptRelationService projectDeptRelationService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
        List<String> tags = Arrays.asList(MsgTagsEnum.PROJECT_ADD.getTag(), MsgTagsEnum.PROJECT_UPDATE.getTag(), MsgTagsEnum.PROJECT_DEL.getTag());
        if (StringUtils.isEmpty(tag) || !tags.contains(tag)) {
            return;
        }

        log.info("收到项目消息{}", JSONObject.toJSONString(messageExt, SerializerFeature.PrettyFormat));

        byte[] messageBody = messageExt.getBody();
        String message = new String(messageBody);
        JSONObject body = JSONObject.parseObject(message);

        projectMessageDeal(body);
    }

    /**
     * 项目消息处理
     *
     * @param body
     */
    private void projectMessageDeal(JSONObject body) {
        log.info("收到项目变更消息：" + JSONObject.toJSONString(body, SerializerFeature.PrettyFormat));
        if (null != body && null != body.getInteger("pjId")) {
            Integer projectId = body.getInteger("pjId");
            SimpleConstructionProjectDto simpleProject = constructionProjectService.findSimpleProject(projectId);

            ProjectDeptRelation relation = new ProjectDeptRelation();
            log.info("项目信息：" + JSONObject.toJSONString(simpleProject, SerializerFeature.PrettyFormat));

            if (null == simpleProject) {
                Integer companyId = body.getInteger("coId");
                relation.setProjectId(projectId);
                relation.setCompanyId(companyId);
                relation.setStatus((byte) 3);
            } else {
                relation.setProjectId(projectId);
                Integer companyId = simpleProject.getCompanyId();
                relation.setCompanyId(companyId);
                Integer departmentId = simpleProject.getDepartmentId();
                relation.setDepartmentId(departmentId);
                String departmentCode = simpleProject.getDepartmentCode();
                relation.setDepartmentCode(departmentCode);
                Byte status = simpleProject.getStatus();
                relation.setStatus(status);
            }

            List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
            if (CollectionUtils.isEmpty(dataSourceKeys)) {
                log.error("项目部门信息变更，同步所有数据源，数据源不存在...");
                return;
            }

            dataSourceKeys.forEach(key -> {
                projectDeptRelationService.syncUpdateAssignDataSource(String.valueOf(key), relation);
            });
        }
    }
}
