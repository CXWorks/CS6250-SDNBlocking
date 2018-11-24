/*
 * Copyright 2015 Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.gatech;

import edu.gatech.blocking.controller.MainController;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.net.flow.FlowRuleEvent.Type.RULE_REMOVED;
import static org.onosproject.net.flow.criteria.Criterion.Type.ETH_SRC;

// import java.util.Objects;
// import java.util.Optional;
// import java.util.Timer;
// import java.util.TimerTask;

/**
Base on onePing
 */
@Component(immediate = true)
public class dropSource {

    private static Logger log = LoggerFactory.getLogger(dropSource.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ApplicationId appId;
    private final PacketProcessor packetProcessor = new dropPacketProcessor();
    private final FlowRuleListener flowListener = new InternalFlowListener();
    private final MainController mainController = new MainController();



    private final TrafficSelector intercept = DefaultTrafficSelector.builder()
            .matchEthType(Ethernet.TYPE_IPV4).matchIPProtocol(IPv4.PROTOCOL_ICMP)
            .build();

    private static final int PRIORITY = 128;

    @Activate
    public void activate() {
        appId = (ApplicationId) coreService.registerApplication("org.onosproject.oneping");
        packetService.addProcessor(packetProcessor, PRIORITY);
        flowRuleService.addListener(flowListener);
        packetService.requestPackets(intercept, PacketPriority.CONTROL, appId);
        mainController.activate();
        log.info("Drop Source");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        flowRuleService.removeFlowRulesById(appId);
        flowRuleService.removeListener(flowListener);
        mainController.deactivate();
        log.info("Stopped Dropping");
    }

    // Intercepts packets
    private class dropPacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            Ethernet eth = context.inPacket().parsed();
            IPacket iPacket = eth.getPayload();
            if (iPacket instanceof IPv4) {
                IPv4 iPv4 = (IPv4) iPacket;
                IPacket udpPacket = iPv4.getPayload();
                if (udpPacket instanceof UDP) {
                    UDP udp = (UDP) udpPacket;
                    if (mainController.shouldBlock(udp)) {
                        context.block();
                    }
                }
            }
        }
    }

    // Listens for our removed flows.
    private class InternalFlowListener implements FlowRuleListener {
        @Override
        public void event(FlowRuleEvent event) {
            FlowRule flowRule = event.subject();
            if (event.type() == RULE_REMOVED && flowRule.appId() == appId.id()) {
                Criterion criterion = flowRule.selector().getCriterion(ETH_SRC);
                MacAddress src = ((EthCriterion) criterion).mac();
                MacAddress dst = ((EthCriterion) criterion).mac();
                // log.warn(MSG_PING_REENABLED, src, dst, flowRule.deviceId());
                log.warn("What is this? Soure is " + src + " Dst is " + dst);
            }
        }
    }
}
