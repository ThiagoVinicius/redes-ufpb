/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.interfaces.UnnableToConnectException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Thiago
 */
public class FakeTransport extends Transport {


    public static class ReceivedMessage {
        public final InterlayerData data;
        public final int source_ip;
        public ReceivedMessage(InterlayerData data, int source_ip) {
            this.data = data;
            this.source_ip = source_ip;
        }
    }

    public FakeTransport(Network downLayer) {
        super(downLayer);
        this.received = new LinkedBlockingQueue<ReceivedMessage>();
    }

    public LinkedBlockingQueue<ReceivedMessage> received;

    @Override
    public void bubbleDown(InterlayerData data, int dest_ip) {
        super.bubbleDown(data, dest_ip);
    }

    @Override
    protected void close(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Connection connect(int dest_ip, int remote_port) throws UnnableToConnectException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isActive(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Connection listen(int local_port) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void processReceivedData(InterlayerData data, int source_ip) {
        received.offer(new ReceivedMessage(data, source_ip));
    }

    @Override
    protected void put(Connection con, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int maxPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int minPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
