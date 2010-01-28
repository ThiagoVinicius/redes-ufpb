/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.application.test;

import br.ufpb.di.redes.layers.application.Cliente;
import br.ufpb.di.redes.layers.application.Servidor;
import br.ufpb.di.redes.layers.datalink.datalink1.src.DataLink1;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.NetworkImpl;
import br.ufpb.di.redes.layers.network.impl2.NetworkImpl2;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.physical.test.CheaterPhysicalLayer;
import br.ufpb.di.redes.layers.tests.DefaultTest;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.source.TCP;

/**
 *
 * @author Thiago
 */
public class ApplicationTest extends DefaultTest {



    public void testApplication () throws InterruptedException {

        final Servidor server = new Servidor(interNetwork.allMachines[5].transport);
        final Cliente client1 = new Cliente(interNetwork.allMachines[0].transport, 0,
                interNetwork.allMachines[5].network.getIp());

        final Cliente client2 = new Cliente(interNetwork.allMachines[3].transport, 1,
                interNetwork.allMachines[5].network.getIp());


        new Thread () {
            public void run () {
                server.go();
            }
        }.start();

        Thread.sleep(1000L);

        new Thread () {
            public void run () {
                client1.run();
            }
        }.start();

        

        Thread.sleep(10000L);

        new Thread () {
            public void run () {
                client2.run();
            }
        }.start();

        Thread.sleep(Long.MAX_VALUE);


    }

    @Override
    public DataLink getDataLinkLayer(int machineId, int id, Physical downLayer, int mac) {
        return new DataLink1(downLayer, id, mac);
    }

    @Override
    public Network getNetworkLayer(int machineId, DataLink[] datalink, int[] ipArray) {
        //return new NetworkImpl2(datalink, ipArray);
        return new NetworkImpl(datalink, ipArray, 80);
    }

    @Override
    public Transport getTransportLayer(int machineId, Network downLayer) {
        //int ip = interNetwork.allMachines[machineId-1].network.getIp();
        return new TCP(downLayer);
    }


}
