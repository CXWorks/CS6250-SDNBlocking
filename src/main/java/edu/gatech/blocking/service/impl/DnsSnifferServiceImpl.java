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

            // use port 53 to check whether it is a DNS packet
            if(packet.getSourcePort() == 53 ||
                    packet.getDestinationPort() == 53) {
                // extract domain names from question and answer sections
                List<String> nameList = new ArrayList<>();
                nameList.addAll(decodeQuestions(byteBuf, qdCount));
                nameList.addAll(decodeAnswers(byteBuf, anCount));

                questionList = nameList;
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

    private List<String> decodeAnswers(ByteBuf buf, int count) throws Exception {
        List<String> answerList = new ArrayList<>();
        for (int i = count; i > 0; i --) {
            final String r = recordDecoder.decodeRecord(buf);
            if (r == null) {
                // Truncated response
                break;
            }

            answerList.add(r);
        }
        return answerList;
    }
}
