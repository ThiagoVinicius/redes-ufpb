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
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jailton
 */
public class TCP extends Transport implements IConstants {
    
    private static final Logger logger = LoggerFactory.getLogger(TCP.class);
    private int source_ip;
    private int local_port = 0;
    private PacketTCP lastReceivedPacket;

    ThreeWaysHandshake handshake = new ThreeWaysHandshake();
    //List container = new LinkedList<ToReceiveMessage>();
    ArrayBlockingQueue container = new ArrayBlockingQueue<ToReceiveMessage>(1);

    public TCP(Network downLayer, int source_ip) {
        super(downLayer);
        this.source_ip = source_ip;
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
        //container.add(0, receive);
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

        

        while( container.size() == 0 ) {
            //logger.debug("connect-");
            finalTime = System.currentTimeMillis();
            if( ( finalTime - initialTime ) > TIME_OUT_CONNECTION ) {
                throw new UnnableToConnectException();
            }
        }

        ToReceiveMessage message = (ToReceiveMessage) container.poll();

        String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

        PacketTCP packetReceived = new PacketTCP(dataReceived);


        PacketTCP thirdWay = handshake.thirdWay(dest_ip, remote_port, packetReceived);
        String dataHeaderThirdWay = thirdWay.toString();
        InterlayerData dataThirdWay = new InterlayerData(dataHeaderThirdWay.length());

        dataThirdWay.data[0] = parseStringToInt(dataHeaderThirdWay);

        bubbleDown(dataThirdWay, dest_ip);

        return new Connection(local_port, remote_port, source_ip, dest_ip, this);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public Connection listen(int local_port) {
        
        PacketTCP packetReceived;
        ToReceiveMessage message = null;

        logger.debug("entrei no listen");

        do {
            while( (container.size() == 0 ));

            logger.debug("listen-");
            
            message = (ToReceiveMessage) container.poll();

            String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

            //logger.debug(dataReceived);
            
            packetReceived = new PacketTCP(dataReceived);
        } while( !packetReceived.getPortLocal().
                equals(parseIntToString(local_port, NUM_BITS_MAX_PORT)) );

        PacketTCP secondWay = handshake.secondWay(local_port,
                parseStringToInt(packetReceived.getPortLocal()), packetReceived);
        String dataHeaderSecondWay = secondWay.toString();
        InterlayerData dataSecondWay = new InterlayerData(dataHeaderSecondWay.length());

        dataSecondWay.data[0] = parseStringToInt(dataHeaderSecondWay);

        bubbleDown(dataSecondWay, message.source_ip);
        
        logger.debug("Conexao estabelecida!");

        return new Connection(local_port, parseStringToInt(packetReceived.getPortRemote()),
                source_ip, message.source_ip, this);
    }

    @Override
    @SuppressWarnings("empty-statement")
    protected void close(Connection connection) {
        throw new UnsupportedOperationException("Not supported yet.");
//        PairOfTwoWaysHandshake handshakeClose = new PairOfTwoWaysHandshake();
//        ToReceiveMessage message = null;
//
//        // tera algo aqui no if, ate' que nao tenha decidido fica a estrutura
//        if(true) {
//
//            long initialTime = System.currentTimeMillis();
//            long finalTime = 0;
//
//            PacketTCP firstWay = handshakeClose.firstWay(lastReceivedPacket);
//
//            String dataHeaderFirstWay = firstWay.toString();
//            InterlayerData dataFirstWay = new InterlayerData(dataHeaderFirstWay.length());
//
//            dataFirstWay.data[0] = parseStringToInt(dataHeaderFirstWay);
//
//            bubbleDown(dataFirstWay, connection.destIp);
//
//            // ver o fato se ainda esta' recebendo dados
//            while( (container.size() == 0 ));
//
//            message = (ToReceiveMessage) container.poll();
//
//            String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);
//
//            PacketTCP fourthWay = handshakeClose.fourthWay(new PacketTCP(dataReceived));
//
//            String dataHeaderFourthWay = fourthWay.toString();
//            InterlayerData dataFourthWay = new InterlayerData(dataHeaderFourthWay.length());
//
//            dataFirstWay.data[0] = parseStringToInt(dataHeaderFourthWay);
//
//            bubbleDown(dataFourthWay, connection.destIp);
//
//            while( finalTime < MAXIMUM_SEGMENT_LIFETIME ) {
//                finalTime = System.currentTimeMillis() - initialTime;
//            }
//
//            connection.close();
//
//        }
//        else {
//        //    packet = handshakeClose.secondWay(packet);
//        //    packet = handshakeClose.thirdWay(packet);
//        }
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
