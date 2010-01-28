/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.test;

import br.ufpb.di.redes.layers.datalink.datalink1.src.DataLink1;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.NetworkImpl;
import br.ufpb.di.redes.layers.network.impl2.NetworkImpl2;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.tests.DefaultTest;
import br.ufpb.di.redes.layers.tests.Machine;
import br.ufpb.di.redes.layers.tests.Util;
import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.interfaces.UnnableToConnectException;
import br.ufpb.di.redes.layers.transport.source.TCP;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class TransportTest extends DefaultTest {

    public static int REPEAT = 100;
    
    public Connection connection1;
    public Connection connection2;

    private static Logger logger = LoggerFactory.getLogger(TransportTest.class);

    public void testListenConnect () throws Exception {

        Thread runner = new Thread () {
            @Override
            public void run() {
                listenConnectImpl();
            }
        };

        runner.start();

        runner.join(60000L); //se demorar muito, canso de esperar...

        if (runner.isAlive())
            fail("Teste alcancou TIMEOUT - possivel deadlock detectado.");

        runner.interrupt();

    }

    private void listenConnectImpl() {

        int selector[];

        for (int i = 0; i < REPEAT; ++i) {

            selector = Util.nextInts(interNetwork.allMachines.length);

            final Machine machine1 = interNetwork.allMachines[selector[0]];
            final Machine machine2 = interNetwork.allMachines[selector[1]];

            final Transport transport1 = machine1.transport;
            final Transport transport2 = machine2.transport;

            final Semaphore waitfor = new Semaphore(0);
            final Semaphore listenready = new Semaphore(0);

            new Thread () {
                public void run() {
                    try {
                        listenready.acquireUninterruptibly();
                        Thread.yield();
                        connection2 = transport2.connect(machine1.network.getIp(), 0);
                    } catch (UnnableToConnectException ex) {
                        logger.error("Oops", ex);
                    }

                    waitfor.release();
                }
            }.start();
            
            new Thread() {
                public void run() {
                    listenready.release();
                    connection1 = transport1.listen(0);

                    waitfor.release();
                }
            }.start();

            try {
                waitfor.acquire(2);
            } catch (InterruptedException e) {
            }
            
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(TransportTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            DataOutputStream dos = new DataOutputStream(connection1.getOutputStream());
            DataInputStream dis = new DataInputStream(connection2.getInputStream());
            
            try {
                dos.writeUTF("Ola pessoal.");
                assertEquals("Ola pessoal.", dis.readUTF());
            } catch(IOException ex) {
                
            }
            
            assertTrue(connection1 != null);
            assertTrue(connection2 != null);
            
            connection1.close();

        }
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
