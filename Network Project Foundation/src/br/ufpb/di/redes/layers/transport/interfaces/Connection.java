/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.transport.interfaces;

/**
 * Esta classe representa uma conexao entre dois pontos.
 *
 * @author Thiago
 */
public abstract class Connection {

//    public final int local_port;
//    public final int remote_port;
//    public final int dest_ip;
//    public final int source_ip;
//
//    public Connection(int local_port, int remote_port, int dest_ip, int source_ip) {
//        this.local_port = local_port;
//        this.remote_port = remote_port;
//        this.dest_ip = dest_ip;
//        this.source_ip = source_ip;
//    }

    /**
     * Envia dados para o host remoto.
     * @param data dados a ser enviados
     * @throws ConnectionLostException
     */
    public abstract void send(byte data[]) throws ConnectionLostException;

    /**
     * Recebe dados do host remoto, salvando-os no array data.
     * @param data
     * @throws ConnectionLostException
     */
    public abstract void receive(byte data[]) throws ConnectionLostException;

    /**
     * Encerra a conexao com o host remoto.
     */
    public abstract void close();

    /**
     * Verifica se a conexao ainda esta ativa.
     * @return
     */
    public abstract boolean isActive();

}
