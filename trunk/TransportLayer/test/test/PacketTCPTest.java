/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import br.ufpb.di.redes.layers.transport.source.PacketTCP;
import junit.framework.TestCase;

/**
 *
 * @author Jailton
 */
public class PacketTCPTest extends TestCase {

    public void testClassPacketTCP() {
        PacketTCP p = new PacketTCP("100100100100100100100100111");
    }

}
