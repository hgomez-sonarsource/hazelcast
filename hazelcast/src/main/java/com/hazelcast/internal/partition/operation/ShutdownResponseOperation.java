/*
 * Copyright (c) 2008, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.partition.operation;

import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.internal.partition.InternalPartitionService;
import com.hazelcast.internal.partition.MigrationCycleOperation;
import com.hazelcast.internal.partition.impl.InternalPartitionServiceImpl;
import com.hazelcast.internal.partition.impl.PartitionDataSerializerHook;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.NodeEngine;

public class ShutdownResponseOperation extends AbstractPartitionOperation implements MigrationCycleOperation {

    public ShutdownResponseOperation() {
    }

    @Override
    public void run() {
        InternalPartitionServiceImpl partitionService = getService();
        final ILogger logger = getLogger();
        final Address caller = getCallerAddress();

        final NodeEngine nodeEngine = getNodeEngine();
        final ClusterService clusterService = nodeEngine.getClusterService();
        final Address masterAddress = clusterService.getMasterAddress();

        if (nodeEngine.isRunning()) {
            logger.severe("Received a shutdown response from " + caller + ", but this node is not shutting down!");
            return;
        }

        boolean fromMaster = masterAddress.equals(caller);
        if (fromMaster) {
            if (logger.isFinestEnabled()) {
                logger.finest("Received shutdown response from " + caller);
            }
            partitionService.onShutdownResponse();
        } else {
            logger.warning("Received shutdown response from " + caller + " but known master is: " + masterAddress);
        }
    }

    @Override
    public boolean returnsResponse() {
        return false;
    }

    @Override
    public String getServiceName() {
        return InternalPartitionService.SERVICE_NAME;
    }

    @Override
    public int getId() {
        return PartitionDataSerializerHook.SHUTDOWN_RESPONSE;
    }
}
