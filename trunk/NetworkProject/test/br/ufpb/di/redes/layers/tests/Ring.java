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

    /**
     * Stand back, doing black magic here.
     *
     * @param machine
     * @return
     */
    public int getIp (int machine) {
        return machines[machine].ips[dataLinkIds[machine]];
    }

    public int[] getExternalIps (int machine) {
        int result[] = new int[machines[machine].ips.length - 1];
        int internalIp = getIp(machine);

        int i = 0, j = 0;
        while (i < machines[machine].ips.length) {

            if (machines[machine].ips[i] != internalIp) {
                result[j] = machines[machine].ips[i];
                ++j;
            }

            ++i;
        }

        return result;
    }

}
