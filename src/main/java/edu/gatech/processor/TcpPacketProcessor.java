package edu.gatech.processor;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv4;
import org.onlab.packet.TCP;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;

public class TcpPacketProcessor implements PacketProcessor {
    @Override
    public void process(PacketContext context) {
        Ethernet eth = context.inPacket().parsed();
        System.out.println(eth.toString());
        IPacket iPacket = eth.getPayload();
        if (iPacket instanceof TCP) {
            TCP tcpPacket = (TCP) iPacket;
            System.out.println(tcpPacket.toString());
        }

    }

    private boolean isTCP(Ethernet eth) {
        return eth.getEtherType() == Ethernet.TYPE_IPV4 &&
                ((IPv4) eth.getPayload()).getProtocol() == IPv4.PROTOCOL_TCP;
    }
}
