/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physicalLayer.UteisGraficos;

import physicalLayer.Sinal.Sinal;
import java.io.File;
import java.io.FileWriter;


public class GraficoTxt {

    public static void escreveOndaTxt(byte onda[], String arquivo) {
        System.out.println("");
        FileWriter a = null;
        try {
            a = new FileWriter(new File(arquivo), false);
            for (int i = 0; i < onda.length; i++) {
                a.write(String.valueOf(onda[i]).concat("\n"));

                a.flush();
            }

            a.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bitsDoGrafo(byte onda[], int offset) {
        System.out.println("");
        int fim = offset + Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS;
        byte[] bit = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL];


        FileWriter a = null;
        try {
            int i = 0, z = 0;
            for (int j = offset; j < fim; j++) {

                if ((j - offset) % Sinal.QUANTIDADEAMOSTRAPORSINAL == 0 && j != offset) {
                    a = new FileWriter(new File("bit" + (z++) + ".txt"), false);
                    for (int t = 0; t < bit.length; t++) {
                        a.write(String.valueOf(bit[t]).concat("\n"));
                        a.flush();
                    }

                    i = 0;
                }

                bit[i++] = onda[j];
            }


            a.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
