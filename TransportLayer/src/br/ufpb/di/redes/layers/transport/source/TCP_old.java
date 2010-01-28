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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jailton
 */
public class TCP_old extends Transport implements IConstants {
    
    private static final Logger logger = LoggerFactory.getLogger(TCP_old.class);

    private int source_ip;
    private int local_port;

    private PacketTCP lastReceivedPacket;
    
    private boolean closeReply = false;
    private boolean timeoutPut = false;

    private Connection connection;
    private boolean closeConnection;
    //private static int connectID;

    private ThreeWaysHandshake handshake = new ThreeWaysHandshake();
    private ArrayBlockingQueue container = new ArrayBlockingQueue<ToReceiveMessage>(1);
    private LinkedBlockingQueue listBytes = new LinkedBlockingQueue(8);


    public TCP_old(Network downLayer, int source_ip) {
        super(downLayer);
        this.source_ip = source_ip;
        //conexoes = new HashMap<Integer, Connection>();
    }


    /**
     * Classe interna que servira' para encapsular data + ip.
     */
     private static class ToReceiveMessage {
        public final InterlayerData data;
        public final int source_ip;
        public ToReceiveMessage(InterlayerData data, int source_ip) {
            this.data = data;
            this.source_ip = source_ip;
        }
    }

//     public void threads() {
//         
//         new Thread() {
//            @Override
//             public void run() {
//                 close(connection);
//             }
//         }.start();
//         
//     }


