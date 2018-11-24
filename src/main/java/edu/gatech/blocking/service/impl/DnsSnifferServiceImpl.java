package edu.gatech.blocking.service.impl;

import edu.gatech.blocking.service.DnsSnifferService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.onlab.packet.UDP;

import java.util.ArrayList;
import java.util.List;

public class DnsSnifferServiceImpl implements DnsSnifferService {
    private final DnsRecordDecoder recordDecoder;

    public DnsSnifferServiceImpl() {
        recordDecoder = new DnsRecordDecoder();
    }

    @Override
    public List<String> sniffDnsPacket(UDP packet) {
        // build the datagram packet
        byte[] bytes = packet.getPayload().serialize();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        List<String> questionList = null;

        try {
            final int id = byteBuf.readUnsignedShort();
            final int flag = byteBuf.readUnsignedShort();
            final int qdCount = byteBuf.readUnsignedShort();
            final int anCount = byteBuf.readUnsignedShort();
            final int nsCount = byteBuf.readUnsignedShort();
            final int arCount = byteBuf.readUnsignedShort();

            // this is supposed to be a dns packet
            // ref: https://stackoverflow.com/questions/7565300/identifying-dns-packets
            if(qdCount == 1 && (flag >> 15) == 0
                    && anCount == 0 && nsCount == 0
                    && isPossibleArCount(arCount)
                    && (flag & 0x000f) == 0) {
                questionList = decodeQuestions(byteBuf, qdCount);
            }
        } catch (Exception e) {
            System.out.println("Exception occurs when parsing the packet");
        } finally {
        }
        return questionList;
    }

    private boolean isPossibleArCount(int arCount) {
        return arCount == 0 || arCount == 1 || arCount == 2;
    }

    private List<String> decodeQuestions(ByteBuf buf, int questionCount) throws Exception {
        List<String> questionList = new ArrayList<>();
        for (int i = questionCount; i > 0; i--) {
            questionList.add(recordDecoder.decodeQuestion(buf));
        }
        return questionList;
    }
}
