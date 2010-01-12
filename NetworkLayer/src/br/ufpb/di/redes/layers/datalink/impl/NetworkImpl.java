package br.ufpb.di.redes.layers.datalink.impl;


import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author J. Marques
 */
public class NetworkImpl extends Network {
    
    //Coloca o q vc precisar, como por exemplo o IP, no caso um arrays de IPs
    public NetworkImpl(DataLink[] downLayers) {
        super(downLayers);
    }

    @Override
    protected void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_ip) {
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

    @Override
    public int getIp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
