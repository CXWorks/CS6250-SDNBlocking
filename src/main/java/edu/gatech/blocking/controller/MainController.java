package edu.gatech.blocking.controller;

import edu.gatech.blocking.service.DnsSnifferService;
import edu.gatech.blocking.service.impl.DnsSnifferServiceImpl;
import io.netty.handler.codec.dns.DnsQuestion;
import org.onlab.packet.UDP;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainController {

    private static final String config_blocking_name_file = "cs6250.txt";
    private final DnsSnifferService dnsSnifferService = new DnsSnifferServiceImpl();
    private Map<String, Integer> blockSource = new ConcurrentHashMap<>();

    public void activate() {
        try {
            File file = new File(config_blocking_name_file);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
                blockSource.put(st, 0);
            br.close();
        } catch (IOException e) {

        }

    }

    public void deactivate() {
        try {
            File file = new File(config_blocking_name_file);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String s : blockSource.keySet()) {
                writer.write(s);
            }
            writer.close();
        } catch (IOException e) {

        }

    }

    public boolean shouldBlock(UDP udp) {
        List<DnsQuestion> list = dnsSnifferService.sniffDnsPacket(udp);
        if (list != null) {
            boolean shouldBlock = false;
            for (DnsQuestion dnsQuestion : list) {
                //
            }
        }
        return false;
    }
}
