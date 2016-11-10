package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassNetworkManagementProxyV2;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;

public class ZWaveCommandClassNetworkManagementProxy extends ZWaveCommandClass {

    public ZWaveCommandClassNetworkManagementProxy(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        // versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
    }

    @ZWaveResponseHandler(id = CommandClassNetworkManagementProxyV2.NODE_INFO_CACHED_REPORT, name = "NODE_INFO_CACHED_REPORT")
    public void handleSwitchColorSet(ZWaveCommandClassPayload payload, int endpoint) {
        Map<String, Object> response = CommandClassNetworkManagementProxyV2
                .handleNodeInfoCachedReport(payload.getPayloadBuffer());

    }

    public ZWaveCommandClassTransactionPayload getNodeInfoCachedGet(Integer seqNo, Integer maxAge, Integer nodeId) {
        byte[] payload = CommandClassNetworkManagementProxyV2.getNodeInfoCachedGet(seqNo, maxAge, nodeId);
        if (payload == null) {
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), payload)
                .withExpectedResponseCommand(CommandClassNetworkManagementProxyV2.NODE_INFO_CACHED_REPORT)
                .withPriority(TransactionPriority.Get).build();
    }

}
