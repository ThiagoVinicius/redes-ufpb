/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.source;

/**
 *
 * @author Jailton
 */
public interface IConstants {

    public static final int TIME_OUT_CONNECTION = 1000;

    public static final int TIME_OUT_PUT = 300;

    public static final int MAXIMUM_SEGMENT_LIFETIME = 10000;

    public static final int NUM_BITS_MAX_ACKNUMBER = 4;

    public static final int NUM_BITS_MAX_SEQNUMBER = 4;

    public static final int NUM_BITS_MAX_PORT = 4;

    public static final int NUM_BITS_MAX_WINDOW = 4;

    public static final int NUM_BITS_FLAGS = 4;
     
    public static final int BUFFER_SIZE = 24;
    
    public static final long PACKET_WAIT_TIMEOUT = 10L;


     public static final int NUM_BITS_HEADER = NUM_BITS_MAX_ACKNUMBER +
             2*NUM_BITS_MAX_PORT + NUM_BITS_MAX_SEQNUMBER +
             NUM_BITS_MAX_WINDOW + NUM_BITS_FLAGS;

}
