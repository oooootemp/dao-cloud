package com.junmo.boot.banlance.impl;

import com.junmo.boot.banlance.DaoLoadBalance;
import com.junmo.boot.bootstrap.unit.Client;

import java.util.Set;

/**
 * @author: sucf
 * @date: 2023/7/6 23:59
 * @description:
 */
public class HashLoadBalanceImpl extends DaoLoadBalance {
    @Override
    public Client route(Set<Client> availableClients) {

        return null;
    }
}
