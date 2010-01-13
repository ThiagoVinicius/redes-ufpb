/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Esta classe esta aqui para abstrair um array de bits.
 * <p/>
 * Esta classe se parece muito com a classe BitSet, da biblioteca padrao de
 * Java, exceto que esta aqui e' implementada utilizando um array de bytes,
 * visivel publicamente.
 * <p/>
 * Esta classe existe para facilitar a vida das pessoas, ao permitir a
 * manipulacao e inspecao de bits individuais num array de bytes - o que sera
 * bastante necessario, num projeto de redes.
 * <p/>
 * O array que implementa estas funcionalidades e' visivel publicamente, entao
 * voce pode editar do jeito que achar melhor.
 *
 * @author Thiago
 */
public class InterlayerData {

    private static final int BIT_0 =  (1 << 0);
    private static final int BIT_1 =  (1 << 1);
    private static final int BIT_2 =  (1 << 2);
    private static final int BIT_3 =  (1 << 3);
    private static final int BIT_4 =  (1 << 4);
    private static final int BIT_5 =  (1 << 5);
    private static final int BIT_6 =  (1 << 6);
    private static final int BIT_7 =  (1 << 7);
    private static final int BIT_8 =  (1 << 8);
    private static final int BIT_9 =  (1 << 9);
    private static final int BIT_10 = (1 << 10);
    private static final int BIT_11 = (1 << 11);
    private static final int BIT_12 = (1 << 12);
    private static final int BIT_13 = (1 << 13);
    private static final int BIT_14 = (1 << 14);
    private static final int BIT_15 = (1 << 15);
    private static final int BIT_16 = (1 << 16);
    private static final int BIT_17 = (1 << 17);
    private static final int BIT_18 = (1 << 18);
    private static final int BIT_19 = (1 << 19);
    private static final int BIT_20 = (1 << 20);
    private static final int BIT_21 = (1 << 21);
    private static final int BIT_22 = (1 << 22);
    private static final int BIT_23 = (1 << 23);
    private static final int BIT_24 = (1 << 24);
    private static final int BIT_25 = (1 << 25);
    private static final int BIT_26 = (1 << 26);
    private static final int BIT_27 = (1 << 27);
    private static final int BIT_28 = (1 << 28);
    private static final int BIT_29 = (1 << 29);
    private static final int BIT_30 = (1 << 30);
    private static final int BIT_31 = (1 << 31);

    private static final int N_BIT_0 = ~(1 << 0);
    private static final int N_BIT_1 = ~(1 << 1);
    private static final int N_BIT_2 = ~(1 << 2);
    private static final int N_BIT_3 = ~(1 << 3);
    private static final int N_BIT_4 = ~(1 << 4);
    private static final int N_BIT_5 = ~(1 << 5);
    private static final int N_BIT_6 = ~(1 << 6);
    private static final int N_BIT_7 = ~(1 << 7);
    private static final int N_BIT_8 = ~(1 << 8);
    private static final int N_BIT_9 = ~(1 << 9);
    private static final int N_BIT_10 = ~(1 << 10);
    private static final int N_BIT_11 = ~(1 << 11);
    private static final int N_BIT_12 = ~(1 << 12);
    private static final int N_BIT_13 = ~(1 << 13);
    private static final int N_BIT_14 = ~(1 << 14);
    private static final int N_BIT_15 = ~(1 << 15);
    private static final int N_BIT_16 = ~(1 << 16);
    private static final int N_BIT_17 = ~(1 << 17);
    private static final int N_BIT_18 = ~(1 << 18);
    private static final int N_BIT_19 = ~(1 << 19);
    private static final int N_BIT_20 = ~(1 << 20);
    private static final int N_BIT_21 = ~(1 << 21);
    private static final int N_BIT_22 = ~(1 << 22);
    private static final int N_BIT_23 = ~(1 << 23);
    private static final int N_BIT_24 = ~(1 << 24);
    private static final int N_BIT_25 = ~(1 << 25);
    private static final int N_BIT_26 = ~(1 << 26);
    private static final int N_BIT_27 = ~(1 << 27);
    private static final int N_BIT_28 = ~(1 << 28);
    private static final int N_BIT_29 = ~(1 << 29);
    private static final int N_BIT_30 = ~(1 << 30);
    private static final int N_BIT_31 = ~(1 << 31);

