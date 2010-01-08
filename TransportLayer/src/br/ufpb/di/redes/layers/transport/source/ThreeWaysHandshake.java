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
        packet.setAckNumber(packetConnection.getSequenceNumber());
        packet.setACKFlag( "1" );
        packet.setSYNFlag( "1" );

        return packet;
    }

    public PacketTCP thirdWay( PacketTCP packetReply) {
        PacketTCP packet = new PacketTCP( packetReply.getPortLocal(),
                packetReply.getPortRemote(), "" );

        packet.setSequenceNumber(packetReply.getAckNumber());
        packet.setAckNumber(packetReply.getSequenceNumber());
        packet.setSYNFlag("0");
        
        return packet;
    }
}
