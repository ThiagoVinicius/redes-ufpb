/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author Thiago
 */
public class InterlayerDataTest extends TestCase {

    public static int REPEAT = 100000;

    public void testToString () {

        InterlayerData sample;

        sample = new InterlayerData(7);
        sample.putInfo(0, 7, 10);
        assertEquals("0001010", sample.toString());

        sample = new InterlayerData(9);
        sample.putInfo(0, 9, -1);
        assertEquals("11111111 1", sample.toString());

        sample = new InterlayerData(64);
        sample.putInfo(0, 32, -1);
        sample.putInfo(32, 32, 0);
        assertEquals("11111111 11111111 11111111 11111111 00000000 00000000 00000000 00000000", sample.toString());

    }

    public void testEquals () {
        InterlayerData data1;
        InterlayerData data2;

        Random rand = new Random();

        for (int i = 0; i < REPEAT; ++i) {
            data1 = new InterlayerData(rand.nextInt(1024));
            data2 = new InterlayerData(data1.length);

            for (int j = 0; j < data1.length; ++j) {
                if (Math.random() < .5) {
                    data1.setBit(j);
                    data2.setBit(j);
                } else {
                    data1.clearBit(j);
                    data2.clearBit(j);
                }
            }

            assertEquals(data1, data2);

        }

    }

}
