/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jailton
 */
public class ThreeWaysHandshake implements IConstants {

    private static final Logger logger = LoggerFactory.getLogger(ThreeWaysHandshake.class);

    private String initialSequenceNumber_A;
    private String initialSequenceNumber_B;

    public ThreeWaysHandshake() {
       chooseSeqNumber();
    }

    /**
     * Ira' escolher randomicamente o numero de sequencia, sendo cada entidade
     * possuindo um numero exclusivo.
     */
    private void chooseSeqNumber() {
        Random random = new Random();

        int num1 = random.nextInt( (int) Math.pow(2, NUM_BITS_MAX_SEQNUMBER) );
        int num2 = random.nextInt( (int) Math.pow(2, NUM_BITS_MAX_SEQNUMBER) );

        while(num1 == num2) {
            num2 = random.nextInt( (int) Math.pow(2, NUM_BITS_MAX_SEQNUMBER) );
        }

        initialSequenceNumber_A = parseIntToString(num1, NUM_BITS_MAX_SEQNUMBER);
        initialSequenceNumber_B = parseIntToString(num2, NUM_BITS_MAX_SEQNUMBER);
    }

    /**
     * Primeira via do handshake (pedido de conexao).
     *
     * @param portSrc Porta local em inteiro.
     * @param portDst Porta remota em inteiro.
     * @return Pacote configurado com numeros e flags do primeiro handshake.
     */
    public PacketTCP firstWay( int portSrc, int portDst ) {
        
        String portLocal = parseIntToString(portSrc, NUM_BITS_MAX_PORT);
        String portRemote = parseIntToString(portDst, NUM_BITS_MAX_PORT);
        PacketTCP packet = new PacketTCP( portLocal, portRemote, "" );

        packet.setSequenceNumber( initialSequenceNumber_A );
        packet.setACKFlag("0");
        packet.setSYNFlag("1");
        packet.setPortLocal(portLocal);
        packet.setPortRemote(portRemote);

        logger.debug("Primeira via do handshake executada.");

        return packet;
    }

    /**
     * Segunda via do handshake (resposta).
     *
     * @param portSrc Porta local em inteiro.
     * @param portDst Porta remota em inteiro.
     * @param packetConnection Pacote recebido do transmissor.
     * @return Pacote configurado com numeros e flags do segundo handshake.
     */
    public PacketTCP secondWay( int portSrc, int portDst,
            PacketTCP packetConnection ) {

        String portLocal = parseIntToString(portSrc, NUM_BITS_MAX_PORT);
        String portRemote = parseIntToString(portDst, NUM_BITS_MAX_PORT);
        PacketTCP packet = new PacketTCP( portLocal, portRemote, "" );

        packet.setSequenceNumber( initialSequenceNumber_B );
        int plus = parseStringToInt( packetConnection.getSequenceNumber() ) + 1;
        packet.setAckNumber( parseIntToString( plus, NUM_BITS_MAX_ACKNUMBER) );
        packet.setACKFlag("1");
        packet.setSYNFlag("1");

        logger.debug("Segunda via do handshake executada.");

        return packet;
    }

    /**
     * Terceira via do handshake (confirmacao).
     *
     * @param packetReply Pacote resposta do receptor.
     * @return Pacote configurado com numeros e flags do terceiro handshake.
     */
    public PacketTCP thirdWay( PacketTCP packetReply ) {
        PacketTCP packet = new PacketTCP( packetReply.getPortLocal(),
                packetReply.getPortRemote(), "" );

        packet.setSequenceNumber(packetReply.getAckNumber());
        int plus = parseStringToInt(packetReply.getSequenceNumber()) + 1;
        packet.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );
        packet.setSYNFlag("0");

        logger.debug("Terceira via do handshake executada.");
        
        return packet;
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
