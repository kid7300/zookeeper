/*
 * 文 件 名:  CreateNode.java
 * 版    权:  Copyright © 2011-2014 深圳市房多多科技有限公司 All Rights Reserved
 * 编写人:  dengkun
 * 编 写 时 间:  2016年5月14日
 */
package com.dengkun.zookeeper.node;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author dengkun
 * @since 2016年5月14日
 */
public class CreateNode {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000,
                3));
        try {
            client.start();
            Stat stat = client.checkExists().forPath("/zkk");
            if (stat == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath("/zkk", "test".getBytes());
            }
            byte[] bytes = client.getData().forPath("/zkk");
            System.out.println(new String(bytes));
            System.out.println("over");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}
