/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.network.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.datalink.test.CheaterDataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class CheaterNetwork extends Network {

    public int minpacketSize;
    public int maxpacketSize;

    public int ip;

    public Map<Integer, CheaterNetwork> others;

    private static final Logger logger = LoggerFactory.getLogger(CheaterDataLink.class);

    public CheaterNetwork(DataLink downLayers[], int minpacketSize, int maxpacketSize, int ip, Map<Integer, CheaterNetwork> others) {
        super(downLayers);
        this.minpacketSize = minpacketSize;
        this.maxpacketSize = maxpacketSize;
        this.ip = ip;
        this.others = others;
    }

    @Override
    protected void processReceivedData(InterlayerData data, int fromIp, int dataLinkId) {
        //nothing to do on cheater mode.
    }

    @Override
    protected void processSentData(InterlayerData data, int destIp) {
        others.get(destIp).fakeReceived(data, ip);
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
