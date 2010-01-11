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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class CheaterTransport extends Transport {

    public HashSet<Connection> connections;
    public HashSet<Integer> availablePorts;

    public volatile AtomicBoolean listening;
    public Semaphore listenSemaphore;

    public CheaterTransport(Network downLayer, int minpacketSize, int maxpacketSize) {
        super(downLayer);
        this.ip = downLayer.getIp();
        connections = new HashSet<Connection>();
        availablePorts = new HashSet<Integer>(Arrays.asList(new Integer [] {
            8, 9, 10, 11, 12, 13, 14, 15
        }));
        listenSemaphore = new Semaphore(0);
        listening = new AtomicBoolean(false);

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
        int local_port = -1;
        Iterator <Integer> iter = availablePorts.iterator();
        while (iter.hasNext()) {
            local_port = iter.next();
            iter.remove();
        }
        if (local_port == -1) {
            throw  new UnnableToConnectException();
        }

        Connection freshConnection = new Connection(local_port, remote_port, dest_ip, ip, this);

        connections.add(freshConnection);

        return freshConnection;
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
