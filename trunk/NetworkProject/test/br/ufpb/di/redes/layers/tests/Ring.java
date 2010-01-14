/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.tests;

import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;

/**
 *
 * @author Thiago
 */
public class Ring {


    public Machine machines[];
//    public Network networks[];
//    public DataLink datalinks[];
    public int dataLinkIds[];

//    public int machineToDataLink[];
//
    /**
     * Stand back, doing black magic here.
     *
     * @param machine
     * @return
     */
    public DataLink getDataLink (int machine) {
        return machines[machine].datalink[dataLinkIds[machine]];
    }

}
