package cn.pinming.data.sync.mapper;

import cn.pinming.data.sync.entity.ProjectDeptRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author yangpg
 * @Date 2021/5/18 19:34
 * @Version 1.0
 */
public interface ProjectDeptRelationMapper extends BaseMapper<ProjectDeptRelation> {

    @Insert("<script>\n" +
            "INSERT INTO f_project_dept_relation(id, company_id, project_id, department_id, department_code, " +
            "status, gmt_create, gmt_modify)\n" +
            "<foreach collection=\"relations\" item=\"relation\" open=\" values \" separator=\",\" >\n" +
            "    (\n" +
            "#{relation.id},#{relation.companyId},#{relation.projectId},#{relation.departmentId},#{relation.departmentCode}," +
            "#{relation.status},now(),#{relation.gmtModify}\n" +
            ")\n" +
            "</foreach>\n" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void saveRelationBatch(@Param("relations") List<ProjectDeptRelation> relations);

    @Select("select * from f_project_dept_relation where company_id = #{companyId} and project_id= #{projectId} limit 1")
    ProjectDeptRelation selectRelationByCoIdAndpjId(@Param("companyId") Integer companyId, @Param("projectId") Integer projectId);

    @Insert("<script>\n" +
            "INSERT INTO f_project_dept_relation(id, company_id, project_id, department_id, department_code, " +
            "status, gmt_create, gmt_modify)\n" +
            "value" +
            "    (\n" +
            "#{relation.id},#{relation.companyId},#{relation.projectId},#{relation.departmentId},#{relation.departmentCode}," +
            "#{relation.status},#{relation.gmtCreate},#{relation.gmtModify}\n" +
            ")\n" +
            "</script>")
    void saveRelation(@Param("relation") ProjectDeptRelation relation);

    @Update("update f_project_dept_relation set " +
            "company_id =  #{companyId}," +
            "project_id =  #{projectId}," +
            "department_id =  #{departmentId}," +
            "department_code =  #{departmentCode}," +
            "status =  #{status}," +
            "gmt_modify =  now()" +
            "where id = #{id}")
    void updateRelation(ProjectDeptRelation relation);
}
