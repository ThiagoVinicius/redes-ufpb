/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.datalink.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.datalink1.src.DataLink1;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.network.test.FakeNetwork;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.tests.DefaultTest;
import br.ufpb.di.redes.layers.tests.Ring;
import br.ufpb.di.redes.layers.tests.Util;
import java.util.Random;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class DataLinkTest extends DefaultTest {

    public static int REPEAT = 1000;

    private static Logger logger = LoggerFactory.getLogger(DataLinkTest.class);

    public void testSendReceiveImpl () throws Exception {

        Thread runner = new Thread () {
            public void run() {
                sendReceiveImpl();
            }
        };

        Thread.sleep(500L);

        runner.start();
        runner.join(5000L); //demorou, perdeu

        if (runner.isAlive())
            fail("TIMEOUT do teste alcancado.");

        runner.interrupt();

    }

    private void sendReceiveImpl () {

        int selector[];
        int ring;

        Ring theRing;

        DataLink dataLink1;
        DataLink dataLink2;

        FakeNetwork top1;
        FakeNetwork top2;

        int id1, id2;

        InterlayerData data;

        Random rand = new Random();
        
        int low, high, diff;
        int dataSize;

        for (int i = 0; i < REPEAT; ++i) {

            ring = rand.nextInt(interNetwork.networks.length);

            theRing = interNetwork.networks[ring];

            selector = Util.nextInts(theRing.machines.length);

            dataLink1 = theRing.getDataLink(selector[0]);
            dataLink2 = theRing.getDataLink(selector[1]);

            top1 = (FakeNetwork) theRing.machines[selector[0]].network;
            top2 = (FakeNetwork) theRing.machines[selector[1]].network;

            id1 = theRing.dataLinkIds[selector[0]];
            id2 = theRing.dataLinkIds[selector[1]];

            low = dataLink1.minPacketSize();
            high = dataLink2.maxPacketSize();
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

            top1.bubbleDown(data, dataLink2.getMac(), id2);
            try {
                assertEquals(data, top2.received.take().data);
            } catch (InterruptedException ex) {
                logger.error("", ex);
                TestCase.fail("Excecao encontrada. Parando.");
            }

        }

    }

    @Override
    public DataLink getDataLinkLayer(int machineId, int id, Physical downLayer, int mac) {
        return new DataLink1(downLayer, id, mac);
    }

    @Override
    public Network getNetworkLayer(int machineId, DataLink[] datalink, int[] ipArray) {
        return new FakeNetwork(datalink);
    }

}
