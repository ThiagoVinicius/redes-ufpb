/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.tests;

import java.util.Random;

/**
 *
 * @author Thiago
 */
public class Util {

    public static Random rand = new Random();

    public static int[] nextInts(int max) {
        int first = rand.nextInt(max);
        int seccond = rand.nextInt(max);
        while (seccond == first) {
            seccond = rand.nextInt(max);
        }

        return new int [] { first, seccond };
    }

}
