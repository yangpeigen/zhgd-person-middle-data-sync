package cn.pinming.data.sync.util;

import cn.pinming.data.sync.config.ThreadPoolProperties;

import java.util.concurrent.*;

public class ThreadPoolUtils {
    public static ThreadPoolExecutor createExecutor(ThreadPoolProperties config, RejectedExecutionHandler handler) {
        BlockingQueue<Runnable> q = config.getQueueSize() < 0 ? new LinkedBlockingDeque<>() : new ArrayBlockingQueue<>(config.getQueueSize());
        ThreadPoolExecutor executor = new ThreadPoolExecutor(config.getCoreSize(), config.getMaxSize(), config.getKeepAlive(), TimeUnit.SECONDS, q, handler);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
