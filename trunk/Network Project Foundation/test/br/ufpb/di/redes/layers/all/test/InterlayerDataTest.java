/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import junit.framework.TestCase;

/**
 *
 * @author Thiago
 */
public class InterlayerDataTest extends TestCase {

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

}
