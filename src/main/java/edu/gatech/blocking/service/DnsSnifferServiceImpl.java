package edu.gatech.blocking.service;

import edu.gatech.blocking.model.DnsPacket;
import org.onlab.packet.UDP;

public class DnsSnifferServiceImpl implements DnsSnifferService{
    @Override
    public DnsPacket sniffDnsPacket(UDP packet) {
        return new DnsPacket(true, "www.bilibili.com");
    }
}
