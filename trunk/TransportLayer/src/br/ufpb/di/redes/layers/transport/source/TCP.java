/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.interfaces.UnnableToConnectException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jailton
 */
public class TCP extends Transport implements IConstants {
    
    private static final Logger logger = LoggerFactory.getLogger(TCP.class);
    private int local_port = 0;

    ThreeWaysHandshake handshake = new ThreeWaysHandshake();
    List container = new LinkedList<ToReceiveMessage>();;

    public TCP(Network downLayer) {
        super(downLayer);
    }

     private static class ToReceiveMessage {
        public final InterlayerData data;
        public final int source_ip;
        public ToReceiveMessage(InterlayerData data, int source_ip) {
            this.data = data;
            this.source_ip = source_ip;
        }
    }

    @Override
    protected void processReceivedData(InterlayerData data, int source_ip) {
        ToReceiveMessage receive = new ToReceiveMessage(data, source_ip);
        container.add(receive);
    }

    @Override
    public Connection connect(int dest_ip, int remote_port) throws UnnableToConnectException {
        
        long initialTime, finalTime;
        PacketTCP firstWay = handshake.firstWay(local_port, remote_port);
        String dataHeaderFirstWay = firstWay.toString();
        InterlayerData dataFirstWay = new InterlayerData(dataHeaderFirstWay.length());

        dataFirstWay.data[0] = parseStringToInt(dataHeaderFirstWay);

        bubbleDown(dataFirstWay, dest_ip);
        
        initialTime = System.currentTimeMillis();

        while( container.get(0) == null ) {
            finalTime = System.currentTimeMillis();
            if( ( finalTime - initialTime ) > TIME_OUT_CONNECTION ) {
                throw new UnnableToConnectException();
            }
        }

        ToReceiveMessage message = (ToReceiveMessage) container.get(0);

        String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

        PacketTCP p = new PacketTCP(dataReceived);


        PacketTCP thirdWay = handshake.thirdWay(dest_ip, remote_port, p);
        String dataHeaderThirdWay = thirdWay.toString();
        InterlayerData dataThirdWay = new InterlayerData(dataHeaderThirdWay.length());

        dataThirdWay.data[0] = parseStringToInt(dataHeaderThirdWay);

        bubbleDown(dataThirdWay, dest_ip);

        logger.debug("Conexao estabelecida!");

        return new Connection(local_port, remote_port, dest_ip, dest_ip, this);
    }

    @Override
    public Connection listen(int local_port) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void close(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isActive(Connection con) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void put(Connection con, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int minPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int maxPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Dado uma cadeia de string de 0s e 1s, ira' retornar um inteiro.
     *
     * @param value Cadeia de bits.
     * @return Numero inteiro.
     */
     private int parseStringToInt( String value ) {

         int valueInt = 0;
         int base = 1;

         for(int c = value.length()-1; c >= 0; c--) {
             valueInt += (value.charAt(c)-'0') * base;
             base *= 2;
         }

         return valueInt;
     }

    /**
      * Transforma um numero inteiro para um string, especificando o numero de
      * bits dessa cadeia, caso o valor seja maior que o numero de bits, ira'
      * funcionar como um buffer circular.
      *
      * @param value Valor inteiro.
      * @param numBit Numero de bits.
      * @return Cadeira de bits em string
      */
     private String parseIntToString( int value, int numBit ) {

         /*
         if ( value > (Math.pow(2, numBit)-1) ) {
             throw new IllegalArgumentException("O valor estoura o numero de bits!");
         }
         */

         while ( value > (Math.pow(2, numBit)-1) ) {
             value = value - (int) (Math.pow(2, numBit));
         }

         String string = "";

         while(value > 0 ) {
            string = (value % 2) + string;
            value /= 2;
         }

         if(string.length() < numBit) {
             int addBit = numBit - string.length();

             for(int c = 0; c < addBit; c++) {
                string = "0" + string;
             }
         }

         return string;
     }

}
