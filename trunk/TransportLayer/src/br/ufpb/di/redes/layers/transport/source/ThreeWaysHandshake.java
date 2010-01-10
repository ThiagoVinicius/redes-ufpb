/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

import java.util.Random;

/**
 *
 * @author Jailton
 */
public class ThreeWaysHandshake implements IConstants {

    private String initialSequenceNumber_A;
    private String initialSequenceNumber_B;

    public ThreeWaysHandshake() {
       chooseSeqNumber();
    }

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

    public PacketTCP firstWay( int portSrc, int portDst ) {
        
        String portLocal = parseIntToString(portSrc, NUM_BITS_MAX_PORT);
        String portRemote = parseIntToString(portDst, NUM_BITS_MAX_PORT);
        PacketTCP packet = new PacketTCP( portLocal, portRemote, "" );

        packet.setSequenceNumber( initialSequenceNumber_A );
        packet.setACKFlag("0");
        packet.setSYNFlag("1");
        packet.setPortLocal(portLocal);
        packet.setPortRemote(portRemote);

        return packet;
    }

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

        return packet;
    }

    public PacketTCP thirdWay( PacketTCP packetReply ) {
        PacketTCP packet = new PacketTCP( packetReply.getPortLocal(),
                packetReply.getPortRemote(), "" );

        packet.setSequenceNumber(packetReply.getAckNumber());
        int plus = parseStringToInt(packetReply.getSequenceNumber()) + 1;
        packet.setAckNumber( parseIntToString(plus, NUM_BITS_MAX_ACKNUMBER) );
        packet.setSYNFlag("0");
        
        return packet;
    }

     private int parseStringToInt( String value ) {

         int valueInt = 0;
         int base = 1;

         for(int c = value.length()-1; c >= 0; c--) {
             valueInt += (value.charAt(c)-'0') * base;
             base *= 2;
         }

         return valueInt;
     }

     private String parseIntToString( int value, int numBit ) {

         if( value > (Math.pow(2, numBit)-1) ) {
             throw new IllegalArgumentException("O valor estoura o numero de bits!");
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
