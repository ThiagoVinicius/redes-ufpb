/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.physical.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class CheaterPhysicalLayer extends Physical {

    public int minPackSize;
    public int maxPackSize;

    public CheaterPhysicalLayer forward;

    private static final Logger logger = LoggerFactory.getLogger(CheaterPhysicalLayer.class);

    public CheaterPhysicalLayer(int minPackSize, int maxPackSize, CheaterPhysicalLayer nextLayer) {
        this.minPackSize = minPackSize;
        this.maxPackSize = maxPackSize;
        this.forward = nextLayer;
    }

    @Override
    protected void processSentData(InterlayerData data) {
        forward.fakeReceived(data);
    }

    @Override
    public int maxPacketSize() {
        return maxPackSize;
    }

    @Override
    public int minPacketSize() {
        return minPackSize;
    }

    public void fakeReceived(InterlayerData data) {
        bubbleUp(data);
    }

}
