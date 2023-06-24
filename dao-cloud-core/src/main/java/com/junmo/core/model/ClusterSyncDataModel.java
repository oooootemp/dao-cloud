package com.junmo.core.model;

import lombok.Data;

/**
 * @author: sucf
 * @date: 2023/5/30 00:11
 * @description:
 */
@Data
public class ClusterSyncDataModel extends Model {
    /**
     * type
     * -2: remove the configuration from the configuration center
     * -1: indicates that the service is down from the cluster
     * 0: update the configuration from the configuration center
     * 1: indicates that the service is added to the cluster
     * 2: add the configuration from the configuration center
     */
    private byte type;
    private RegisterProviderModel registerProviderModel;
    private ProxyConfigModel proxyConfigModel;
    private String configJson;
}
