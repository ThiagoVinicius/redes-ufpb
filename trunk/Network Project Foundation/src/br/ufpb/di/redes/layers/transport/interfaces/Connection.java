/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Esta classe representa uma conexao entre dois pontos.
 *
 * @author Thiago
 */
public class Connection {

    private class Output extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            put(b);
        }

    }

    private class Input extends InputStream {

        public LinkedBlockingQueue<Integer> buffer;

        @Override
        public int read() throws IOException {
            try {
                return buffer.take();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        protected void put (int b) {
            buffer.add(b);
        }

    }

    protected Transport myFather;

    private InputStream input;
    private OutputStream output;

    public final int localPort;
    public final int remotePort;
    public final int sourceIp;
    public final int destIp;


    public Connection(int localPort, int remotePort, int destIp, int sourceIp, Transport myFather) {
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.myFather = myFather;
        this.input = new Input();
        this.output = new Output();
    }

    public InputStream getInputStream() {
        return input;
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public void close() {
        myFather.close(this);
    }

    public boolean isActive() {
        return myFather.isActive(this);
    }

    private void put (int b) {
        myFather.put(this, (byte)b);
    }
    
}
