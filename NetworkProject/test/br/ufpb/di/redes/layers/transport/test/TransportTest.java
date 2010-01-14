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
import br.ufpb.di.redes.layers.transport.interfaces.UnnableToConnectException;
import br.ufpb.di.redes.layers.transport.source.TCP;
import java.util.concurrent.Semaphore;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public class TransportTest extends DefaultTest {

    public static int REPEAT = 10;

    private static Logger logger = LoggerFactory.getLogger(TransportTest.class);

    public void testListenConnect () throws Exception {

        Thread runner = new Thread () {
            @Override
            public void run() {
                listenConnectImpl();
            }
        };

        runner.start();

        runner.join(5000L); //se demorar muito, canso de esperar...

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

            new Thread () {
                public void run() {
                    try {
                        transport2.connect(machine1.network.getIp(), 0);
                    } catch (UnnableToConnectException ex) {
                        logger.error("Oops", ex);
                    }

                    waitfor.release();
                }
            }.start();

            new Thread() {
                public void run() {
                    transport1.listen(0);

                    waitfor.release();
                }
            }.start();

            try {
                waitfor.acquire(2);
            } catch (InterruptedException e) {
                
            }

        }
    }

    @Override
    public Transport getTransportLayer(int machineId, Network downLayer) {
        int ip = interNetwork.allMachines[machineId-1].network.getIp();
        return new TCP(downLayer, ip);
    }



}
