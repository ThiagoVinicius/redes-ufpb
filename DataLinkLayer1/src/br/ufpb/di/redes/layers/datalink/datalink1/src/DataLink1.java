/**
 * Implementacao da camada DataLink1.
 *
 * Camada em arquitetura de Token Ring, que define 2 quadros:
 * Quadro de Permissão e Endereçamento (Token) - será criado assim que a rede
 * for iniciada por um dos enlaces (de mac 0) e é o responsável por definir
 * quem poderá enviar mensagens, além de dar algumas informações importantes.
 * Este tipo de quadro sempre possuirá controle no formato "01".
 * O quadro possui 16 bits no seguinte formato: CCOOODDDXXbpcccc
 *
 * Quadro de Dados - criado sempre que algum enlace for enviar mensagem, pode
 * ter 2 valores de controle: "10" para dados intermediários e "11" para um
 * quadro de dados final.
 * O quadro possui 16 bits no seguinte formato: CCddddddddXXcccc
 *
 * Legenda: C - bit de controle de quadro
 *          O - endereço de origem
 *          D - endereço de destino
 *          X - bit de preenchimento
 *          b - bit de dados (indica que os próximos quadros conterão dados)
 *          p - bit de permissão
 *          c - CRC
 *          d - dados
 *
 *
 * @author Amanda Barreto Cavalcanti
 * @author Gutenberg Pessoa Botelho Neto
 */

