package edu.gatech;

import edu.gatech.blocking.service.DnsSnifferService;
import edu.gatech.blocking.service.impl.DnsSnifferServiceImpl;
import org.onlab.packet.UDP;
import org.onosproject.cli.app.HandleCommand;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainController extends HandleCommand {

    private static final String config_blocking_name_file = "cs6250.txt";
    private final DnsSnifferService dnsSnifferService = new DnsSnifferServiceImpl();
    private Map<String, Integer> blockSource = new ConcurrentHashMap<>();

    protected MainController() {
        super("CS6250");
    }

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
        List<String> list = dnsSnifferService.sniffDnsPacket(udp);
        if (list != null) {
            boolean shouldBlock = false;
            for (String dnsQuestion : list) {
                //
                for (String toblock : blockSource.keySet()) {
		    Pattern p=Pattern.compile(toblock);
                    shouldBlock = shouldBlock || p.match(dnsQuestion);
                }
            }
            return shouldBlock;
        }
        return false;
    }

    @Override
    public void handleCommand(String[] strings) {
        if (strings.length == 2) {
            if (strings[1].equalsIgnoreCase("show")) {
                for (String s : blockSource.keySet()) {
                    System.out.println("---->" + s);
                }
            }
        }
        if (strings.length >= 3) {
            String op = strings[1];
            String host = strings[2];
            if (op.equalsIgnoreCase("add")) {
                blockSource.put(host, 0);
            } else if (op.equalsIgnoreCase("del")) {
                blockSource.remove(host);
            }
        }
    }
}
