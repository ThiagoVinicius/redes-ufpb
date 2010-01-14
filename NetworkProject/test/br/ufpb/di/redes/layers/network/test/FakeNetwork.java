/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.network.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Thiago
 */
public class FakeNetwork extends Network {

    public static class ReceivedData {
        public final InterlayerData data;
        public final int source_mac;
        public final int datalink_id;

        public ReceivedData(InterlayerData data, int source_mac, int datalink_id) {
            this.data = data;
            this.source_mac = source_mac;
            this.datalink_id = datalink_id;
        }

    }

    public FakeNetwork(DataLink[] downLayers) {
        super(downLayers);
        this.received = new LinkedBlockingQueue<ReceivedData>();
    }

    public LinkedBlockingQueue<ReceivedData> received;

    @Override
    protected void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id) {
        received.offer(new ReceivedData(data, soruce_mac, datalink_id));
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_ip) {
    }

    @Override
    public void bubbleDown(InterlayerData data, int dest_mac, int datalink_id) {
        super.bubbleDown(data, dest_mac, datalink_id);
    }



    @Override
    public int maxPacketSize() {
        return downLayer[0].maxPacketSize();
    }

    @Override
    public int minPacketSize() {
        return downLayer[0].minPacketSize();
    }

    public int getIp() {
        return 0;
    }


}
