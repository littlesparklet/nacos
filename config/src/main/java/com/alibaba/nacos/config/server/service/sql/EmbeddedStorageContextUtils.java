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

package com.alibaba.nacos.config.server.service.sql;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.model.ConfigAllInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.event.ConfigDumpEvent;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporarily saves all insert, update, and delete statements under a transaction in the order in which they occur.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class EmbeddedStorageContextUtils {
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param configInfo {@link ConfigInfo}
     * @param srcIp      The IP of the operator
     * @param time       Operating time
     */
    public static void onModifyConfigInfo(ConfigInfo configInfo, String srcIp, Timestamp time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(false).namespaceId(configInfo.getTenant())
                    .dataId(configInfo.getDataId()).group(configInfo.getGroup()).isBeta(false)
                    .content(configInfo.getContent()).type(configInfo.getType()).handleIp(srcIp)
                    .lastModifiedTs(time.getTime()).encryptedDataKey(configInfo.getEncryptedDataKey()).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param configInfo {@link ConfigInfo}
     * @param betaIps    Receive client IP for grayscale configuration publishing
     * @param srcIp      The IP of the operator
     * @param time       Operating time
     */
    public static void onModifyConfigBetaInfo(ConfigInfo configInfo, String betaIps, String srcIp, Timestamp time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(false).namespaceId(configInfo.getTenant())
                    .dataId(configInfo.getDataId()).group(configInfo.getGroup()).isBeta(true).betaIps(betaIps)
                    .content(configInfo.getContent()).type(configInfo.getType()).handleIp(srcIp)
                    .lastModifiedTs(time.getTime()).encryptedDataKey(configInfo.getEncryptedDataKey()).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param configInfo {@link ConfigInfo}
     * @param tag        tag info
     * @param srcIp      The IP of the operator
     * @param time       Operating time
     */
    public static void onModifyConfigTagInfo(ConfigInfo configInfo, String tag, String srcIp, Timestamp time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(false).namespaceId(configInfo.getTenant())
                    .dataId(configInfo.getDataId()).group(configInfo.getGroup()).isBeta(false).tag(tag)
                    .content(configInfo.getContent()).type(configInfo.getType()).handleIp(srcIp)
                    .lastModifiedTs(time.getTime()).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param configInfo {@link ConfigInfo}
     * @param grayName gray name
     * @param grayRule gray rule
     * @param srcIp      The IP of the operator
     * @param time       Operating time
     */
    public static void onModifyConfigGrayInfo(ConfigInfo configInfo, String grayName, String grayRule, String srcIp, Timestamp time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(false).namespaceId(configInfo.getTenant())
                    .dataId(configInfo.getDataId()).group(configInfo.getGroup()).isBeta(false).grayName(grayName)
                    .grayRule(grayRule).content(configInfo.getContent()).type(configInfo.getType()).handleIp(srcIp)
                    .lastModifiedTs(time.getTime()).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param namespaceId namespaceId
     * @param group       groupName
     * @param dataId      dataId
     * @param srcIp       The IP of the operator
     * @param time        Operating time
     */
    public static void onDeleteConfigInfo(String namespaceId, String group, String dataId, String srcIp,
            Timestamp time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(true).namespaceId(namespaceId).group(group)
                    .dataId(dataId).isBeta(false).handleIp(srcIp).lastModifiedTs(time.getTime()).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param configInfos {@link ConfigAllInfo} list
     */
    public static void onBatchDeleteConfigInfo(List<ConfigAllInfo> configInfos) {
        if (!EnvUtil.getStandaloneMode()) {
            List<ConfigDumpEvent> events = new ArrayList<>();
            for (ConfigAllInfo configInfo : configInfos) {
                String namespaceId =
                        StringUtils.isBlank(configInfo.getTenant()) ? StringUtils.EMPTY : configInfo.getTenant();
                ConfigDumpEvent event = ConfigDumpEvent.builder().remove(true).namespaceId(namespaceId)
                        .group(configInfo.getGroup()).dataId(configInfo.getDataId()).isBeta(false).build();
                
                events.add(event);
            }
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFOS_CONFIG_DUMP_EVENT, JacksonUtils.toJson(events));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param namespaceId namespaceId
     * @param group       group
     * @param dataId      dataId
     * @param time        Operating time
     */
    public static void onDeleteConfigBetaInfo(String namespaceId, String group, String dataId, long time) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(true).namespaceId(namespaceId).dataId(dataId)
                    .group(group).isBeta(true).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param namespaceId namespaceId
     * @param group       group
     * @param dataId      dataId
     * @param tag         tag info
     * @param srcIp       The IP of the operator
     */
    public static void onDeleteConfigTagInfo(String namespaceId, String group, String dataId, String tag,
            String srcIp) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(true).namespaceId(namespaceId).group(group)
                    .dataId(dataId).isBeta(true).tag(tag).handleIp(srcIp).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
    
    /**
     * In the case of the in-cluster storage mode, the logic of horizontal notification is implemented asynchronously
     * via the raft state machine, along with the information.
     *
     * @param namespaceId namespaceId
     * @param group       group
     * @param dataId      dataId
     * @param grayName gray name
     * @param srcIp       The IP of the operator
     */
    public static void onDeleteConfigGrayInfo(String namespaceId, String group, String dataId, String grayName,
            String srcIp) {
        if (!EnvUtil.getStandaloneMode()) {
            ConfigDumpEvent event = ConfigDumpEvent.builder().remove(true).namespaceId(namespaceId).group(group)
                    .dataId(dataId).isBeta(true).grayName(grayName).handleIp(srcIp).build();
            
            Map<String, String> extendInfo = new HashMap<>(2);
            extendInfo.put(Constants.EXTEND_INFO_CONFIG_DUMP_EVENT, JacksonUtils.toJson(event));
            EmbeddedStorageContextHolder.putAllExtendInfo(extendInfo);
        }
    }
}
