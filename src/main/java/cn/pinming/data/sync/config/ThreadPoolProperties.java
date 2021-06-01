package cn.pinming.data.sync.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author yangpg
 * @Date 2021/5/20 14:03
 * @Version 1.0
 */
@Getter
@Setter
public class ThreadPoolProperties implements Serializable {

    /**
     * 核心线程数量
     **/
    private int coreSize = 10;
    /**
     * 最大线程数量
     **/
    private int maxSize = 100;
    /**
     * 空闲线程最大空闲时间(秒)
     **/
    private int keepAlive = 20;
    /**
     * 任务等待队列最大值
     **/
    private int queueSize = 100;
}
