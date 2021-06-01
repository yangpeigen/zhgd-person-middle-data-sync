package cn.pinming.data.sync.task;

import cn.pinming.data.sync.config.ThreadPoolProperties;
import cn.pinming.data.sync.util.ThreadPoolUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author yangpg
 * @Date 2021/5/20 13:57
 * @Version 1.0
 */
@Component
public class DataSyncTask implements InitializingBean {

    private ThreadPoolExecutor executor;

    @Override
    public void afterPropertiesSet() throws Exception {
        executor = ThreadPoolUtils.createExecutor(new ThreadPoolProperties(), new ThreadPoolExecutor.DiscardPolicy());
    }

    public void executeTask(DataSyncRunner runner) {
        executor.execute(runner);
    }

}
