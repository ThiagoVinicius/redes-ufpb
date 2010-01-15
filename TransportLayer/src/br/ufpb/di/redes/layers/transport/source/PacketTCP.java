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
public class PacketTCP implements IConstants {

    private static final Logger logger = LoggerFactory.getLogger(PacketTCP.class);

    private String portLocal;
    private String portRemote;
    private String sequenceNumber;
    private String ackNumber;
    private String windowSize;

    private String ACK;
    private String RST;
    private String SYN;
    private String FIN;

    private String data;

    public PacketTCP(String portLocal, String portRemote, String data) {
        this.portLocal = portLocal;
        this.portRemote = portRemote;
        this.sequenceNumber = "0000";
        this.ackNumber = "1111";
        this.windowSize = "0000";

        this.ACK = "0";
        this.RST = "0";
        this.SYN = "0";
        this.FIN = "0";

        this.data = data;
    }

    public PacketTCP(String stream) {
        logger.debug(stream);
        int initial = 0, last = NUM_BITS_MAX_PORT;
        this.portLocal = stream.substring(initial, last);

        initial = last; last = initial+NUM_BITS_MAX_PORT;
        this.portRemote = stream.substring(initial, last);

        initial = last; last = initial+NUM_BITS_MAX_SEQNUMBER;
        this.sequenceNumber = stream.substring(initial, last);

        initial = last; last = initial+NUM_BITS_MAX_ACKNUMBER;
        this.ackNumber = stream.substring(initial, last);

        initial = last; last = initial+NUM_BITS_MAX_WINDOW;
        this.windowSize = stream.substring(initial, last);

        initial = last; last = initial+1;
        this.ACK = stream.substring(initial, last);

        initial = last; last = initial+1;
        this.RST = stream.substring(initial, last);

        initial = last; last = initial+1;
        this.SYN = stream.substring(initial, last);
        
        initial = last; last = initial+1;
        this.FIN = stream.substring(initial, last);

        if( last == (stream.length()) ) {
            this.data = ""; 
        }
        else {
            initial = last; last = stream.length();
            this.data = stream.substring(initial, last);
        }
    }

    @Override
   public String toString() {
        return (portLocal + portRemote + sequenceNumber + ackNumber +
                windowSize + ACK + RST + SYN + FIN + data);
    }

    public String getACKFlag() {
        return ACK;
    }

    public void setACKFlag(String ACK) {
        this.ACK = ACK;
    }

    public String getAckNumber() {
        return ackNumber;
    }

    public void setAckNumber(String ackNumber) {
        this.ackNumber = ackNumber;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFINFlag() {
        return FIN;
    }

    public void setFINFlag(String FIN) {
        this.FIN = FIN;
    }

    public String getPortLocal() {
        return portLocal;
    }

    public void setPortLocal(String portLocal) {
        this.portLocal = portLocal;
    }

    public String getPortRemote() {
        return portRemote;
    }

    public void setPortRemote(String portRemote) {
        this.portRemote = portRemote;
    }

    public String getRSTFlag() {
        return RST;
    }

    public void setRSTFlag(String RST) {
        this.RST = RST;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSYNFlag() {
        return SYN;
    }

    public void setSYNFlag(String SYN) {
        this.SYN = SYN;
    }

    public String getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }
    
}
