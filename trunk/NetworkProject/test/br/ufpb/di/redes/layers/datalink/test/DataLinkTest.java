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
import br.ufpb.di.redes.layers.tests.Machine;
import br.ufpb.di.redes.layers.tests.Ring;
import br.ufpb.di.redes.layers.tests.Util;
import java.util.Random;

/**
 *
 * @author Thiago
 */
public class DataLinkTest extends DefaultTest {

    public static int REPEAT = 1000;

    public void testSendReceive () {

        int selector[];
        int ring;

        Ring theRing;

        DataLink dataLink1;
        DataLink dataLink2;

        InterlayerData data;

        Random rand = new Random();
        
        int low, high, diff;

        for (int i = 0; i < REPEAT; ++i) {

            ring = rand.nextInt(interNetwork.networks.length);

            theRing = interNetwork.networks[ring];

            selector = Util.nextInts(theRing.datalinks.length);

            dataLink1 = theRing.datalinks[selector[0]];
            dataLink2 = theRing.datalinks[selector[1]];

            low = dataLink1.minPacketSize();
            high = dataLink2.maxPacketSize();
            diff = high - low + 1;

            data = new InterlayerData(rand.nextInt(diff) + low);

            for (int j = 0; j < data.length; ++j) {
                if (Math.random() <= .5) {
                    data.setBit(i);
                } else {
                    data.clearBit(i);
                }
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
