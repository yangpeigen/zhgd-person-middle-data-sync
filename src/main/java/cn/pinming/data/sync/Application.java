package cn.pinming.data.sync;

import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Slf4j
@MapperScan("cn.pinming.data.sync.mapper")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableNacosConfig
@EnableDubbo(scanBasePackages = "cn.pinming.data")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

