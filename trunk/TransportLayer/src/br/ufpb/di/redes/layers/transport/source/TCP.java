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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jailton
 */
public class TCP extends Transport {
    
    private Hashtable <Connection, ConnectionState> connections;
    
    private Set <Integer> listen_avail;
    
    private static final Logger logger = LoggerFactory.getLogger(TCP.class);

    public TCP(Network downLayer) {
        super(downLayer);
        this.connections = new Hashtable<Connection, ConnectionState> ();
        listen_avail = new HashSet<Integer>(Arrays.asList(
                new Integer [] {8, 9, 10, 11, 12, 13, 14, 15}));
        listen_avail = Collections.synchronizedSet(listen_avail);
        
        
    }
    
    @Override
    protected void close(Connection con) {

        ConnectionState state = connections.get(con);
        if (state == null) {
            return;
        }
        boolean goodToGo = false;

        synchronized (this) {

            if (state.curState == ConnectionState.State.CONNECTED) {
                state.curState = ConnectionState.State.CLOSE_1;
                goodToGo = true;
            }

        }

        if (goodToGo) {

            while (true) {

                state.handshakeClose = new ThreeWaysHandshakeClose();

                PacketTCP firstWay = state.handshakeClose.firstWay(state.lastPacket);
                String dataHeaderFirstWay = firstWay.toString();

                InterlayerData dataFirstWay = new InterlayerData(dataHeaderFirstWay.length());
                dataFirstWay.putInfo(0, IConstants.NUM_BITS_HEADER, parseStringToInt(dataHeaderFirstWay));

                bubbleDown(dataFirstWay, con.destIp);

                long initialTime, finalTime;
                long elapsedTime;

                initialTime = System.currentTimeMillis();

                synchronized (this) {
                    while (state.curState != ConnectionState.State.CLOSED) {

                        finalTime = System.currentTimeMillis();
                        elapsedTime = finalTime - initialTime;
                        if (elapsedTime > IConstants.TIME_OUT_CONNECTION) {
                            break;
                        }
                        try {
                            wait(Math.abs(IConstants.TIME_OUT_CONNECTION - elapsedTime));
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                break;
            }


        }

    }

    @Override
    public Connection connect(int dest_ip, int remote_port) throws UnnableToConnectException {
        
        int local_port = -1;
        
        for (int i = 8; i < 16; ++i) {
            if (listen_avail.remove(i)) {
                local_port = i;
                break;
            }
        }
        
        if (local_port == -1) {
            throw new UnnableToConnectException();
        }
        
        Connection newCon = new Connection(local_port, remote_port, dest_ip, downLayer.getIp(), this);
        
        ConnectionState old = connections.get(newCon);
        
        if (old != null) {
            throw new UnnableToConnectException();
        }
        
        connections.put(newCon, new ConnectionState(ConnectionState.State.CONNECT_1, newCon, this));
        
        ConnectionState state = connections.get(newCon);
        
        //primeira via do handshake
        long initialTime, finalTime;
        long elapsedTime;
        
        logger.debug("Enviando primeira via do handshake");
        
        ThreeWaysHandshake handshake = new ThreeWaysHandshake();
        state.handshake = handshake;
        
        PacketTCP firstWay = handshake.firstWay(local_port, remote_port);
        String dataHeaderFirstWay = firstWay.toString();
        InterlayerData dataFirstWay = new InterlayerData(dataHeaderFirstWay.length());
        dataFirstWay.putInfo(0, IConstants.NUM_BITS_HEADER, parseStringToInt(dataHeaderFirstWay));
        
        bubbleDown(dataFirstWay, dest_ip);
        
        //state.curState = ConnectionState.State.CONNECT_1;
        
        initialTime = System.currentTimeMillis();

        synchronized (this) {
            while (state.curState != ConnectionState.State.CONNECTED) {
                
                finalTime = System.currentTimeMillis();
                elapsedTime = finalTime - initialTime;
                if (elapsedTime > IConstants.TIME_OUT_CONNECTION) {
                    throw new UnnableToConnectException();
                }
                try {
                    wait(Math.abs(IConstants.TIME_OUT_CONNECTION - elapsedTime));
                } catch (InterruptedException ex) {
                }
            }
        }
        
        
        return newCon;
        
    }

    @Override
    protected boolean isActive(Connection con) {
        ConnectionState state = connections.get(con);
        if (state == null)
            return false;
        return state.curState == ConnectionState.State.CONNECTED ||
               state.curState == ConnectionState.State.WAIT_ACK;
    }

    @Override
    public Connection listen(int local_port) {
        
        Connection newCon = new Connection(local_port, -1, -1, downLayer.getIp(), this);
        
        ConnectionState old = connections.get(newCon);
        
        if (old != null) {
            //FIXME
            logger.debug("Porta ocupada. o.O");
            return null;
        }
        
        
        ConnectionState state = new ConnectionState(ConnectionState.State.LISTEN, newCon, this);
        ThreeWaysHandshake handshake = new ThreeWaysHandshake();
        state.handshake = handshake;
        
        connections.put(newCon, state);
        
        //aguardando conexao
        logger.debug("Aguardando connection request.");
        
        synchronized (this) {
            while (state.curState != ConnectionState.State.CONNECT_2) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        
        state = state.next_hop_CONNECT_2;
        
        synchronized (this) {
            while (state.curState != ConnectionState.State.CONNECTED) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        
        
        logger.debug("Listen retornando.");
        
        return state.con;
        
        
    }

    @Override
    protected void processReceivedData(InterlayerData data, int source_ip) {
        
        //int header = data.takeInfo(0, IConstants.NUM_BITS_HEADER);
        
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data.getBit(i)) {
                sb.append('1');
            } else {
                sb.append('0');
            }
        }
        
        PacketTCP pack = new PacketTCP(sb.toString());

        //connection reply
        if (pack.getACKFlag().equals("1") && pack.getSYNFlag().equals("1")) {
            
            logger.debug("recebido connection reply");

            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();

            Connection key = new Connection(local_port, remote_port, remote_ip, local_ip, this);

            ConnectionState state = connections.get(key);

            if (state == null) {
                logger.debug("Reply para uma conexao que nao existe.");
                return;
            }

            if (state.curState == ConnectionState.State.CONNECT_1) {


                PacketTCP thirdWay = state.handshake.thirdWay(local_port, remote_port);

                String dataHeaderThirdWay = thirdWay.toString();

                InterlayerData dataThirdWay = new InterlayerData(dataHeaderThirdWay.length());
                dataThirdWay.putInfo(0, IConstants.NUM_BITS_HEADER, parseStringToInt(dataHeaderThirdWay));

                bubbleDown(dataThirdWay, remote_ip);

                state.curState = ConnectionState.State.CONNECTED;
                state.lastPacket = pack;
                logger.debug("conexao estabelecida.");

            } 
            else {
                logger.debug("Conexao nao esta no estado CONNECT_1, porem em {}", state.curState);
            }
        }
        
        //connect request
        else if (pack.getACKFlag().equals("0") && pack.getSYNFlag().equals("1")) {
            
            logger.debug("recebido connect request");
            
            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();
            
            Connection key = new Connection(local_port, -1, -1, local_ip, this);
            ConnectionState oldConnection = connections.get(key);
            
            if (oldConnection == null)
                return;
            
            if (oldConnection.curState == ConnectionState.State.LISTEN) {
                
                connections.remove(oldConnection.con);
                //Connection newConnection = new Connection(local_port, remote_port, remote_ip, local_ip, this);
                Connection newConnection = new Connection(local_port, remote_port, remote_ip, local_ip, this);
                
                connections.put(newConnection, new ConnectionState(ConnectionState.State.LISTEN, newConnection, this));
                ConnectionState state = connections.get(newConnection);
                state.handshake = oldConnection.handshake;
                
                PacketTCP secondWay = oldConnection.handshake.secondWay(local_port, remote_port, pack);
                String dataHeaderSecondWay = secondWay.toString();

                InterlayerData dataSecondWay = new InterlayerData(dataHeaderSecondWay.length());
                dataSecondWay.putInfo(0, IConstants.NUM_BITS_HEADER, parseStringToInt(dataHeaderSecondWay));

                bubbleDown(dataSecondWay, remote_ip);

                oldConnection.next_hop_CONNECT_2 = state;
                oldConnection.con = newConnection;
                oldConnection.curState = ConnectionState.State.CONNECT_2;
                state.lastPacket = pack;
                state.curState = ConnectionState.State.CONNECT_2;
                
                
                logger.debug("Enviado connection reply");


            } else {
                logger.debug("Conexao nao esta no estado LISTEN, porem em {}", oldConnection.curState);
            }
            
            
        }
        //close request
        else if (pack.getACKFlag().equals("0") && pack.getFINFlag().equals("1")) {
            
            logger.debug("recebido close request");

            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();

            Connection key = new Connection(local_port, remote_port, remote_ip, local_ip, this);

            ConnectionState state = connections.get(key);

            if (state == null) {
                return;
            }
            
            synchronized (this) {
                if (state.curState == ConnectionState.State.CONNECTED) {
                    state.curState = ConnectionState.State.CLOSE_2;
                } else {
                    return;
                }
            }

            
            state.handshakeClose = new ThreeWaysHandshakeClose();
            
            PacketTCP secondWay = state.handshakeClose.secondWay(pack);

            String dataHeaderSecondWay = secondWay.toString();
            InterlayerData dataSecondWay = new InterlayerData(dataHeaderSecondWay.length());

            dataSecondWay.putInfo(0, IConstants.NUM_BITS_HEADER, parseStringToInt(dataHeaderSecondWay));
            
            bubbleDown(dataSecondWay, remote_ip);

            state.lastPacket = pack;
            state.curState = ConnectionState.State.CLOSED;
            connections.remove(state.con);
            
            listen_avail.add(state.con.localPort);
            
            logger.debug("enviado close reply. Conexao encerrada.");

            
        }
        //close reply
        else if (pack.getACKFlag().equals("1") && pack.getFINFlag().equals("1")) {
            
            logger.debug("recebido close reply");

            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();

            Connection key = new Connection(local_port, remote_port, remote_ip, local_ip, this);

            ConnectionState state = connections.get(key);

            if (state == null) {
                logger.debug("CLOSE REPLY: Conexao nao encontrada. Buaaaaa");
                return;
            }
            
            synchronized (this) {
                if (state.curState == ConnectionState.State.CLOSE_1) {
                    state.curState = ConnectionState.State.CLOSE_3;
                } else {
                    return;
                }
            }

            state.lastPacket = pack;
            
            state.curState = ConnectionState.State.CLOSED;
            connections.remove(state.con);
            
            listen_avail.add(state.con.localPort);
            
            logger.debug("Recebido close reply. Conexao encerrada.");
            
        }
        
        //ACK
        else if (pack.getACKFlag().equals("1") && pack.getFINFlag().equals("0") &&
                 pack.getSYNFlag().equals("0")) {
            
            logger.debug("recebido ack");

            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();

            Connection key = new Connection(local_port, remote_port, remote_ip, local_ip, this);

            ConnectionState state = connections.get(key);

            if (state == null) {
                logger.debug("ACK: Conexao nao encontrada. Buaaaaa");
                return;
            }
            
            synchronized (this) {
                if (state.curState != ConnectionState.State.WAIT_ACK &&
                    state.curState != ConnectionState.State.CONNECT_2) {
                    logger.debug("OMG! Nao esta em WAIT_ACK, nem CONNECT_2 mas em {}", state.curState);
                    return;
                }
            }
            
            state.lastPacket = pack;
            state.curState = ConnectionState.State.CONNECTED;
            //connections.remove(state.con);
            
            //listen_avail.add(state.con.localPort);
            
            logger.debug("Recebido ACK. pacote foi enviado com sucesso.");
            
        }
        
        //Dados
        else if (pack.getACKFlag().equals("0") && pack.getFINFlag().equals("0") &&
                 pack.getSYNFlag().equals("0")) {
            
            logger.debug("recebido dados");

            int local_port = parseStringToInt(pack.getPortRemote());
            int remote_port = parseStringToInt(pack.getPortLocal());
            int remote_ip = source_ip;
            int local_ip = downLayer.getIp();

            Connection key = new Connection(local_port, remote_port, remote_ip, local_ip, this);

            ConnectionState state = connections.get(key);

            if (state == null) {
                logger.debug("RECV: Conexao nao encontrada. Buaaaaa");
                return;
            }
            
            synchronized (this) {
                if (state.curState != ConnectionState.State.CONNECTED &&
                    state.curState != ConnectionState.State.WAIT_ACK) {
                    return;
                    //state.curState = ConnectionState.State.CLOSE_3;
                }
            }
            
            if (!pack.equals(state.lastPacket)) {
                state.lastPacket = pack;
                int numBytes = pack.getData().length()/8;
                int initial = 0, last = 8;
                for (int c = 0; c < numBytes; c++) {
                    int num = parseStringToInt(pack.getData().substring(initial, last));
                    bubbleUp(state.con, num);
                    initial += 8;
                    last += 8;
                }
            }
            //state.lastPacket = pack;
            
            PacketTCP ack = new PacketTCP(
                parseIntToString(state.con.localPort, IConstants.NUM_BITS_MAX_PORT), 
                parseIntToString(state.con.remotePort, IConstants.NUM_BITS_MAX_PORT), 
                "");
            ack.setACKFlag("1");
            
            
            InterlayerData ackData = ack.getInterlayerData();
            
            bubbleDown(ackData, remote_ip);
            
            logger.debug("Pacote recebido. Ack enviado.");
            
        }
        

        synchronized (this) {
            notifyAll();
        }
        
    }

    @Override
    protected void put(Connection con, byte b) {
        ConnectionState state = connections.get(con);
        try {
            state.toSend.put(b);
        } catch (InterruptedException ex) {
            logger.error("Interrupted?!");
        }
    }

    @Override
    public int maxPacketSize() {
        return downLayer.maxPacketSize() - IConstants.NUM_BITS_HEADER;
    }

    @Override
    public int minPacketSize() {
        return 8;
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
     private static String parseIntToString( int value, int numBit ) {

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
     
     /**
     * Dado uma cadeia de string de 0s e 1s, ira' retornar um inteiro.
     *
     * @param value Cadeia de bits.
     * @return Numero inteiro.
     */
     private static int parseStringToInt( String value ) {

        return Integer.parseInt(value, 2);
     }
     
    @Override
    public String getName(){
        return "TRANSPORTE " + downLayer.getIp();
    }

    protected void send(Connection con, List<Byte> data) {
        
        ConnectionState state = connections.get(con);
        
        if (state == null)
            return;
        
        logger.debug("Send chamado");
        
        synchronized (this) {
            
            if (state.curState == ConnectionState.State.WAIT_ACK) {
                while (state.curState != ConnectionState.State.CONNECTED) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        
            if (state.curState != ConnectionState.State.CONNECTED) {
                return;
            }
            else {
                state.curState = ConnectionState.State.WAIT_ACK;
            }
        
        }
        
        logger.debug("Preparando para enviar.");
        
        StringBuilder sb = new StringBuilder();
        
        for (byte i : data) {
            sb.append(parseIntToString(i & 0xff, 8));
        }
        
        String seqNumber = state.seqNumber;
        if (seqNumber.equals("0001")) {
            seqNumber = "0000";
        } else {
            seqNumber = "0001";
        }
        
        state.seqNumber = seqNumber;
        
        PacketTCP packet = new PacketTCP(
                parseIntToString(con.localPort, IConstants.NUM_BITS_MAX_PORT), 
                parseIntToString(con.remotePort, IConstants.NUM_BITS_MAX_PORT), 
                sb.toString());
        
        packet.setSequenceNumber(seqNumber);
        InterlayerData packData = packet.getInterlayerData();
        
        state.waitingAck = packet;
        state.waitingAckData = packData;
        
        long initialTime, finalTime;
        long elapsedTime;

        initialTime = System.currentTimeMillis();
        
        while (state.curState != ConnectionState.State.CONNECTED) {

            state.curState = ConnectionState.State.WAIT_ACK;
            bubbleDown(packData, con.destIp);
            logger.debug("Enviado, aguardando ack. Estado = {}", state.curState);
            
            synchronized (this) {
                while (state.curState != ConnectionState.State.CONNECTED) {

                    finalTime = System.currentTimeMillis();
                    elapsedTime = finalTime - initialTime;
                    if (elapsedTime > IConstants.TIME_OUT_CONNECTION) {
                        break;
                    }
                    try {
                        wait(Math.abs(IConstants.TIME_OUT_CONNECTION - elapsedTime));
                    } catch (InterruptedException ex) {
                    }
                }
            }

        }
        
        
    }

    @Override
    protected void bubbleDown(InterlayerData data, int dest_ip) {
        logger.debug("Enviando mensagem: {}", data);
        super.bubbleDown(data, dest_ip);
    }

    @Override
    protected void bubbleUp(Connection whereTo, int b) {
        logger.debug("Subindo mensagem: {} para {}",
                Integer.toBinaryString(b), whereTo);
        super.bubbleUp(whereTo, b);
    }


    
    

}
