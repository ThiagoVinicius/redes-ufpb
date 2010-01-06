/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.datalink.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class CheaterDataLink extends DataLink {

    public int minpacketSize;
    public int maxpacketSize;

    public int mac;

    public Map<Integer, CheaterDataLink> others;

    private static final Logger logger = LoggerFactory.getLogger(CheaterDataLink.class);

    public CheaterDataLink(Physical downLayer, int id, int minpacketSize, int maxpacketSize, Map<Integer, CheaterDataLink> others, int myMac) {
        super(downLayer, id);
        this.minpacketSize = minpacketSize;
        this.maxpacketSize = maxpacketSize;
        this.others = others;
        this.mac = myMac;
    }

    @Override
    protected void processReceivedData(InterlayerData data) {
        //nothing to do on cheater mode.
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_mac) {
        others.get(dest_mac).fakeReceived(data, mac);
    }

    @Override
    public int maxPacketSize() {
        return maxpacketSize;
    }

    @Override
    public int minPacketSize() {
        return minpacketSize;
    }

    public void fakeReceived (InterlayerData data, int souce_mac) {
        bubbleUp(data, souce_mac);
    }




}
