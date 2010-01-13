/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all;

/**
 * Camada que não faz nada. Criada para ser passada como parâmetro de classe,
 * para as camadas que estao nas extremidades da pilha.
 *
 * @author Thiago
 */
public final class NullLayer extends Layer {

    public NullLayer() {
        super(null);
    }

    @Override
    public void attach(Layer upLayer) {
    }

    @Override
    public EntityState getState() {
        return EntityState.HALTED;
    }

    @Override
    public void start() {
    }

    @Override
    public int maxPacketSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int minPacketSize() {
        return 0;
    }



}
