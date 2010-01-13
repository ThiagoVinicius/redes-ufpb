/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.test;

import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.tests.DefaultTest;
import br.ufpb.di.redes.layers.tests.Machine;
import br.ufpb.di.redes.layers.tests.Util;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.source.TCP;
import java.io.File;

/**
 *
 * @author Thiago
 */
public class TransportTest extends DefaultTest {

    public static int REPEAT = 10;

    public void testListenConnect () throws Exception {

        Machine machine1;
        Machine machine2;

        Transport transport1;
        Transport transport2;

        int selector[];

        

        for (int i = 0; i < REPEAT; ++i) {

            selector = Util.nextInts(interNetwork.allMachines.length);

            machine1 = interNetwork.allMachines[selector[0]];
            machine2 = interNetwork.allMachines[selector[1]];

            transport1 = machine1.transport;
            transport2 = machine2.transport;

            transport2.connect(machine1.network.getIp(), 0);
            transport1.listen(0);



        }

    }

    @Override
    public Transport getTransportLayer(int machineId, Network downLayer) {
        int ip = interNetwork.allMachines[machineId-1].network.getIp();
        return new TCP(downLayer, ip);
    }



}
