/*
 * 文 件 名:  ZookeeperConnectionManager.java
 * 编写人:  dengkun
 * 编 写 时 间:  2016年5月14日
 */
package com.dengkun.zookeeper.sequence;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author dengkun
 * @since 2016年5月14日
 */
public class ZookeeperSequence {

    private static CuratorFramework client;

    public static CuratorFramework getClient() throws Exception {
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        return client;
    }

    public static void close() {
        client.close();
    }

    public ZookeeperSequence() {

    }

    public static void main(String[] args) throws Exception {
        final ZookeeperSequenceService service = new ZookeeperSequenceService();
        // Executor exeutor = MoreExecutors.;
        // Futures.addCallback(new ListenableFuture(new Runnable() {
        //
        // public void run() {
        // service.getSequenceId("/test", 100, 200);
        // }
        // }
        // Futures.addCallback(future, callback, exeutor);

        final CountDownLatch latch = new CountDownLatch(10);
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        for (int i = 0; i < 10; i++) {
            Futures.addCallback(executorService.submit(new Callable<Long>() {

                public Long call() throws Exception {
                    return service.getSequenceId("/test", 100, 200);
                }
            }), new FutureCallback<Long>() {

                public void onSuccess(Long result) {
                    System.out.println(Thread.currentThread().getId() + ": " + result);
                    latch.countDown();
                }

                public void onFailure(Throwable t) {
                    System.out.println(Thread.currentThread().getId() + ": " + "error");
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
        close();
        // close();
    }
}
