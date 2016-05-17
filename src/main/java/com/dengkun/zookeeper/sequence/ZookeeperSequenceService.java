/*
 * 文 件 名:  ZookeeperSequenceService.java
 * 版    权:  Copyright © 2011-2014 深圳市房多多科技有限公司 All Rights Reserved
 * 编写人:  dengkun
 * 编 写 时 间:  2016年5月14日
 */
package com.dengkun.zookeeper.sequence;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

/**
 * @author dengkun
 * @since 2016年5月14日
 */
public class ZookeeperSequenceService {

    private StringBuffer str;

    public ZookeeperSequenceService() {
        str = new StringBuffer();
    }

    public long getSequenceId(String rootPath, int idcNum, int type) throws Exception {
        long sequenceId = -1;
        /* 避免在并发环境下出现线程安全，则添加排他锁 */
        synchronized (this) {
            /* IDC机房编码不能够超过3位,type不能够超过6位 */
            if (idcNum < 1000 && type < 1000000) {
                CuratorFramework client = ZookeeperSequence.getClient();
                if (client == null) {
                    System.out.println("client is null");
                    return sequenceId;
                } else {
                    /* 清空历史数据 */
                    if (null != str)
                        str.delete(0, str.length());
                    final String nodePath = "/" + String.valueOf(idcNum) + String.valueOf(type);
                    int version = getVersion(rootPath, client, nodePath);
                    if (-1 != version) {
                        /* 如果数据长度不足10位,高位补0 */
                        for (int i = 0; i < (10 - String.valueOf(version).length()); i++)
                            str.append("0");
                        str.append(String.valueOf(version));
                        str.insert(0, nodePath);
                        sequenceId = Long.parseLong(str.toString().substring(1));
                    }
                }
            }
        }
        return sequenceId;
    }

    /**
     * @param rootPath
     * @param client
     * @param nodePath
     * @return
     * @throws Exception
     */
    private int getVersion(String rootPath, CuratorFramework client, String nodePath) throws Exception {
        int version = -1;
        if (null != client && null != rootPath && null != nodePath) {
            final String PATH = rootPath + nodePath;
            try {
                /* 如果rootNode不存在,则创建 */
                if (null == client.checkExists().forPath(rootPath))
                    client.create().forPath(rootPath);
                /* 如果node不存在,则创建 */
                if (null == client.checkExists().forPath(PATH))
                    client.create().forPath(PATH);
                /* 改变某一个node的数据后,获取唯一版本号 */
                Stat stat = client.setData().forPath(PATH, "hello".getBytes());
                version = stat.getVersion();
            } catch (Exception e) {
                throw new Exception("zookeeper error," + e.getMessage());
            }
        }
        return version;
    }

}
