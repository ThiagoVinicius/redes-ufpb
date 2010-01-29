/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jailton
 */
public class ConnectionState {
    
    public enum State {
        CONNECT_1,
        CONNECT_2,
        LISTEN,
        CONNECTED,
        SEND,
        RECV,
        WAIT_ACK,
        CLOSE_1,
        CLOSE_2,
        CLOSE_3,
        CLOSED
    }

    public ConnectionState(State curState, Connection con, TCP tcp) {
        this.curState = curState;
        this.con = con;
        toSend = new ArrayBlockingQueue<Byte>(IConstants.BUFFER_SIZE);
        this.tcp = tcp;
        seqNumber = "1";
        
        sendThread = new Thread(new SenderThread());
        sendThread.start();
        
    }
    
    public final TCP tcp;
    volatile public State curState;
    volatile public Connection con;
    volatile public ThreeWaysHandshake handshake;
    volatile public ThreeWaysHandshakeClose handshakeClose;
    volatile public PacketTCP lastPacket;
    volatile public ArrayBlockingQueue<Byte> toSend;
    volatile public PacketTCP waitingAck;
    volatile public InterlayerData waitingAckData;
    volatile public String seqNumber;
    
    public final Thread sendThread;
    
    volatile public ConnectionState next_hop_CONNECT_2;
    
    public class SenderThread implements Runnable {

        public void run() {
            while (!Thread.interrupted()) {
                //FIXME 6 e' bem especifico
                List<Byte> data = new ArrayList<Byte>(6);
                try {
                    data.add(toSend.take());
                } catch (InterruptedException ex) {
                    break;
                }
                
                long initialTime, finalTime, ellapsed;
                
                initialTime = System.currentTimeMillis();
                
                //FIXME 6 maldito
                while (data.size() < 6) {
                    
                    finalTime = System.currentTimeMillis();
                    ellapsed = finalTime - initialTime;
                    
                    Byte next = null;
                    try {
                        next = toSend.poll(Math.abs(IConstants.PACKET_WAIT_TIMEOUT - ellapsed), TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConnectionState.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if (next == null) {
                        break;
                    } else {
                        data.add(next);
                    }
                    
                    
                    
                }
                
                tcp.send(con, data);
                
            }
        }
        
    }

    @Override
    protected void finalize() throws Throwable {
        sendThread.interrupt();
        super.finalize();
    }
    
    
    
    
}
