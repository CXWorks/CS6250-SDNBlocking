package edu.gatech.blocking.service;

import edu.gatech.blocking.model.DnsPacket;
import org.onlab.packet.UDP;

public interface DnsSnifferService {
    /**
     * try to parse one DNS packet (query & reply) from the given UDP packet
     * @param packet
     * @return if this is not a UDP packet, return null
     */
    public DnsPacket sniffDnsPacket(UDP packet);
}
