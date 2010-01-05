/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all;

/**
 *
 * @author Thiago
 */
public interface Joint <E> {
    //public void detach();
    public void attach(E attachable);
}
