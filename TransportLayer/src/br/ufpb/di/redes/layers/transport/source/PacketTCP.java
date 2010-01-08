/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

/**
 *
 * @author Jailton
 */
public class PacketTCP {

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
        this.data = data;
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
