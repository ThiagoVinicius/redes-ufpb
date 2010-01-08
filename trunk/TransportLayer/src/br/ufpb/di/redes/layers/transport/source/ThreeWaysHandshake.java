/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

/**
 *
 * @author Jailton
 */
public class ThreeWaysHandshake {

    public PacketTCP firstWay( String portLocal, String portRemote ) {
        PacketTCP packet = new PacketTCP( portLocal, portRemote, "" );

        packet.setSequenceNumber( "001" );
        packet.setACKFlag( "0" );
        packet.setSYNFlag( "1" );
        packet.setPortLocal(portLocal);
        packet.setPortRemote(portRemote);

        return packet;
    }

    public PacketTCP secondWay( PacketTCP packetConnection ) {
        PacketTCP packet = new PacketTCP( packetConnection.getPortLocal(),
                packetConnection.getPortRemote(), "" );

        packet.setSequenceNumber( "010" );
        int plus = parseStringToInt(packetConnection.getSequenceNumber()) + 1;
        packet.setAckNumber(parseIntToString(plus));
        packet.setACKFlag( "1" );
        packet.setSYNFlag( "1" );

        return packet;
    }

    public PacketTCP thirdWay( PacketTCP packetReply) {
        PacketTCP packet = new PacketTCP( packetReply.getPortLocal(),
                packetReply.getPortRemote(), "" );

        packet.setSequenceNumber(packetReply.getAckNumber());
        int plus = parseStringToInt(packetReply.getSequenceNumber()) + 1;
        packet.setAckNumber(parseIntToString(plus));
        packet.setSYNFlag("0");
        
        return packet;
    }

     private int parseStringToInt( String value ) {

         if(value.equals("000")) return 0;
         if(value.equals("001")) return 1;
         if(value.equals("010")) return 2;
         if(value.equals("011")) return 3;
         if(value.equals("100")) return 4;
         if(value.equals("101")) return 5;
         if(value.equals("110")) return 6;
         if(value.equals("111")) return 7;

         return 0;
     }

     private String parseIntToString( int value ){

         switch(value){
             case 0: return "000";
             case 1: return "001";
             case 2: return "010";
             case 3: return "011";
             case 4: return "100";
             case 5: return "101";
             case 6: return "110";
             case 7: return "111";

             default: return "000";
         }
     }

}
