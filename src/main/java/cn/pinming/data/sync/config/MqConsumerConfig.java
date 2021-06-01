package cn.pinming.data.sync.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author yangpg
 * @Date 2021/5/31 15:29
 * @Version 1.0
 */
@Data
public class MqConsumerConfig implements Serializable {
    private String namesrvAddr;
    private String topics;
}
