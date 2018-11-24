package edu.gatech.blocking.service;

import io.netty.handler.codec.dns.DnsQuestion;
import org.onlab.packet.UDP;

import java.util.List;

public interface DnsSnifferService {
    /**
     * try to parse one DNS packet (query & reply) from the given UDP packet
     * @param packet
     * @return if this is not a UDP packet, return null
     */
    public List<DnsQuestion> sniffDnsPacket(UDP packet);
}
