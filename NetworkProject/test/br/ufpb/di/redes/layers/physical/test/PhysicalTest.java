/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.physical.test;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.datalink.test.FakeDataLink;
import br.ufpb.di.redes.layers.network.test.FakeNetwork;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.io.File;
import java.net.MalformedURLException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import physicalLayer.Principal.Fisica;

/**
 *
 * @author Thiago
 */
public class PhysicalTest extends TestCase {

    static {
        try {
            File f = new File("log4j.properties");
            System.setProperty("log4j.configuration", f.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
        }
    }

    public static final int REPEAT = 10;

    private static final Logger logger = LoggerFactory.getLogger(PhysicalTest.class);

    static {
        try {
            File f = new File("log4j.properties");
            System.setProperty("log4j.configuration", f.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
        }
    }

    public void testSendReceive () throws Exception {

        Physical toTest = new Fisica();
        FakeDataLink top = new FakeDataLink(toTest, 0);

        toTest.start();
        toTest.attach(top);
        top.attach(new FakeNetwork(new DataLink[] {top}));
        top.start();

        Thread.sleep(20L);

        for (int i = 0; i < REPEAT; ++i) {

        InterlayerData data = new InterlayerData(toTest.maxPacketSize());

        for (int j = 0; j < data.length; ++j) {
            if (Math.random() < .5) {
                data.setBit(j);
            } else {
                data.clearBit(j);
            }
        }



        top.bubbleDown(data);

        assertEquals(data, top.received.take());

        logger.info("enviado: {}", data);
        logger.info("recebido: {}", top.received.take());

        //top.received.take();

        //Thread.sleep(500L);

        }

    }





}