     /**
      * Metodo que recebe os dados da camada de rede.
      *
      * @param data Dados provinientes da camada de rede.
      * @param source_ip IP local.
      */
    @Override
    protected void processReceivedData(InterlayerData data, int source_ip) {
        if(connection != null) {
            try {
                receive(connection);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(TCP_old.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ToReceiveMessage receive = new ToReceiveMessage(data, source_ip);
        container.add(receive);
        
        String chainBit = Integer.toBinaryString(data.data[0]);
        char fin = chainBit.charAt(23);
        if(fin == '1') {
            //aqui pegara' uma conexao na tabela
            closeReply = true;
            close(connection);
        }

    }


    /**
     * Ira' executar o connect usando o Handshake de 3 vias, onde utilizara'
     * a primeira e terceira via do handshake.
     *
     * @param dest_ip IP remoto.
     * @param remote_port Porta remota.
     * @return Conexao estabelecida.
     * @throws UnnableToConnectException Lancara' caso nao ocorra conexao
     */
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
            finalTime = System.currentTimeMillis();
            if( ( finalTime - initialTime ) > TIME_OUT_CONNECTION ) {
                throw new UnnableToConnectException();
            }
        }


        ToReceiveMessage message = (ToReceiveMessage) container.poll();

        String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

        PacketTCP packetReceived = new PacketTCP(dataReceived);
        PacketTCP thirdWay = handshake.thirdWay(dest_ip, remote_port);

        String dataHeaderThirdWay = thirdWay.toString();

        InterlayerData dataThirdWay = new InterlayerData(dataHeaderThirdWay.length());
        dataThirdWay.data[0] = parseStringToInt(dataHeaderThirdWay);

        bubbleDown(dataThirdWay, dest_ip);

        logger.debug("Conexao estabelecida!");

        connection = new Connection(local_port, remote_port, source_ip, dest_ip, this);

        return connection;
    }


    /**
     * Ira' executar o listen usando o Handshake de 3 vias, onde utilizara'
     * a segunda via do handshake.
     *
     * @param local_port Porta local.
     * @return Conexao estabelecida.
     */
    @Override
    @SuppressWarnings("empty-statement")
    public Connection listen(int local_port) {
        
        PacketTCP packetReceived;
        ToReceiveMessage message;

        do {
            while( (container.size() == 0 ));
            
            message = (ToReceiveMessage) container.poll();
            String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

            packetReceived = new PacketTCP(dataReceived);

        } while( !packetReceived.getPortLocal().
                equals(parseIntToString(local_port, NUM_BITS_MAX_PORT)) );

        PacketTCP secondWay = handshake.secondWay(local_port,
                parseStringToInt(packetReceived.getPortLocal()), null);
        String dataHeaderSecondWay = secondWay.toString();

        InterlayerData dataSecondWay = new InterlayerData(dataHeaderSecondWay.length());
        dataSecondWay.data[0] = parseStringToInt(dataHeaderSecondWay);

        bubbleDown(dataSecondWay, message.source_ip);

        connection = new Connection(local_port, parseStringToInt(packetReceived.getPortRemote()),
                source_ip, message.source_ip, this);

        return connection;
    }


    /**
     * Metodo que encerra a conexao, onde tera um bloco if-else que dividira'
     * o transmissor e receptor do disconnect request.
     *
     * @param connection Conexao a qual sera' encerrada.
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void close(Connection connection) {
        //throw new UnsupportedOperationException("Not supported yet.");
        ThreeWaysHandshakeClose handshakeClose = new ThreeWaysHandshakeClose();
        ToReceiveMessage message;

        // aqui solicita o disconnect request
        if(!closeReply) {
            PacketTCP firstWay = handshakeClose.firstWay(lastReceivedPacket);
            String dataHeaderFirstWay = firstWay.toString();

            InterlayerData dataFirstWay = new InterlayerData(dataHeaderFirstWay.length());
            dataFirstWay.data[0] = parseStringToInt(dataHeaderFirstWay);

            bubbleDown(dataFirstWay, connection.destIp);


            // ver o fato se ainda esta' recebendo dados
            while( (container.size() == 0 ));


            message = (ToReceiveMessage) container.poll();
            String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

            PacketTCP thirdWay = handshakeClose.thirdWay(new PacketTCP(dataReceived));
            String dataHeaderThirdWay = thirdWay.toString();

            InterlayerData dataThirdWay = new InterlayerData(dataHeaderThirdWay.length());
            dataFirstWay.data[0] = parseStringToInt(dataHeaderThirdWay);

            bubbleDown(dataThirdWay, connection.destIp);

            closeConnection = true;
            this.connection = null;
        }

        else {
            long initialTime = System.currentTimeMillis();
            long finalTime = 0;

            PacketTCP secondWay = handshakeClose.secondWay(lastReceivedPacket);

            String dataHeaderSecondWay = secondWay.toString();
            InterlayerData dataSecondWay = new InterlayerData(dataHeaderSecondWay.length());

            dataSecondWay.data[0] = parseStringToInt(dataHeaderSecondWay);

            bubbleDown(dataSecondWay, connection.destIp);

            while( finalTime < MAXIMUM_SEGMENT_LIFETIME ) {
                finalTime = System.currentTimeMillis() - initialTime;
            }

            closeReply = false;

            closeConnection = true;
            this.connection = null;
        }

    }

    public void send(Connection con) {
        String chainBytes = "";
        timeoutPut = true;
        
        if(maxPacketSize() == listBytes.size()) {
                for(int c = 0; c < listBytes.size(); c++) {
                    int num = (Integer)listBytes.poll();
                    String bytes = parseIntToString(num, 8);
                    chainBytes = bytes + chainBytes;
                }

                PacketTCP packet = new PacketTCP(parseIntToString(con.localPort, NUM_BITS_MAX_PORT),
                        parseIntToString(con.remotePort, source_ip), "");
                packet.setSequenceNumber(lastReceivedPacket.getAckNumber());
                int plus = parseStringToInt(lastReceivedPacket.getSequenceNumber()) + 1;
                packet.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );
                packet.setData(chainBytes);

                String completePacket = packet.toString();
                InterlayerData dataPacket = new InterlayerData(completePacket.length());

                int numOfData = completePacket.length()/32;
                int initial = 0, last = 8;

                for(int c = 0; c < numOfData; c++) {
                    dataPacket.data[c] = parseStringToInt(completePacket.substring(initial, last));
                    initial = last; last = 2*last;
                }

                bubbleDown(dataPacket, con.destIp);
            }

            else {

                timeoutPut = false;

                long initialTime = System.currentTimeMillis();
                long finalTime;
                boolean outLariat = false;

                while( !outLariat ) {
                    finalTime = System.currentTimeMillis();

                    if(timeoutPut) {
                        return;
                    }
                    if( ( finalTime - initialTime ) > TIME_OUT_PUT ) {

                        for(int c = 0; c < listBytes.size(); c++) {
                    int num = (Integer)listBytes.poll();
                    String bytes = parseIntToString(num, 8);
                    chainBytes = bytes + chainBytes;
                }

                PacketTCP packet = new PacketTCP(parseIntToString(con.localPort, NUM_BITS_MAX_PORT),
                        parseIntToString(con.remotePort, source_ip), "");
                packet.setSequenceNumber(lastReceivedPacket.getAckNumber());
                int plus = parseStringToInt(lastReceivedPacket.getSequenceNumber()) + 1;
                packet.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );
                packet.setData(chainBytes);

                String completePacket = packet.toString();
                InterlayerData dataPacket = new InterlayerData(completePacket.length());

                int numOfData = completePacket.length()/32;
                int initial = 0, last = 8;

                for(int c = 0; c < numOfData; c++) {
                    dataPacket.data[c] = parseStringToInt(completePacket.substring(initial, last));
                    initial = last; last = 2*last;
                }

                bubbleDown(dataPacket, con.destIp);

                        timeoutPut = true;
                    }
                }

            }
    }

    @Override
    protected boolean isActive(Connection con) {
        if( !closeConnection && (con == null) ) {
            return false;
        }

        return true;
    }

    @Override
    protected void put(Connection con, byte b) {
        
        listBytes.offer(b);
        
        if( minPacketSize() > (listBytes.size() + NUM_BITS_HEADER/8) ) {
            send(con);
        }

           

        
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("empty-statement")
    public void receive(Connection con) throws InterruptedException {

        PacketTCP packetReceived;
        ToReceiveMessage message = (ToReceiveMessage) container.take();

        String dataReceived = parseIntToString(message.data.data[0], NUM_BITS_HEADER);

        packetReceived = new PacketTCP(dataReceived);

        int numBytes = packetReceived.getData().length()/8;
        int initial = 0, last = 8;

        for(int c = 0; c < numBytes; c++) {
            byte num = (byte)parseStringToInt(packetReceived.getData().substring(initial, last));
            num = (byte) (num & 0xff);
            bubbleUp(con, num);
        }

    }

    @Override
    public int minPacketSize() {
        //throw new UnsupportedOperationException("Not supported yet.");
       return downLayer.maxPacketSize();
    }

    @Override
    public int maxPacketSize() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return downLayer.minPacketSize();
    }


    /**
     * Dado uma cadeia de string de 0s e 1s, ira' retornar um inteiro.
     *
     * @param value Cadeia de bits.
     * @return Numero inteiro.
     */
     private int parseStringToInt( String value ) {

        return Integer.parseInt(value, 2);
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

         while ( value > (Math.pow(2, numBit)-1) ) {
             value = value - (int) (Math.pow(2, numBit));
         }

         String string = Integer.toBinaryString(value);

         if(string.length() < numBit) {
             int addBit = numBit - string.length();

             for(int c = 0; c < addBit; c++) {
                string = "0" + string;
             }
         }

         return string;
     }

    public LinkedBlockingQueue getListBytes() {
        return listBytes;
    }

    public void setListBytes(LinkedBlockingQueue listBytes) {
        this.listBytes = listBytes;
    }

    public PacketTCP getLastReceivedPacket() {
        return lastReceivedPacket;
    }

    public void setLastReceivedPacket(PacketTCP lastReceivedPacket) {
        this.lastReceivedPacket = lastReceivedPacket;
    }

    



}