package br.ufpb.di.redes.layers.datalink.datalink1.src;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLink1 extends DataLink {
    /**
     * Instância para logging.
     */
    private static final Logger logger = LoggerFactory.getLogger(DataLink1.class);
    
    /**
     * Valor do primeiro mac atribuido na rede, sera utilizado para definir
     * o enlace responsavel pela criacao do token.
     */
    private static final int PRIMEIROMAC = 0;
    
    /**
     * Quantidade de bits especificos para dados contidos em um quadro de dados.
     */
    private static final int BITSDADOS = 8;

    /**
     * Tamanho dos quadros de dados e de permissao e endereçamento e dos quadros
     * sem CRC.
     */
    private static final int TAMQUADRODEDADOS = 16;
    private static final int TAMQUADROPERMISSAOENDERECAMENTO = 16;
    private static final int TAMMENSAGEMSEMCRC = 12;
    
    /**
     * Valores de campos de controle de quadro para os 3 tipos diferentes
     * de quadro possíveis.
     */
    private static final int CTRLQUADROPERMISSAOEENDERECAMENTO = 1;
    private static final int CTRLQUADRODEDADOSINTERMEDIARIO = 2;
    private static final int CTRLQUADRODEDADOSFINAL = 3;

    /**
     * Tamanho do MAC e dos campos de controle de quadro e CRC.
     */
    private static final int TAMCONTROLE = 2;
    private static final int TAMMAC = 3;
    private static final int TAMCRC = 4;

    /**
     * Armazenará os quadros recebidos pertencentes a determinada mensagem
     * até que o último seja recebido, para que possam ser unidos.
     */
    private ArrayList<InterlayerData> mensagemSentAtual;
    private ArrayList<InterlayerData> mensagemReceivedAtual;

    /**
     * Variáveis que indicam se o corrente enlace está em algum estado especial
     * (pronto para enviar mensagem, para receber mensagem, ou descartando
     * quadros).
     */
    boolean mensagemASerEnviada;
    boolean mensagemASerRecebida;
    boolean descartaQuadros;

    Semaphore s1, s2; // para a sincronizacao - provavelmente nao vai dar certo! =P
    
    /** Campo referente ao MAC deste enlace. */
    private int mac;

    /** Thread utilizada para testar o timeout da passagem de token */
    private Thread monitorToken;

    /** Tempo maximo (em segundos) que o enlace pode passar sem receber token */
    private final double timeout = 15;

    /** Utilizados para a verificacao do timeout */
    private double tempoAtual;
    private double tempoUltimoToken;

    public DataLink1 (Physical downLayer, int id, int mac) {
        super(downLayer, id);
        this.mac = mac;
        logger.info("Enlace criado com id " + id + " e MAC " + mac + ".");
        s1 = new Semaphore(0);
        s2 = new Semaphore(0);
        mensagemSentAtual = new ArrayList<InterlayerData>();
        mensagemReceivedAtual = new ArrayList<InterlayerData>();
        mensagemASerEnviada = false;
        mensagemASerRecebida = false;
        descartaQuadros = false;
        tempoAtual = tempoUltimoToken = 0;
    }

    /**
     * Retorna a Thread que sera utilizada para monitorar a passagem de token
     * neste enlace
     * @param enlace referencia para o enlace atual
     * @return a thread desejada
     */
    private Thread getMonitorToken(DataLink1 enlace) {
        final DataLink1 enlaceInterno = enlace;
        return new Thread() {
            public void run(){
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        logger.warn("Excecao em thread de monitor de token.");
                    }
                    enlaceInterno.checaPassagemDeToken();
                }
            }
        };
    }

    /**
     * Utilizado para verificar a última vez em que um token foi recebido
     * ou enviado por este enlace.
     */
    private void checaPassagemDeToken() {
        this.tempoAtual = System.currentTimeMillis();
        //logger.info("DIFERENCA: {}", ((this.tempoAtual - this.tempoUltimoToken) / 1000));
        if (((this.tempoAtual - this.tempoUltimoToken) / 1000) > this.timeout) {
            logger.info ("Erro de timeout, enviando novo token.");
            mensagemReceivedAtual.clear();
            mensagemASerRecebida = false;
            descartaQuadros = true;
            this.tempoUltimoToken = this.tempoAtual;
            bubbleDown(criaTokenInicial());
        }
    }

    /**
     * Método para calcular o CRC de 4 bits a partir de dados com 12 bits
     * de comprimento. Se a mensagem tiver mais de 12 bits, será avaliado o
     * CRC dos 12 primeiros.
     * @param dados dados a partir dos quais será calculado o CRC.
     * @return valor do CRC.
     */
    private int calculaCRC4 (InterlayerData dados) {
        /** Se a mensagem nao tem tamanho suficiente, não é calculado o CRC. */
        if (dados.length < TAMMENSAGEMSEMCRC) {
            logger.warn("Mensagem com tamanho errado para o calculo de CRC.");
            logger.warn("Retornando 0");
            return 0;
        }

        /**
         * Cria-se inicialmente uma mensagem auxiliar contendo os 12 bits
         * de dados a partir dos quais se calculará o CRC. Os bits seguintes
         * são preenchidos com 0.
         */
        InterlayerData aux = new InterlayerData(TAMMENSAGEMSEMCRC + TAMCRC);
        aux.putInfo(0, TAMMENSAGEMSEMCRC, dados.takeInfo(0, TAMMENSAGEMSEMCRC));

        /**
         * O polinomio gerador utilizado é o x^4 + x + 1 = 0b10011. O bit
         * mais significativo, porém, não é necessário, por conta disso,
         * será representado como 0b0011 = 3.
         */
        int polinomioGerador = 3;

        int infoAtual = 0;
        for (int i = 0; i < TAMMENSAGEMSEMCRC; i++) {
            /**
             * O XOR deve ser efetuado quando o bit da posição atual
             * (que é o mais significativo, já que os anteriores são zerados
             * a cada etapa) for 1.
             */
            if (aux.getBit(i)) {
                infoAtual = aux.takeInfo(i+1, TAMCRC);
                infoAtual ^= polinomioGerador;
                aux.clearBit(i);
                if (i != (TAMMENSAGEMSEMCRC -1)) {
                    aux.putInfo(i+1, TAMCRC, infoAtual);
                }
            }
        }
        /** O CRC estará nos últimos 4 bits de aux. */
        //logger.debug("CRC calculado: " + aux.takeInfo(aux.length-TAMCRC, TAMCRC));
        return aux.takeInfo(aux.length-TAMCRC, TAMCRC);
    }

    /**
     * Método para criar um quadro de dados.
     * @param controle valor do campo de controle deste quadro (2 ou 3)
     * @param dados serao encapsulados no quadro.
     * @return quadro contendo os dados, o controle e o CRC.
     */
    private InterlayerData criaQuadroDeDados (int controle,
                    InterlayerData dados) {
        InterlayerData aux = new InterlayerData (TAMQUADRODEDADOS);
        if (dados.length != BITSDADOS) {
            logger.warn("Recebido um quadro com tamanho errado.");
            logger.warn("Nao foi possivel criar o quadro de dados.");
            logger.warn("Retornando um quadro nulo!");
            return null;
        }

        logger.info("Criando quadro de dados com controle " + controle + ".");
 
        defineControle(controle, aux);
        /** Armazena os dados no quadro */
        aux.putInfo(TAMCONTROLE, BITSDADOS, dados.takeInfo(0, BITSDADOS));
        int CRC = calculaCRC4(aux);
        /** Armazena o CRC nos últimos bits do quadro */
        aux.putInfo(aux.length-TAMCRC, TAMCRC, CRC);

        return aux;
    }

    /**
     * Método para criar o campo de permissão e endereçamento.
     * @param dest_mac MAC de destino para o token atual.
     * @param bitDeDados diz se o proximo quadro contém dados.
     * @param bitDePermissao diz se o token esta livre.
     * @return quadro contendo o controle (1), MAC de origem e destino,
     * bit de dados, bit de permissao e CRC.
     */ 
    private InterlayerData criaQuadroDePermissaoEEnderecamento (int dest_mac,
                    int bitDeDados, int bitDePermissao) {
        logger.info("Criando token com:");
        logger.info("\tMAC de Destino: " + dest_mac);
        logger.info("\tBit de Dados: " + bitDeDados);
        logger.info("\tBit de Permissão: " + bitDePermissao);
        InterlayerData aux = new InterlayerData(TAMQUADROPERMISSAOENDERECAMENTO);
        defineControle(CTRLQUADROPERMISSAOEENDERECAMENTO, aux);
        /** Armazena o MAC de origem no quadro */
        aux.putInfo(TAMCONTROLE, TAMMAC, mac);
        /** Armazena o MAC de destino */
        aux.putInfo(TAMCONTROLE+TAMMAC, TAMMAC, dest_mac);
        /** Armazena os bits de dados e permissão */
        aux.putInfo(aux.length-(TAMCRC+2), 1, bitDeDados);
        aux.putInfo(aux.length-(TAMCRC+1), 1, bitDePermissao);
        int CRC = calculaCRC4(aux);
        /** Armazena o CRC nos últimos bits do quadro */
        aux.putInfo(aux.length-TAMCRC, TAMCRC, CRC);

        return aux;
    }

    /**
     * Método para criar diversos quadros de dados, cada um contendo 8 bits
     * de dados, com 2 bits para controle de quadro e 4 bits para CRC.
     * Para que todos os quadros criados possam ser depois reunidos na mensagem
     * original sem problemas, é necessário que a mensagem original tenha uma
     * quantidade de bits múltipla de 8.
     * @param dados serao fragmentados e encapsulados em quadros.
     * @return ArrayList contendo todos os quadros criados por este método.
     */
    private ArrayList<InterlayerData> criaQuadrosDeDados (InterlayerData dados) {
        logger.info("Encapsulando mensagem de tamanho " + dados.length);
        if (dados.length < BITSDADOS || dados.length % BITSDADOS != 0) {
            logger.warn("Mensagem recebida com tamanho errado.");
            logger.warn("Não será possível criar os quadros, retornando nulo");
            return null;
        }
        ArrayList<InterlayerData> quadros = new ArrayList<InterlayerData>();
        InterlayerData aux;
        for (int i = 0; i < dados.length; i += BITSDADOS) {
            aux = new InterlayerData (BITSDADOS);
            aux.putInfo(0, BITSDADOS, dados.takeInfo(i, BITSDADOS));
            aux = criaQuadroDeDados ((i + BITSDADOS < dados.length) ? 
                CTRLQUADRODEDADOSINTERMEDIARIO : CTRLQUADRODEDADOSFINAL, aux);
            
            /**
             * Se não for possível criar o quadro de dados, é retornada uma
             * lista nula que, obviamente, não será enviada pela rede.
             */
            if (aux == null) {
                return null;
            }
            logger.debug("Quadro " + aux + " criado");
            quadros.add(aux);
        }

        logger.info("Retornando " + quadros.size() + " quadros de dados.");
        return quadros;
    }

    /**
     * Método para criar o token inicial da rede.
     * @return o token
     */
    private InterlayerData criaTokenInicial () {
        /**
         * O token inicial tem bit de dados 0 (pois ainda nao há mensagem
         * a ser enviada) e bit de permissão 1 pois está livre
         */
        return criaQuadroDePermissaoEEnderecamento(PRIMEIROMAC, 0, 1);
    }

    /**
     * Método para colocar nos dois bits iniciais de um quadro o valor de
     * seu controle.
     * @param controle valor a ser colocado no quadro.
     * @param quadro que será modificado.
     */
    private void defineControle (int controle, InterlayerData quadro) {
        if (quadro.length < TAMCONTROLE || (controle != CTRLQUADRODEDADOSFINAL &&
                                controle != CTRLQUADRODEDADOSINTERMEDIARIO &&
                                controle != CTRLQUADROPERMISSAOEENDERECAMENTO)) {

            logger.warn("Não foi possível atribuir o valor de controle ao quadro.");
            return;
        }

        quadro.putInfo(0, TAMCONTROLE, controle);
    }

    /**
     * Retira os bits inseridos pelo enlace neste quadro para que apenas
     * os dados relevantes à camada de rede possam ser repassados.
     * @param quadro criado pelo enlace, de onde serao retiradas informacoes
     * irrelevantes para as camadas superiores.
     * @return dados sem os campos inseridos pelo enlace.
     */
    private InterlayerData desenquadra (InterlayerData quadro) {
        if (quadro.length != TAMQUADRODEDADOS) {
            logger.warn("Recebido quadro de dados com tamanho errado.");
            logger.warn("Não é possível desenquadrar.");
            logger.warn("Retornando o quadro sem modificações.");
            return quadro;
        }
        InterlayerData aux = new InterlayerData(BITSDADOS);
        aux.putInfo(0, BITSDADOS, quadro.takeInfo(TAMCONTROLE, BITSDADOS));
        
        return aux;
    }

    /**
     * Método getter para a variável mac.
     * @return MAC do enlace.
     */
    public int getMac() {
        return mac;
    }

    /**
     * Retorna o valor do bit de dados em um token.
     * @param token a ser avaliado.
     * @return bit de dados.
     */
    public boolean getBitDeDadosToken (InterlayerData token) {
        if (token.takeInfo(TAMQUADROPERMISSAOENDERECAMENTO-(TAMCRC+2), 1) == 1)
            return true;
        else return false;
    }

    /**
     * Retorna o valor do bit de permissão em um token.
     * @param token a ser avaliado.
     * @return bit de permissão.
     */
    public boolean getBitDePermissaoToken (InterlayerData token) {
        if (token.takeInfo(TAMQUADROPERMISSAOENDERECAMENTO-(TAMCRC+1), 1) == 1)
            return true;
        else return false;
    }

    /**
     * Retorna o valor do controle do quadro.
     * @param dados quadro cujo controle será retornado.
     * @return controle.
     */
    public int getControle(InterlayerData dados) {
        return dados.takeInfo(0, TAMCONTROLE);
    }

    /**
     * Retorna o MAC de destino de um token.
     * @param token a ser avaliado.
     * @return MAC de destino.
     */
    public int getMACDestino (InterlayerData token) {
        return token.takeInfo(TAMCONTROLE+TAMMAC, TAMMAC);
    }

    /**
     * Retorna o MAC de origem de um token.
     * @param token a ser avaliado.
     * @return MAC de origem.
     */
    public int getMACOrigem (InterlayerData token) {
        return token.takeInfo(TAMCONTROLE, TAMMAC);
    }

    /**
     * Método getter do name deste enlace.
     * @return nome do enlace, contendo seu MAC.
     */
    public String getName () {
        return "Enlace de MAC " + mac;
    }
    
    /**
     * Método que, a partir de uma lista de quadros de dados compondo uma
     * mensagem fragmentada, recupera a mensagem original.
     * @param msg ArrayList contendo os quadros de dados.
     * @return a mensagem original.
     */
    private InterlayerData recuperaMensagem (ArrayList<InterlayerData> msg) {
        if (msg.size() == 0) {
            logger.warn("Array vazio recebido para recuperação de mensagem.");
            logger.warn("Retornando referência nula.");
            return null;
        }
        InterlayerData aux = new InterlayerData(msg.size() * BITSDADOS);

        for (int i = 0; i < msg.size(); i++) {
            aux.putInfo(i*BITSDADOS, BITSDADOS,
                    desenquadra(msg.get(i)).takeInfo(0, BITSDADOS));
        }

        logger.info("Mensagem recuperada com tamanho " + aux.length);
        logger.debug("Mensagem recuperada: " + aux);

        return aux;
    }

    /**
     * Verifica se o CRC contido em determinado quadro está correto.
     * @param quadro cujo CRC será avaliado.
     * @return valor booleano dizendo se o CRC está correto (true) ou não.
     */
    private boolean verificaCRC(InterlayerData quadro) {
        return calculaCRC4(quadro) ==
                quadro.takeInfo(quadro.length - TAMCRC, TAMCRC);
    }

    @Override
    public void start() {
        super.start();

        logger.info("Enlace de MAC " + mac + " iniciado.");
        if (mac == PRIMEIROMAC) {
            this.tempoUltimoToken = System.currentTimeMillis();
            bubbleDown(criaTokenInicial());
            logger.info("Token enviado.");
            monitorToken = getMonitorToken(this);
            monitorToken.start();
        }
    }

    @Override
    protected void bubbleDown(InterlayerData data) {
        logger.debug("Enviando mensagem para a Fisica: {}", data);
        super.bubbleDown(data);
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_mac) {
        logger.info("Mensagem " + data + " recebida da camada de Rede.");
        mensagemSentAtual = criaQuadrosDeDados(data);
        if (mensagemSentAtual == null || mensagemSentAtual.size() == 0) {
            logger.warn("Problema na mensagem recebida da Rede.");
            logger.warn("Mensagem descartada.");
            return;
        }

        mensagemASerEnviada = true;
        try {
            logger.info("Aguardando autorização para envio de mensagem.");
            s1.acquire();
        } catch (InterruptedException ex) {
            logger.error("Excecao lancada em processSentData ", ex);
        }
        
        /** Envia na frente um token relativo à mensagem atual. */
        InterlayerData token = criaQuadroDePermissaoEEnderecamento(dest_mac, 1, 0);
        this.tempoUltimoToken = System.currentTimeMillis();
        bubbleDown(token);
        /** Envia cada um dos quadros de dados. */
        for (int i = 0; i < mensagemSentAtual.size(); i++)
            bubbleDown(mensagemSentAtual.get(i));
        mensagemASerEnviada = false;
        s2.release();
    }

    @Override
    protected void processReceivedData(InterlayerData data) {
        logger.info("Mensagem recebida da camada Física: {}.", data);
        if (data.length != TAMQUADRODEDADOS &&
            data.length != TAMQUADROPERMISSAOENDERECAMENTO) {
            logger.warn("Mensagem com tamanho incorreto será descartada.");
            return;
        }

        int controle = getControle(data);

        if (descartaQuadros && controle != 1) {
            return;
        }

        if (!verificaCRC(data) && controle == 1) {
            logger.warn ("Erro de CRC, enviando novo token.");
            mensagemReceivedAtual.clear();
            mensagemASerRecebida = false;
            descartaQuadros = true;
            this.tempoUltimoToken = System.currentTimeMillis();
            bubbleDown(criaTokenInicial());
            return;
        }

        if (controle == CTRLQUADROPERMISSAOEENDERECAMENTO) {
            this.tempoUltimoToken = System.currentTimeMillis();

            /** Sai do modo de descartar quadros se esse token estiver correto. */
            descartaQuadros = false;

            if (mensagemASerRecebida) {
                mensagemASerRecebida = false;
                mensagemReceivedAtual.clear();
            }

            /**
             * Se o enlace não tem nenhuma mensagem a enviar e o
             * token atual não tem nenhuma mensagem para este enlace,
             * devolve o token à camada física.
             */
            if (!mensagemASerEnviada &&
                (!getBitDeDadosToken(data) || (getMACDestino(data) != mac))) {
                logger.info("Repassando token que nao interessa a este enlace.");
                bubbleDown(data);
                return;
            }

            /**
             * Se o token atual contém mensagem para este enlace, prepara-se
             * para recebê-la.
             */
            if (getBitDeDadosToken(data) && (getMACDestino(data) == mac)
                    && !(getBitDePermissaoToken(data))) {
                if (mensagemASerRecebida) mensagemReceivedAtual.clear();
                mensagemASerRecebida = true;
                logger.info("Token recebido que há mensagem para este enlace.");
                return;
            }

            /**
             * Se o enlace tem mensagem a ser enviada e o token está livre,
             * libera a thread de envio.
             */
            if (mensagemASerEnviada && getBitDePermissaoToken(data)) {
                logger.info("Ocupado o token para envio de mensagem.");
                s1.release();
                try {
                    /**
                     * Garante que só retornará do método após a mensagem
                     * ter sido enviada.
                     */
                    s2.acquire();
                } catch (InterruptedException ex) {
                   logger.error("Excecao lancada em processReceivedData ", ex);
                }
                return;
            } else if (mensagemASerEnviada && !getBitDePermissaoToken(data))
                bubbleDown(data);
        } else if (controle == CTRLQUADRODEDADOSINTERMEDIARIO) {
            /**
             * Se nao há mensagem para ser recebida, este quadro de dados
             * não interessa.
             */
            if (!mensagemASerRecebida) {
                logger.info("Mandando para baixo quadro de dados que não interessa.");
                bubbleDown(data);
                return;
            }

            if (!verificaCRC(data)) {
                logger.warn("Erro nos dados recebidos. Descartando..");
                mensagemASerRecebida = false;
                mensagemReceivedAtual.clear();
                descartaQuadros = true;
                bubbleDown(criaTokenInicial());
                return;
            }
            
            logger.info("Armazenando quadro de dados intermediário (" + data + ").");
            mensagemReceivedAtual.add(data);
            return;
        } else if (controle == CTRLQUADRODEDADOSFINAL) {
            /**
             * Se nao há mensagem para ser recebida, este quadro de dados
             * não interessa.
             */
            if (!mensagemASerRecebida) {
                logger.info("Mandando para baixo quadro de dados que não interessa.");
                bubbleDown(data);
                return;
            }

            if (!verificaCRC(data)) {
                logger.warn("Erro nos dados recebidos. Descartando..");
                mensagemASerRecebida = false;
                mensagemReceivedAtual.clear();
                descartaQuadros = true;
                bubbleDown(criaTokenInicial());
                return;
            }

            mensagemReceivedAtual.add(data);
            logger.info("Quadro de dados final recebido. (" + data + "). Recuperando mensagem...");
            InterlayerData msg = recuperaMensagem(mensagemReceivedAtual);
            if (msg != null)
                bubbleUp(msg, mac);

            mensagemReceivedAtual.clear();
            mensagemASerRecebida = false;
            /**
             * Com a mensagem recebida, envia um novo token para a rede.
             */
            this.tempoUltimoToken = System.currentTimeMillis();
            bubbleDown(criaTokenInicial());

            /**
             * Com o token livre novamente, pode ser interessante já verificar
             * se há mensagem a ser enviada. Verei se isso é viável depois que
             * o enlace já estiver funcionando.
             */
//            if (mensagemASerEnviada) {
//                s1.release();
//                try {
//                    s2.acquire();
//                } catch (InterruptedException ex) {
//                   logger.error("Excecao lancada em processReceivedData ", ex);
//                }
//                return;
//            }
            return;
        }
    }

    @Override
    public int minPacketSize() {
        return 8;
    }

    @Override
    public int maxPacketSize() {
        return 80;
    }
}
