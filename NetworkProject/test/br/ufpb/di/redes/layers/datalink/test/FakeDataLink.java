/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.datalink.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import junit.framework.TestCase;

/**
 *
 * @author Thiago
 */
public class FakeDataLink extends DataLink {

    public FakeDataLink(Physical downLayer, int id) {
        super(downLayer, id);
        received = new LinkedBlockingQueue <InterlayerData> ();
    }

    public LinkedBlockingQueue<InterlayerData> received;

    @Override
    protected void processReceivedData(InterlayerData data) {
        received.offer(data);
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_mac) {
    }

    @Override
    public void bubbleDown (InterlayerData data) {
        super.bubbleDown(data);
    }

    @Override
    public int maxPacketSize() {
        return downLayer.maxPacketSize();
    }

    @Override
    public int minPacketSize() {
        return downLayer.minPacketSize();
    }

}
