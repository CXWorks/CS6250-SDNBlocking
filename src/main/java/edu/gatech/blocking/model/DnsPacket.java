package edu.gatech.blocking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DnsPacket {
    private boolean isQuery;
    private String domainName;
}
