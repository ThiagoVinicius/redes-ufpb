/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physicalLayer.AgentesDeSom;

import physicalLayer.Sinal.Sinal;
import physicalLayer.UteisGraficos.GraficoTxt;
import physicalLayer.UteisSom.Som;
import javax.sound.sampled.TargetDataLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaptadorDeSom{

    private static final Logger logger = LoggerFactory.getLogger(CaptadorDeSom.class);

    TargetDataLine linha = null;/*Linha por onde o sinal e lido da caixa de som*/

    static byte[] bitSinalizador = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * 2];
    static byte[] dados = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS + Sinal.AMAIS];
    int numeroDeBytesLidos;
    static byte silencio = 35;/*Acima disso vai ser considarado como um pico*/

    static int MINIMO_POR_BIT_SINALIZADOR = 3;/*Regular*/

    static int PICO = 35;/*Usado na funcao de recuperacao*/

    static int LIMIAR = 10;/*Usado de frequencia*/
    static int vez = 0;
    static int vez2=0;


    /****
     *
     * CaptadorDeSom(): construtor da classe
     *
     ****/
    public CaptadorDeSom() {
        linha = Som.openLineToRead(48000, 8, 1, true, true);
        /***** Sera que seria importante fechar e abrir o canal sempre
        linha.drain();
        linha.close();
         ******/
    }

    public byte[] captaMensagem() {

        int max = 0;

        numeroDeBytesLidos = linha.read(bitSinalizador, 0, bitSinalizador.length);
        GraficoTxt.escreveOndaTxt(bitSinalizador, "BitSinalizadorCaptadado.txt");

        /*01 Verifica se houveram bits sinanizadores de chegada de dados*/
        int contadodorDePicos = 0;/*Essa variavel tem que ser escolhida de acordo com o que o pc ler*/

        for (int i = 0; i < bitSinalizador.length; i++) {
            if (Math.abs(bitSinalizador[i]) > max) {
                max = Math.abs(bitSinalizador[i]);
            }
            if (Math.abs(bitSinalizador[i]) > silencio) {
                ++contadodorDePicos;
            }
        }

        try {

            if (contadodorDePicos > MINIMO_POR_BIT_SINALIZADOR) {
                //System.out.println("CHAMA METODO DE LEITURA DE DADOS");
                /*2.2 Chama o metode de leitura de dados*/
                capturaSom();

                GraficoTxt.escreveOndaTxt(dados, "OndaCaptada" + (vez) + ".txt");
                GraficoTxt.escreveOndaTxt(bitSinalizador, "BitSinalizadorCaptadado" + (vez++) + ".txt");
                //System.exit(1);
                byte[] resultado = analizaSinal(dados, buscaInicioDaOnda(dados));

                return resultado;
            } else {
                // System.out.println("Not min");
                /*2.1 Se Nao espera millsecs e volta para 0*/
                // Thread.sleep(5);
                }
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return null;
    }

    /*****
     * capturaSom(): captura e processa um sinal logo apos a chegada de um bit sinalizador
     **/
    public void capturaSom() {
        numeroDeBytesLidos = linha.read(dados, 0, dados.length);
        /*Aqui os dados devem ser processados e empilhados para a camada de enlace*/
    }

    /***
     *  buscaInicioDaOnda(): busca inicio da onda
     */
    public int buscaInicioDaOnda(byte[] sinalRecebido) {
        int k = 0;

        int marca = 0;
        for (int i = 0; i < Sinal.QUANTIDADEAMOSTRAPORSINAL * 10; i++) {
            if (sinalRecebido[i] > (byte) PICO) {
                marca = i;
            }
            if ((i - marca) > 48) {
                k = (i - marca) / 2 + marca;
                break;
            }
        }

        for (; k < sinalRecebido.length; k++) {
            if (sinalRecebido[k] > PICO) {
                break;
            }
        }
        
        while ((k > 0) && (sinalRecebido[k]) > 0) {
            k--;
        }
        // GraficoTxt.bitsDoGrafo(sinalRecebido, k + 1);
        return ++k;/*Indicativo de erro*/
    }

    public byte[] analizaSinal(byte[] sinalRecebido, int offset) {
        byte[] recuperado = new byte[Sinal.QUANTIDADEDESINAIS];
        int ultimo = offset + Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS;

        int contadorDePicos = 0;/*Sinal 0(positivo) 1*/
        int posBit = 0;
        contadorDePicos = 0;

        try {

            for (int i = offset + 2, j = 1; i < ultimo; i++, j++) {

                if (!(sinalRecebido[i] == 0)) {
                    if (!(sinalRecebido[i - 1] == 0)) {
                        if (!(((sinalRecebido[i] > 0) && (sinalRecebido[i - 1] > 0)) || ((sinalRecebido[i] < 0) && (sinalRecebido[i - 1] < 0)))) {
                            contadorDePicos++;
                        }
                    } else {
                        if (!(((sinalRecebido[i] > 0) && (sinalRecebido[i - 2] > 0)) || ((sinalRecebido[i] < 0) && (sinalRecebido[i - 2] < 0)))) {
                            contadorDePicos++;
                        }
                    }
                }
                if (j % Sinal.QUANTIDADEAMOSTRAPORSINAL == 0) {
                    recuperado[posBit++] = (byte) ((contadorDePicos >= LIMIAR) ? 1 : 0);
                    //        System.err.printf("Recuperado[%d]:%d || Limiar: %d || Inicio %d\n", posBit - 1, recuperado[posBit - 1], contadorDePicos, i - Sinal.QUANTIDADEAMOSTRAPORSINAL);
                    contadorDePicos = 0;
                }
            }

        } catch (Exception e) {
        }
        
        recuperado[posBit] = (byte) ((contadorDePicos >= LIMIAR) ? 1 : 0);
        //System.err.printf("Recuperado[%d]:%d || Limiar: %d\n", posBit, recuperado[posBit], contadorDePicos);
        return recuperado;
    }
}

