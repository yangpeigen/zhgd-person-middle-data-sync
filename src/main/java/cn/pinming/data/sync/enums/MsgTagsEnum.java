package cn.pinming.data.sync.enums;

/**
 * @Author yangpg
 * @Date 2021/6/1 14:34
 * @Version 1.0
 */

/**
 * 消息标签枚举
 */
public enum MsgTagsEnum {
    PROJECT_ADD      ("projectAdd","项目添加"),
    PROJECT_UPDATE   ("projectUpdate","项目修改"),
    PROJECT_DEL      ("projectDel","项目删除");
    private String tag;
    private String msg;

    MsgTagsEnum(String tag, String msg) {
        this.tag = tag;
        this.msg = msg;
    }

    public String getTag(){
        return tag;
    }

    public String getMsg(){
        return msg;
    }
}
