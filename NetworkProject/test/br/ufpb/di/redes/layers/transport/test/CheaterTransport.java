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
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class CheaterTransport extends Transport {

    public Set<Connection> connections;

    public CheaterTransport(Network downLayer, int minpacketSize, int maxpacketSize, int ip) {
        super(downLayer);
        this.ip = ip;
        connections = new HashSet<Connection>();
    }

    public int ip;

    private static final Logger logger = LoggerFactory.getLogger(CheaterTransport.class);

    @Override
    protected void processReceivedData(InterlayerData data, int fromIp) {
        //nothing to do on cheater mode.
    }

    @Override
    public int maxPacketSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int minPacketSize() {
        return 0;
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



}