    private static final int BIT_ARRAY[] = {
        BIT_0,  BIT_1,  BIT_2,  BIT_3,
        BIT_4,  BIT_5,  BIT_6,  BIT_7,
        BIT_8,  BIT_9,  BIT_10, BIT_11,
        BIT_12, BIT_13, BIT_14, BIT_15,
        BIT_16, BIT_17, BIT_18, BIT_19,
        BIT_20, BIT_21, BIT_22, BIT_23,
        BIT_24, BIT_25, BIT_26, BIT_27,
        BIT_28, BIT_29, BIT_30, BIT_31,
    };

    private static final int N_BIT_ARRAY[] = {
        N_BIT_0,  N_BIT_1,  N_BIT_2,  N_BIT_3,
        N_BIT_4,  N_BIT_5,  N_BIT_6,  N_BIT_7,
        N_BIT_8,  N_BIT_9,  N_BIT_10, N_BIT_11,
        N_BIT_12, N_BIT_13, N_BIT_14, N_BIT_15,
        N_BIT_16, N_BIT_17, N_BIT_18, N_BIT_19,
        N_BIT_20, N_BIT_21, N_BIT_22, N_BIT_23,
        N_BIT_24, N_BIT_25, N_BIT_26, N_BIT_27,
        N_BIT_28, N_BIT_29, N_BIT_30, N_BIT_31,
    };


    public InterlayerData(int datalength) {
        data = new int[datalength % 32 == 0 ? datalength/32 : datalength/32 + 1];
        length = datalength;
    }

    public final int data[];
    public final int length;

    public void putInfo (int start, int len, int infoToPut) {
        if (len > 32 || len < 0)
            throw new InvalidParameterException();

        for (int i = start+len-1; i >= start; --i) {
            if ((infoToPut & 0x01) != 0)
                setBit(i);
            else
                clearBit(i);

            infoToPut = infoToPut >>> 1;
        }

    }

    public int takeInfo (int start, int len) {
        if (len > 32 || len < 0)
            throw new InvalidParameterException();

        int result = 0;

        for (int i = 0; i < len; ++i) {
            result <<= 1;
            if (getBit(start+i) == true)
                result |= 0x01;
        }

        return result;

    }

    /**
     * Copia <code>len</code> bits de <code>src</code> a <code>dest</code>,
     * lendo a partir de <code>offset</code>, em <code>src</code>, e comecando
     * a escrita na posicao <code>start</code>, em <code>dest</code>
     *
     * @param dest destino dos bits
     * @param src origem dos bits
     * @param offset posicao inicial da leitura dos bits
     * @param len quantidade de bits a serem copiados
     * @param start posicao inicial de escrita dos bits.
     */
    public static void copyBits (
            InterlayerData dest,
            InterlayerData src,
            int offset,
            int len,
            int start) {

        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset nao pode ser negativo");
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("len nao pode ser negativo");
        }
        if (start < 0) {
            throw new ArrayIndexOutOfBoundsException("start nao pode ser negativo");
        }
        if (offset + len > src.length) {
            throw new ArrayIndexOutOfBoundsException("offset + len maior que src.length");
        }
        if (start + len > dest.length) {
            throw new ArrayIndexOutOfBoundsException("start + len maior que dest.length");
        }


