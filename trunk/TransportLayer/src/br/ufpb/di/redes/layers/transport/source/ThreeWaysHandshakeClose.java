/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jailton
 */
public class ThreeWaysHandshakeClose implements IConstants {

    private static final Logger logger = LoggerFactory.getLogger(ThreeWaysHandshakeClose.class);


    public PacketTCP firstWay( PacketTCP packet ) {

        PacketTCP packetOut = new PacketTCP( packet.getPortRemote(), packet.getPortLocal(), "" );

        packetOut.setSequenceNumber(packet.getAckNumber());
        int plus = parseStringToInt(packet.getSequenceNumber()) + 1;
        packetOut.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );

        packetOut.setFINFlag("1");
        packetOut.setACKFlag("0");

        return packetOut;
    }

    public PacketTCP secondWay( PacketTCP packet ) {

        PacketTCP packetOut = new PacketTCP( packet.getPortRemote(), packet.getPortLocal(), "" );

        packetOut.setSequenceNumber(packet.getAckNumber());
        int plus = parseStringToInt(packet.getSequenceNumber()) + 1;
        packetOut.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );

        packetOut.setFINFlag("1");
        packetOut.setACKFlag("1");

        return packetOut;
    }

    public PacketTCP thirdWay( PacketTCP packet ) {

        PacketTCP packetOut = new PacketTCP( packet.getPortRemote(), packet.getPortLocal(), "" );

        packetOut.setSequenceNumber(packet.getAckNumber());
        int plus = parseStringToInt(packet.getSequenceNumber()) + 1;
        packetOut.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );

        packetOut.setFINFlag("0");
        packetOut.setACKFlag("1");

        return packetOut;
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

         /*
         if ( value > (Math.pow(2, numBit)-1) ) {
             throw new IllegalArgumentException("O valor estoura o numero de bits!");
         }
         */

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

}
