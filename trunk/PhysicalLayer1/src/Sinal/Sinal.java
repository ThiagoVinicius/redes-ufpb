package Sinal;

/*
 * Sinal.java
 *
 * Created on May 31, 2009, 10:14 PM
 *
 * Classe responsavel pela criacaoo e manipulacao dos sinais gerados pela camada fisica.
 * 
 */
public class Sinal {

    /****
     * BARULHOMAXIMO: Constante que define o limite maximo no qual a deteccao
     * de colisao ocorre.
     * */
    public static final byte BARULHOMAXIMO = 100; /*O intervalo eh  0 ate 127*/

    public static final byte AMPLITUDE = 127; /*O intervalo eh  0 ate 127*/

    /**
     * QUANTIDADEDESINAIS:Constante contendo o valor do buffer do daemon.
     * Será o numero de bits trasnnitidos inclino preambulo e posambulo e faixa
     * de erro
     *
     ****/

    //TODO mudar valor para 16
    public static final int QUANTIDADEDESINAIS = 20;
    /****
     * AMAIS: refere-se a jenela de amostras a mais captadas durante a
     *        recepcão.
     *        OBS: Precisa ser melhorada e adequada a cada maquina
     ****/
    public static final int AMAIS = 1000;
    /****
     * QUANTIDADEAMOSTRAPORSINAL:numero de amostras usadas para representar cada
     *        bit.   Dev ser sempre multiplo de 48;
     ****/
    public static final int MULTIPLO = 48;
    public static final int QUANTIDADEAMOSTRAPORSINAL = MULTIPLO;
    /**
     * ZERO,UM: refere-se as frequencias usadas para representar cada bit.
     *
     ***/
    private static final int ZERO = 2;
    private static final int UM = 6;

    /**
     * escreveOndaSenoide(): escreve uma onda senoide. O valor que determina
     *                      quem vai parar no zero é o angulo e nao o tamanho
     *                      do arranjo.(Nao entendi isso.Portanto, o valor do
     *                      angulo vezes a quantidade de amostras no arranjo
     *                      for multiplo de 180 é valido, porém para garantir
     *                      que sempre o sinal(da onda) sempre começa crescente
     *                      e termina crescente temos que ter uma multiplidade
     *                      por 360(para a funcao seno).Paragarantir a "pureza"
     *                      do sinal utilizamos a segunda opcao.
     *
     * Argumentos:
     *          simbolo(saida) - deve ter tamanho multiplo de 36. Simbolos
     *                          a serem transmitidos
     *
     *          frequencia(entrada) - valor do tipo inteiro que determina a
     *                             frequencia da onda.Se a frequencia for duas
     *                             vezes maior que uma dada frequencia a quanti-
     *                             dade de cristas da onda sera duas vezes maior.
     *
     *          angulo(entrada): por enquanto o angulo sempre sera multiplo
     *                         de 10! Nao pensei ainda em valores diferentes destes.
     *
     *          (entrada): determina a amplitude do sinal
     *

     *
     ***/
    public static void escreveOndaSenoide(byte[] simbolo, int offset, float frequencia,
            byte volume) {
        if ((simbolo.length) % Sinal.MULTIPLO != 0) {
            System.out.println("ERRO!! A funcao escreve nao representara uma senoide completa!");
        }

        double anguloEmRadiano = 2 * frequencia * Math.PI / Sinal.QUANTIDADEAMOSTRAPORSINAL;

        /*Simplificacao da equacao: (360/fisica.QUANTIDADEAMOSTRAPORSINAL)*frequencia*angulo*Math.PI/180;*/
        for (int i = 0; i < Sinal.QUANTIDADEAMOSTRAPORSINAL; i++) {
            // System.err.printf("offset: %d,simbolo[%d]: %d:\n",offset,i,simbolo[i]);
            simbolo[offset + i] = (byte) (Math.sin(i * anguloEmRadiano) * volume);
        }
    }

    /********
     * Obs.: Nos dois metodos que se sucedem pode haver um melhoramento na
     * frequencia ou a criacao de mais de dois niveis de intensidades(transmitir
     * dois bits de ma vez ), o que iria melhorar a taxa de transferencia.
     *************/
    /****
     *
     * escreveZero():  escreve um "bit zero" nao onda
     *
     * Argumentos:
     *
     * Retorno:
     *
     ****/
    public static void escreveZero(byte[] simbolo, int offset) {
        escreveOndaSenoide(simbolo, offset, ZERO, AMPLITUDE);
    }

    /****
     *
     * escreveUm():  escreve um "bit zero" nao onda
     *
     * Argumentos:
     *
     * Retorno:
     *
     ****/
    public static void escreveUm(byte[] simbolo, int offset) {
        escreveOndaSenoide(simbolo, offset, UM, AMPLITUDE);
    }

    public static byte[] converteStringParaBytes(String a) {
        int comprimento = a.length();
        byte[] resultado = new byte[comprimento];
        for (int i = 0; i < comprimento; i++) {
            if (a.charAt(i) == '1') {
                resultado[i] = 1;
            } else {
                resultado[i] = 0;
            }
        }
        return resultado;
    }
}
