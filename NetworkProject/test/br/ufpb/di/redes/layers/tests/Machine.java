/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.tests;

import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.physical.test.CheaterPhysicalLayer;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;

/**
 *
 * @author Thiago
 */
public class Machine {

    public Transport transport;
    public Network network;
    public DataLink datalink[];
    public CheaterPhysicalLayer physical[];

    public void doAttachs() {
        for (int i = 0; i < physical.length; ++i) {
            physical[i].attach(datalink[i]);
        }

        for (int i = 0; i < datalink.length; ++i) {
            datalink[i].attach(network);
        }

        network.attach(transport);

    }

}
