/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.network.interfaces;

import br.ufpb.di.redes.layers.all.DefaultValues;
import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.all.Layer;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public abstract class Network extends Layer<Transport, DataLink> {

    /**Tamanho do buffer de mensagens vindas da camada de cima*/
    private static int sendBufferSize = DefaultValues.DOWN_BUFFER_SIZE;

    /**Tamanho do buffer de mensagens vindas da camada de baixo*/
    private static int receivedBufferSize = DefaultValues.DOWN_BUFFER_SIZE;

    /**Logger para esta classe*/
    private static final Logger logger = LoggerFactory.getLogger(Network.class);

    /**Usado para armazenar mensagens vindas da camada de transporte, no buffer*/
    private static class ToSendMessage {
        public final InterlayerData data;
        public final int dest_ip;
        public ToSendMessage(InterlayerData data, int dest_ip) {
            this.data = data;
            this.dest_ip = dest_ip;
        }
    }

    /**Usado para armazenar mensagens vindas da camada de enlace, no buffer*/
    private static class ToReceiveMessage {
        public final InterlayerData data;
        public final int source_mac;
        public final int datalink_id;
        public ToReceiveMessage(InterlayerData data, int source_mac, int datalink_id) {
            this.data = data;
            this.source_mac = source_mac;
            this.datalink_id = datalink_id;
        }
    }

    /**Buffer de mensagens recebidas da camada de cima.*/
    private ArrayBlockingQueue<ToSendMessage> sendBuffer;

    /**Buffer de mensagens recebidas da camada de baixo.*/
    private ArrayBlockingQueue<ToReceiveMessage> receivedBuffer;

    /**Thread que processa mensagens recebidas da camada de cima.*/
    private Thread sendThread;

    /**Thread que processa mensagens recebidas da camada de baixo.*/
    private Thread receivedThread;

    protected DataLink downLayer[]; //propositalmente, oculta um campo da super classe

    public Network(DataLink downLayers[]) {
        super(null);
        this.downLayer = Arrays.copyOf(downLayers, downLayers.length);
        sendBuffer = new ArrayBlockingQueue<ToSendMessage>(sendBufferSize);
        receivedBuffer = new ArrayBlockingQueue<ToReceiveMessage>(receivedBufferSize);
    }

    /**
     * Define a quantidade de mensagens em receivedBuffer.
     * <p/>
     * So deve ser usado durante a fase de configuracao do sistema.
     *
     * @param newSize novo tamanho do buffer. Deve ser maior que zero.
     */
    public static void setReceivedBufferSize(int newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("newSize deve ser maior que 0! " +
                    "newSize = " + newSize );
        }
        Network.receivedBufferSize = newSize;
    }

    /**
     * Define a quantidade de mensagens em sendBuffer.
     * <p/>
     * So deve ser usado durante a fase de configuracao do sistema.
     *
     * @param newSize novo tamanho do buffer. Deve ser maior que zero.
     */
    public static void setSendBufferSize(int newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("newSize deve ser maior que 0! " +
                    "newSize = " + newSize );
        }
        Network.sendBufferSize = newSize;
    }

    /**
     * Esvazia o buffers e inicia as thread de recebimento e envio de mensagens.
     */

    @Override
    public void start () {
        logger.info("Iniciando servico");
        sendBuffer.clear();
        sendThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        ToSendMessage tmp = sendBuffer.take();
                        processSentData(tmp.data, tmp.dest_ip);
                    }
                } catch (InterruptedException e) {
                }
                logger.debug("Thread de recebimento interrompida.");
            }

        };

        receivedBuffer.clear();
        receivedThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        ToReceiveMessage tmp = receivedBuffer.take();
                        processReceivedData(tmp.data, tmp.source_mac, tmp.datalink_id);
                    }
                } catch (InterruptedException e) {
                }
                logger.debug("Thread de envio interrompida.");
            }

        };

        sendThread.start();
        receivedThread.start();
    }

    /**
     * Armazena data e source_mac, se houver espaco imediatamente. Se nao
     * houver espaco imediatamente, bloqueia ate que haja espaco disponivel.
     * <p/>
     * Esta e' a interface publica que deve ser chamada a partir de camadas
     * superiores. Em geral, nao deve ser chamado diretamente o metodo
     * bubbleDown() normalmente se encarrega desta tarefa.
     *
     * @param data PDU a ser enviada.
     * @param dest_ip mac de destino.
     *
     * @throws IllegalStateException Sera lancada se nao houver entidade
     * amarrada ao topo desta entidade.
     */
    public void send(InterlayerData data, int dest_ip) {
        EntityState state = getState();
        if (state == EntityState.RUNNING) {
            logger.debug("Dados recebidos do transporte. Para a fila!");
            try {
                ToSendMessage tmp = new ToSendMessage(data, dest_ip);
                sendBuffer.put(tmp);
            } catch (InterruptedException e) {
                logger.error("Interrompido.", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Dados recebidos do transporte, porem em estado invalido: {}.", state);
            throw new IllegalStateException("Tentando repassar dados, mas " +
                    "a entidade esta em estado invalido: "+state.name());
        }
    }

    /**
     * Armazena data, se houver espaco imediatamente. Se nao
     * houver espaco imediatamente, bloqueia ate que haja espaco disponivel.
     * <p/>
     * Esta e' a interface publica que deve ser chamada a partir de camadas
     * inferiores. Em geral, nao deve ser chamado diretamente o metodo
     * bubbleUp() normalmente se encarrega desta tarefa.
     *
     * @param data PDU recebida.
     * @param source_mac Mac que originou a PDU
     * @param datalink_id identificacao da camada de enlace que executou a
     * chamada
     *
     * @throws IllegalStateException Sera lancada se nao houver entidade
     * amarrada ao topo desta entidade.
     */
    public void received(InterlayerData data, int source_mac, int datalink_id) {
        EntityState state = getState();
        if (state == EntityState.RUNNING) {
            logger.debug("Dados recebidos do enlace. Para a fila!");
            try {
                ToReceiveMessage tmp = new ToReceiveMessage(data, source_mac, datalink_id);
                receivedBuffer.put(tmp);
            } catch (InterruptedException e) {
                logger.error("Interrompido.", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Dados recebidos do enlace, porem em estado invalido: {}.", state);
            throw new IllegalStateException("Tentando repassar dados, mas " +
                    "a entidade esta em estado invalido: "+state.name());
        }
    }

    /**
     * Repassa data e source_mac, para a camada de cima.
     */
    protected void bubbleUp (InterlayerData data, int source_ip) {
        logger.debug("Repassado dados para o transporte.");
        upLayer.received(data, source_ip);
    }

    /**
     * Repassa data, para a camada de baixo.
     * @param data
     * @param dest_mac endereco MAC para onde mandar os dados
     * @param datalink_id Id do enlace a ser usado.
     * @throws InterruptedException
     */
    protected void bubbleDown (InterlayerData data, int dest_mac, int datalink_id) {
        logger.debug("Repassado dados para enlace ID = {}.", datalink_id);
        downLayer[datalink_id].send(data, dest_mac);
    }

    @Override
    protected void finalize() throws Throwable {

        sendThread.interrupt();
        receivedThread.interrupt();

        super.finalize();
    }

    /**
     * Este metodo e' chamado automaticamente, pela thread de envio.
     * <p/>
     * A mensagem deve ser completamente processada e repassada para a camada
     * de baixo, antes que este metodo retorne.
     */
    protected abstract void processSentData(InterlayerData data, int dest_ip);

    /**
     * Este metodo e' chamado automaticamente, pela thread de recebimento.
     * <p/>
     * A mensagem deve ser completamente processada e repassada para a camada
     * de cima, se necessário, antes que este metodo retorne.
     */
    protected abstract void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id);

    /**
     * Deve retornar o ip da maquina atual. No caso de um gateway, que tem mais
     * de um ip, o mesmo ip deve sempre ser retornado.
     *
     * @return O ip desta maquina.
     */
    public abstract int getIp();

}
