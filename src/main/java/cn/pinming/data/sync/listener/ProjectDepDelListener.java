package cn.pinming.data.sync.listener;

import cn.pinming.data.sync.config.DynamicDataSourceContextHolder;
import cn.pinming.data.sync.entity.ProjectDeptRelation;
import cn.pinming.data.sync.service.ProjectDeptRelationService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author yangpg
 * @Date 2021/6/1 10:04
 * @Version 1.0
 */
@Component
@Slf4j
//@RocketMQMessageListener(topic = "${project.dept.change.topic}", selectorExpression = "projectDel", consumerGroup = "${project.dept.change.consumer.group}-consumer")
public class ProjectDepDelListener implements RocketMQListener<MessageExt> {

    @Autowired
    private ProjectDeptRelationService projectDeptRelationService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] messageBody = messageExt.getBody();
        String message = new String(messageBody);
        JSONObject body = JSONObject.parseObject(message);
        log.info("收到项目删除消息：" + JSONObject.toJSONString(body, SerializerFeature.PrettyFormat));
        if (null != body && null != body.getInteger("pjId")) {
            Integer projectId = body.getInteger("pjId");
            Integer companyId = body.getInteger("coId");
            ProjectDeptRelation relation = new ProjectDeptRelation();
            relation.setProjectId(projectId);
            relation.setCompanyId(companyId);
            relation.setStatus((byte) 3);

            List<Object> dataSourceKeys = DynamicDataSourceContextHolder.getDataSourceKeys();
            if (CollectionUtils.isEmpty(dataSourceKeys)) {
                log.error("项目部门信息删除，同步所有数据源，数据源不存在...");
                return;
            }

            dataSourceKeys.forEach(key -> {
                projectDeptRelationService.syncUpdateAssignDataSource(String.valueOf(key), relation);
            });
        }
    }
}
