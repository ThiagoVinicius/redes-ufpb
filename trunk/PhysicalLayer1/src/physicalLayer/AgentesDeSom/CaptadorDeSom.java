/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physicalLayer.AgentesDeSom;

import physicalLayer.Sinal.Sinal;
import physicalLayer.UteisGraficos.GraficoTxt;
import physicalLayer.UteisSom.Som;
import javax.sound.sampled.TargetDataLine;


public class CaptadorDeSom{

    TargetDataLine linha = null;/*Linha por onde o sinal e lido da caixa de som*/

    static byte[] bitSinalizador = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * 3];
    static byte[] dados = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS + Sinal.AMAIS];
    int numeroDeBytesLidos;
    static byte silencio = 15;/*Acima disso vai ser considarado como um pico*/

    static int MINIMO_POR_BIT_SINALIZADOR = 3;/*Regular*/

    static int PICO = 12;/*Usado na funcao de recuperacao*/

    static int LIMIAR = 8;/*Usado de frequencia*/


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
        max = 0;
        /*00 Estao inicial:ler meio*/
        // System.out.println("CAPTADOR:" + System.currentTimeMillis());
        numeroDeBytesLidos = linha.read(bitSinalizador, 0, bitSinalizador.length);
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
            // System.out.println("max:" + max + ",contadores:" + contadodorDePicos+"bitSinalizador.length"+bitSinalizador.length);
            if (contadodorDePicos > MINIMO_POR_BIT_SINALIZADOR) {
                //System.out.println("CHAMA METODO DE LEITURA DE DADOS");
                /*2.2 Chama o metode de leitura de dados*/
                capturaSom();

                byte[] resultado = analizaSinal(dados, buscaInicioDaOnda(dados));
                System.out.print("Resultado: ");
                for (int i = 0; i < resultado.length; i++) {
                    System.out.printf("%d", resultado[i]);
                }
                System.out.println("");
                /*Lixooooooooooo*/
                System.out.println("max:" + max + ",contadores:" + contadodorDePicos + "bitSinalizador.length" + dados.length);

                GraficoTxt.escreveOndaTxt(dados, "OndaCaptada.txt");
                GraficoTxt.escreveOndaTxt(bitSinalizador, "BitSinalizadorCaptadado.txt");

                return resultado;

            }
        } catch (Exception ex) {
            ex.printStackTrace();            
            System.exit(0);
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
        int k = 0, pos;
        for (k = 180; k < sinalRecebido.length; k++) {
            // if (Math.abs(sinalRecebido[k]) > PICO) {
            if (sinalRecebido[k] > PICO) {

                int r = (sinalRecebido[k + 1] > sinalRecebido[k]) ? k + 1 : k - 1;
                GraficoTxt.bitsDoGrafo(dados, r);

                return r;
            }
        }
        return -1;/*Indicativo de erro*/
    }

    public byte[] analizaSinal(byte[] sinalRecebido, int offset) {
        byte[] recuperado = new byte[Sinal.QUANTIDADEDESINAIS];
        int ultimo = offset + Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS;


        System.err.println("Inicio: " + offset);
        System.err.println("Fim: " + ultimo);
        //System.err.println("sinalRecebido.length"+sinalRecebido.length);

        int contadorDePicos = 0;/*Sinal 0(positivo) 1*/
        int posBit = 0;
        contadorDePicos = 0;
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
                System.out.printf("Recuperado[%d]:%d || Limiar: %d || Inicio %d\n", posBit - 1, recuperado[posBit - 1], contadorDePicos, i - Sinal.QUANTIDADEAMOSTRAPORSINAL);
                contadorDePicos = 0;
            }
        }
        recuperado[posBit] = (byte) ((contadorDePicos >= LIMIAR) ? 1 : 0);
        System.out.printf("Recuperado[%d]:%d || Limiar: %d\n", posBit, recuperado[posBit], contadorDePicos);
        return recuperado;
    }
}

