/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.core.v2.client.manager.impl;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.core.remote.ClientConnectionEventListener;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.impl.ConnectionBasedClient;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.event.client.ClientEvent;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * The manager of {@code ConnectionBasedClient}.
 *
 * @author xiweng.yy
 */
@Component("connectionBasedClientManager")
public class ConnectionBasedClientManager extends ClientConnectionEventListener implements ClientManager {
    
    private final ConcurrentMap<String, ConnectionBasedClient> clients = new ConcurrentHashMap<>();
    
    public ConnectionBasedClientManager() {
        GlobalExecutor.scheduleExpiredClientCleaner(new ExpiredClientCleaner(this), 0,
                Constants.DEFAULT_HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void clientConnected(Connection connect) {
        if (!RemoteConstants.LABEL_MODULE_NAMING.equals(connect.getMetaInfo().getLabel(RemoteConstants.LABEL_MODULE))) {
            return;
        }
        clientConnected(new ConnectionBasedClient(connect.getConnectionId(), true));
    }
    
    @Override
    public boolean clientConnected(Client client) {
        Loggers.SRV_LOG.info("Client connection {} connect", client.getClientId());
        if (!clients.containsKey(client.getClientId())) {
            clients.put(client.getClientId(), (ConnectionBasedClient) client);
        }
        return true;
    }
    
    @Override
    public boolean syncClientConnected(String clientId) {
        return clientConnected(new ConnectionBasedClient(clientId, false));
    }
    
    @Override
    public void clientDisConnected(Connection connect) {
        clientDisconnected(connect.getConnectionId());
    }
    
    @Override
    public boolean clientDisconnected(String clientId) {
        Loggers.SRV_LOG.info("Client connection {} disconnect, remove instances and subscribers", clientId);
        ConnectionBasedClient client = clients.remove(clientId);
        if (null == client) {
            return true;
        }
        NotifyCenter.publishEvent(new ClientEvent.ClientDisconnectEvent(client));
        return true;
    }
    
    @Override
    public Client getClient(String clientId) {
        return clients.get(clientId);
    }
    
    @Override
    public Collection<String> allClientId() {
        return clients.keySet();
    }
    
    @Override
    public boolean isResponsibleClient(Client client) {
        return (client instanceof ConnectionBasedClient) && ((ConnectionBasedClient) client).isNative();
    }
    
    @Override
    public void verifyClient(String clientId) {
        ConnectionBasedClient client = clients.get(clientId);
        if (null != client) {
            client.setLastRenewTime();
        } else {
            // TODO get client from source
        }
    }
    
    private static class ExpiredClientCleaner implements Runnable {
        
        private final ConnectionBasedClientManager clientManager;
        
        public ExpiredClientCleaner(ConnectionBasedClientManager clientManager) {
            this.clientManager = clientManager;
        }
        
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            for (String each : clientManager.allClientId()) {
                ConnectionBasedClient client = (ConnectionBasedClient) clientManager.getClient(each);
                if (null != client && isExpireClient(currentTime, client)) {
                    clientManager.clientDisconnected(each);
                }
            }
        }
        
        private boolean isExpireClient(long currentTime, ConnectionBasedClient client) {
            return !client.isNative() && currentTime - client.getLastRenewTime() > Constants.DEFAULT_IP_DELETE_TIMEOUT;
        }
    }
}
