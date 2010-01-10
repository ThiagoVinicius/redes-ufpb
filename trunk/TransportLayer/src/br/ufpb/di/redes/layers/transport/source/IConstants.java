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

    public static final int TIME_OUT_CONNECTION = 60000;

    public static final int NUM_BITS_MAX_ACKNUMBER = 4;

    public static final int NUM_BITS_MAX_SEQNUMBER = 4;

    public static final int NUM_BITS_MAX_PORT = 4;

    public static final int NUM_BITS_MAX_WINDOW = 4;


     public static final int NUM_BITS_HEADER = NUM_BITS_MAX_ACKNUMBER +
             2*NUM_BITS_MAX_PORT + NUM_BITS_MAX_SEQNUMBER +
             NUM_BITS_MAX_WINDOW;

}
