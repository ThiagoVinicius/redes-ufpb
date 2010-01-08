/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.interfaces.UnnableToConnectException;

/**
 *
 * @author Jailton
 */
public class TCP extends Transport {

    public TCP(Network downLayer) {
        super(downLayer);
    }

    @Override
    protected void processReceivedData(InterlayerData data, int source_ip) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Connection connect(int dest_ip, int remote_port) throws UnnableToConnectException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Connection listen(int local_port) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void close(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isActive(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void put(Connection con, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int minPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int maxPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
