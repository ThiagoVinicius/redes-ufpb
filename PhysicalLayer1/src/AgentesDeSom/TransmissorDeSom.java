/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package AgentesDeSom;

import Sinal.Sinal;
import UteisGraficos.GraficoTxt;
import UteisSom.Som;
import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Hugo e Janduy
 */
public class TransmissorDeSom{

    SourceDataLine linha = null;/*Linha por onde o sinal e lido da caixa de som*/

    //byte[] abData = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS];
    static byte[] bitSinalizador = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL*2];
    static byte[] abData = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL * Sinal.QUANTIDADEDESINAIS];
    static byte[] mensBitSinalizador = Sinal.converteStringParaBytes("11");/*COntem a mensagem do bit sinalizador*/


    
    public void run() {

        while (true) {
            try {
                /*01 Verifica na pilha de enlace se há alguma mensagem a ser transmitida*/
                Thread.sleep(1000);
                /*02 'Se Nao',
                aguarda algum tempo pra verificar novamente ou espera ser avisada por enlace*/
                /*03 'Se sim'
                constroi mensagem*/
                System.out.println("Transmissor enviando:");


                /**
                 *Tempo que a thread deve esperar para após o criaBitSinalizador
                 * antes de iniciar o envio do sinal */
                /*Teste(01):11 11101011 00000000 11 0K*/
                /*Teste(02):11 00110011 00110011 11 0K*/
                /*Teste(03):11 01010101 01010101 11 0k*/
                /*Teste(04):11 00000010 00000000 11 0k*/
                /*Teste(05):11 11101111 11111111 11 0k*/
                //11111011111111111111                              11111010110000000011
                constroiOndaCompleta(Sinal.converteStringParaBytes("11111010110000000011"), abData);
                criaBitSinalizador(TransmissorDeSom.mensBitSinalizador, bitSinalizador);//110110001101011011000110101011111111111011101110111


                byte[] m = montaSinal(bitSinalizador,400, abData);

                GraficoTxt.escreveOndaTxt(m, "onda.enviada.txt");
                enviaSom(m);                               //  110110001101011011000110101011111111111011101110111


                // linha.drain();
            /*02 'Se Nao',
                aguarda algum tempo pra verificar novamente ou espera ser avisada por enlace*/
                /*03 'Se sim'
                constroi mensagem*/
            } catch (InterruptedException ex) {
                Logger.getLogger(TransmissorDeSom.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    }

    /*******
     *
     * criaBitSinalizador(): escreve o sinal de start a ser transmitido de
     *                       acordo com "mens" no vetor "start"
     *
     * Argumentos: mens(entrada) - sequencia de start a ser transmitido
     *             start(saida)  - sinal de start a ser transmitido
     *
     * Retorno: nenhum
     *
     ****/
    public void criaBitSinalizador(byte[] mens, byte[] start) {
        constroiOndaCompleta(mens, start);
        /*Aqui embaixa deve ser bitSinalizador.lengthdado o atraso para iniciar a escrita*/
    }

    /********
     * constroiOndaCompleta():
     *                   escreve uma onda senoidal(em onda) completa com
     *                   frequencias alternando ao longo da onda de acordo com
     *                   a sequencia de bits fornecidas por mens.
     *
     * Parametros: mens(entrada): sequencia de bits a serem transmitidos
     *             onda(saida): seguencia de bits representando onda senoidal
     *                          portando a sequencia de bits de mens
     *
     * Rertorno: nenhum
     *
     **********/
    public void constroiOndaCompleta(byte[] mens, byte[] onda) {
        int comprimento = mens.length;
        for (int contagem = 0; contagem < comprimento; contagem++) {
            if (mens[contagem] == 1) {
                Sinal.escreveUm(onda, contagem * Sinal.QUANTIDADEAMOSTRAPORSINAL);
            } else {
                Sinal.escreveZero(onda, contagem * Sinal.QUANTIDADEAMOSTRAPORSINAL);
            }
        }
    }

    /*******
     *
     * montaSinal(): concatena em um unico array de bytes a onda a ser transmi-
     *               tida, incluindo o bit inicializador, um delay(silenciao)
     *               e a mensaegm de bits.
     *
     * Argumentos: bitSinalizador(entrada): bit de alerta de inicializacao de
     *                                      captacao.
     *
     *             delay(entrada): espaco de silenciao entre o bit de alerta
     *                             e a transmissao da mensagem. isso ocorre pa-
     *                             ra dar tempo o captador de som inicializar
     *                             a capturra.
     *
     * Retorno:    sinal: Um sinal contendo todas a informacoes anteriores em
     *                    sequencia, pronto para ser enviado.
     *
     ****/
    public static byte[] montaSinal(byte[] bitSinalizador, int delay, byte[] messagem) {
        byte[] sinal = new byte[bitSinalizador.length + delay + messagem.length];

        int i;
        for (i = 0; i < bitSinalizador.length; i++) {
            sinal[i] = bitSinalizador[i];
        }

        int j;
        for (j = 0; j < delay; j++) {
            sinal[i + j] = (byte) 0;
        }

        i = i + j;

        for (j = 0; j < messagem.length; j++) {
            sinal[i + j] = messagem[j];
        }


        return sinal;
    }

    /*******
     *
     * enviaSom(): envia pela saida de som a onda contida no array de recebido
     *             como argumento
     *
     * Argumentos: onda(entrada) - sinal a ser transmitido
     *
     * Retorno: nenhum
     *
     ****/
    public void enviaSom(byte[] onda) {
        linha = Som.openLineToWrite(48000, 8, 1, true, false);
        linha.write(onda, 0, onda.length);
        linha.drain();
        linha.close();
    }

    private byte[] converteInterlayerDataParaBytes(InterlayerData data)
    {
       int comprimento = Sinal.QUANTIDADEDESINAIS;

        byte[] resultado = new byte[comprimento];

        for (int i = 0; i < comprimento; i++) {
            if (data.getBit(i)) {
                resultado[i] = 1;
            } else {
                resultado[i] = 0;
            }
        }

        return resultado;
    }

   
    public void enviaMensagem(InterlayerData data) {

        constroiOndaCompleta(converteInterlayerDataParaBytes(data), abData);
        criaBitSinalizador(TransmissorDeSom.mensBitSinalizador, bitSinalizador);

        byte[] m = montaSinal(bitSinalizador, 400, abData);

        //GraficoTxt.escreveOndaTxt(m, "onda.enviada.txt");
        enviaSom(m);                           

    }
}