        for (int i = 0; i < len; ++i) {
            if (src.getBit(i+offset) == true) {
                dest.setBit(i+start);
            } else {
                dest.clearBit(i+start);
            }
        }

    };


    /**
     * Faz com que um bit de um array passe a ter valor logico 1.
     *
     * @param index Indice do bit - bit menos significativo esta no indice 0.
     *              <p>
     *              Este parametro deve ter valor entre <code>0</code>
     *              (inclusive) e <code>data.length - 1</code> (exclusive).
     *              <br>
     *              Especialmente, se <code>data.length -1</code> e' zero,
     *              este metodo falhara.
     *
     * @throws ArrayIndexOutOfBoundsException Sempre que o indice for invalido
     *                                        para este array (vide comentarios
     *                                        acima) ou se <code>data</code>
     *                                        tem tamanho 0.
     *
     */
    public void setBit (int index) {
        data[index/32] |= BIT_ARRAY[31 - index%32];
    }

    /**
     * Faz com que um bit deste array passe a ter valor logico 0.
     *
     * @param index Indice do bit - bit menos significativo esta no indice 0.
     *              <p>
     *              Este parametro deve ter valor entre <code>0</code>
     *              (inclusive) e <code>data.length - 1</code> (exclusive).
     *              <br>
     *              Especialmente, se <code>data.length -1</code> e' zero,
     *              este metodo falhara.
     *
     * @throws ArrayIndexOutOfBoundsException Sempre que o indice for invalido
     *                                        para este array (vide comentarios
     *                                        acima).
     *
     */
    public void clearBit (int index) {
        data[index/32] &= N_BIT_ARRAY[31 - index%32];
    }

    /**
     * Inverte o valor de um unico bit.
     *
     * @param index
     */
    public void flipBit (int index) {
        data[index/32] ^= BIT_ARRAY[31 - index%32];
    }

    /**
     * Retorna o valor logico do bit contido neste array.
     *
     * @param index Indice do bit - bit menos significativo esta no indice 0.
     *              <p>
     *              Este parametro deve ter valor entre <code>0</code>
     *              (inclusive) e <code>data.length - 1</code> (exclusive).
     *              <br>
     *              Especialmente, se <code>data.length -1</code> e' zero,
     *              este metodo falhara.
     *
     * @return <code>true</code>, se o valor do bit no indice solicitado e' 1.
     *         <code>false</code>, caso contrario.
     *
     * @throws ArrayIndexOutOfBoundsException Sempre que o indice for invalido
     *                                        para este array (vide comentarios
     *                                        acima).
     *
     */
    public boolean getBit (int index) {
        return (data[index/32] & BIT_ARRAY[31 - index%32]) != 0;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterlayerData other = (InterlayerData) obj;
        if (this.length != other.length) {
            return false;
        }

        int rest = length % 32;
        int last = data.length-1;

        int mask = ~((1 << rest) - 1);

        if (length > 0) {
            data[last] &= mask;
            other.data[last] &= mask;
        }

        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int rest = length % 32;
        int last = data.length-1;

        int mask = ~((1 << rest) - 1);

        if (length > 0) {
            data[last] &= mask;
        }

        int hash = 3;
        hash = 59 * hash + Arrays.hashCode(this.data);
        hash = 59 * hash + this.length;
        return hash;
    }

    private static final String decToBin (int dec, int size) {
        if (size == 0)
            size = 32;

        long theOrMask = 1L << size;
        long theAndMask = theOrMask - 1L;
        long newDec = (dec & theAndMask) | theOrMask;

        String result = Long.toBinaryString(newDec);
        return result.substring(1);
    }

    @Override
    public String toString () {
        StringBuilder buf = new StringBuilder(length + length/8);

        for (int i = 0; i < data.length - 1; ++i) {
            buf.append(decToBin(data[i], 32));
        }
        if (data.length > 0)
            buf.append(decToBin(data[data.length-1] >>> (32 - length%32), length%32));

        for (int i = (length/8)*8; i > 0; i -= 8) {
            buf.insert(i, " ");
        }

        return buf.toString().trim();
    }



}
