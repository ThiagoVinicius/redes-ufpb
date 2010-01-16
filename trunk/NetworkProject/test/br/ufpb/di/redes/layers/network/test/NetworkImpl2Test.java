/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.network.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.datalink1.src.DataLink1;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.NetworkImpl;
import br.ufpb.di.redes.layers.network.impl2.NetworkImpl2;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.tests.DefaultTest;
import br.ufpb.di.redes.layers.tests.Machine;
import br.ufpb.di.redes.layers.tests.Ring;
import br.ufpb.di.redes.layers.tests.Util;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.test.FakeTransport;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author Thiago
 */
public class NetworkImpl2Test extends DefaultTest {

    public static final int REPEAT = 10000;

    public volatile boolean failed = false;

    public void testSendReceive () throws Exception {

        Thread runner = new Thread () {
            public void run() {
                sendReceiveImpl();
            }
        };

        runner.start();
        runner.join(60000L); //demorou, perdeu
        runner.interrupt();
        runner.join();

        if (failed)
            fail("Timeout atingido!");

    }

    private void sendReceiveImpl() {

        int selector[];

        FakeTransport top1;
        FakeTransport top2;

        InterlayerData data;

        Random rand = new Random();

        int low, high, diff;
        int dataSize;

        for (int i = 0; i < REPEAT; ++i) {

            selector = Util.nextInts(interNetwork.allMachines.length);

            final Machine machine1 = interNetwork.allMachines[selector[0]];
            final Machine machine2 = interNetwork.allMachines[selector[1]];

            final Network network1 = machine1.network;
            final Network network2 = machine2.network;

            top1 = (FakeTransport) interNetwork.allMachines[selector[0]].transport;
            top2 = (FakeTransport) interNetwork.allMachines[selector[1]].transport;

            low = network1.minPacketSize();
            high = network1.maxPacketSize();
            diff = high - low + 1;

            dataSize = rand.nextInt(diff) + low;
            dataSize = (dataSize/low)*low;

            data = new InterlayerData(dataSize);

            for (int j = 0; j < data.length; ++j) {
                if (Math.random() < .5) {
                    data.setBit(j);
                } else {
                    data.clearBit(j);
                }
            }

            top1.bubbleDown(data, network2.getIp());

            try {
                assertEquals(data, top2.received.take().data);
            } catch (InterruptedException ex) {
                failed = true;
                break;
            }

        }
    }

//    @Override
//    public DataLink getDataLinkLayer(int machineId, int id, Physical downLayer, int mac) {
//        return new DataLink1(downLayer, id, mac);
//    }

    @Override
    public Network getNetworkLayer(int machineId, DataLink[] datalink, int[] ipArray) {
        return new NetworkImpl2(datalink, ipArray);
    }

    @Override
    public Transport getTransportLayer(int machineId, Network downLayer) {
        return new FakeTransport(downLayer);
    }



}
