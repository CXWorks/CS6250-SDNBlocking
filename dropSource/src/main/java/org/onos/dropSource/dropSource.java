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
package org.onos.dropSource;

import com.google.common.collect.HashMultimap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onlab.packet.IpAddress;

import java.util.*;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.Timer;
// import java.util.TimerTask;

import static org.onosproject.net.flow.FlowRuleEvent.Type.RULE_REMOVED;
import static org.onosproject.net.flow.criteria.Criterion.Type.ETH_SRC;

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

    private final TrafficSelector intercept = DefaultTrafficSelector.builder()
            .matchEthType(Ethernet.TYPE_IPV4).matchIPProtocol(IPv4.PROTOCOL_ICMP)
            .build();

    // private final String[] blockSource = new String[100];
    // private final HashMultimap<DeviceId, String> blockSource = HashMultimap.create();
    Set<String> blockSource = new HashSet<String>();
    private static final int PRIORITY = 128;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.oneping",
                                                () -> log.info("Periscope down."));
        packetService.addProcessor(packetProcessor, PRIORITY);
        flowRuleService.addListener(flowListener);
        packetService.requestPackets(intercept, PacketPriority.CONTROL, appId,
                                     Optional.empty());
        blockSource.add("10.0.0.1");
        log.info("Drop Source");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        flowRuleService.removeFlowRulesById(appId);
        flowRuleService.removeListener(flowListener);
        log.info("Stopped Dropping");
    }

    private void processDrop(PacketContext context, Ethernet eth) {
        DeviceId deviceId = context.inPacket().receivedFrom().deviceId();

        IPv4 ipv4IpPayload = (IPv4) eth.getPayload(); 
        int ipSrcAddr = ipv4IpPayload.getSourceAddress(); 
        IpAddress ipSrcAddrCon = IpAddress.valueOf(ipSrcAddr); 
        log.info("Source: " + ipSrcAddrCon.toString());
        if(blockSource.contains(ipSrcAddrCon.toString())){
            log.info("Blocked Source contains " + ipSrcAddrCon.toString());
            log.info("Dropping...");
            context.block();
        }

        // blockSource.put(deviceId, ipSrcAddrCon.toString());

        /*if (blockSource.get(deviceId).contains(ipSrcAddrCon.toString()) {
            // Two pings detected; ban further pings and block packet-out
            log.warn(MSG_PINGED_TWICE, src, dst, deviceId);
            // banPings(deviceId, src, dst);
        }*/
        
        /*else {
            // One ping detected; track it for the next minute
            log.info(MSG_PINGED_ONCE, src, dst, deviceId);
            pings.put(deviceId, ping);
            timer.schedule(new PingPruner(deviceId, ping), TIMEOUT_SEC * 1000);
        }*/
    }
    /*
    // Installs a temporary drop rule for the ICMP pings between given srd/dst.
    private void banPings(DeviceId deviceId, MacAddress src, MacAddress dst) {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthSrc(src).matchEthDst(dst).build();
        TrafficTreatment drop = DefaultTrafficTreatment.builder()
                .drop().build();

        flowObjectiveService.forward(deviceId, DefaultForwardingObjective.builder()
                .fromApp(appId)
                .withSelector(selector)
                .withTreatment(drop)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .withPriority(DROP_PRIORITY)
                .makeTemporary(TIMEOUT_SEC)
                .add());
    }
    */

    // Indicates whether the specified packet corresponds to ICMP ping.
    private boolean isIcmpPing(Ethernet eth) {
        return eth.getEtherType() == Ethernet.TYPE_IPV4 &&
                ((IPv4) eth.getPayload()).getProtocol() == IPv4.PROTOCOL_ICMP;
    }

    // Intercepts packets
    private class dropPacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            Ethernet eth = context.inPacket().parsed();
            if (isIcmpPing(eth)) {
                processDrop(context, eth);
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
