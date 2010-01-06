/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.datalink.interfaces;

import br.ufpb.di.redes.layers.all.DefaultValues;
import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.all.Layer;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thiago
 */
public abstract class DataLink extends Layer<Network, Physical> {

    /**Tamanho do buffer de mensagens vindas da camada de cima*/
    private static int sendBufferSize = DefaultValues.DOWN_BUFFER_SIZE;

    /**Tamanho do buffer de mensagens vindas da camada de baixo*/
    private static int receivedBufferSize = DefaultValues.DOWN_BUFFER_SIZE;

    /**Logger para esta classe*/
    private static final Logger logger = LoggerFactory.getLogger(DataLink.class);

    /**Usado para armazenar mensagens vindas da camada de rede, no buffer*/
    private static class ToSendMessage {
        public final InterlayerData data;
        public final int dest_mac;
        public ToSendMessage(InterlayerData data, int dest_mac) {
            this.data = data;
            this.dest_mac = dest_mac;
        }
    }

    /**Buffer de mensagens recebidas da camada de cima.*/
    private ArrayBlockingQueue<ToSendMessage> sendBuffer;

    /**Buffer de mensagens recebidas da camada de baixo.*/
    private ArrayBlockingQueue<InterlayerData> receivedBuffer;

    /**Thread que processa mensagens recebidas da camada de cima.*/
    private Thread sendThread;

    /**Thread que processa mensagens recebidas da camada de baixo.*/
    private Thread receivedThread;

    private final int id;

    public DataLink(Physical downLayer, int id) {
        super(downLayer);
        this.id = id;
        sendBuffer = new ArrayBlockingQueue<ToSendMessage>(sendBufferSize);
        receivedBuffer = new ArrayBlockingQueue<InterlayerData>(receivedBufferSize);
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
        DataLink.receivedBufferSize = newSize;
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
        DataLink.sendBufferSize = newSize;
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
                        processSentData(tmp.data, tmp.dest_mac);
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
                        processReceivedData(receivedBuffer.take());
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
     * Armazena data e dest_mac, se houver espaco imediatamente. Se nao
     * houver espaco imediatamente, bloqueia ate que haja espaco disponivel.
     * <p/>
     * Esta e' a interface publica que deve ser chamada a partir de camadas
     * superiores. Em geral, nao deve ser chamado diretamente o metodo
     * bubbleDown() normalmente se encarrega desta tarefa.
     *
     * @param data PDU a ser enviada.
     * @param dest_mac mac de destino.
     * 
     * @throws IllegalStateException Sera lancada se nao houver entidade
     * amarrada ao topo desta entidade.
     */
    public void send(InterlayerData data, int dest_mac) {
        EntityState state = getState();
        if (state == EntityState.RUNNING) {
            logger.debug("Dados recebidos da rede. Para a fila!");
            try {
                ToSendMessage tmp = new ToSendMessage(data, dest_mac);
                sendBuffer.put(tmp);
            } catch (InterruptedException e) {
                logger.error("Interrompido.", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Dados recebidos da rede, porem em estado invalido: {}.", state);
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
     *
     * @throws IllegalStateException Sera lancada se nao houver entidade
     * amarrada ao topo desta entidade.
     */
    public void received(InterlayerData data) {
        EntityState state = getState();
        if (state == EntityState.RUNNING) {
            logger.debug("Dados recebidos de fisica. Para a fila!");
            try {
                receivedBuffer.put(data);
            } catch (InterruptedException e) {
                logger.error("Interrompido.", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Dados recebidos de fisica, porem em estado invalido: {}.", state);
            throw new IllegalStateException("Tentando repassar dados, mas " +
                    "a entidade esta em estado invalido: "+state.name());
        }
    }

    /**
     * Repassa data e source_mac, para a camada de cima.
     */
    protected void bubbleUp (InterlayerData data, int source_mac) {
        logger.debug("Repassado dados para a rede.");
        upLayer.received(data, source_mac, id);
    }

    /**
     * Repassa data, para a camada de baixo.
     * @param data
     * @throws InterruptedException
     */
    protected void bubbleDown (InterlayerData data) {
        logger.debug("Repassado dados para fisica.");
        downLayer.send(data);
    }

    /**
     * Este metodo e' chamado automaticamente, pela thread de envio.
     * <p/>
     * A mensagem deve ser completamente processada e repassada para a camada
     * de baixo, antes que este metodo retorne.
     */
    protected abstract void processSentData(InterlayerData data, int dest_mac);
    
    /**
     * Este metodo e' chamado automaticamente, pela thread de recebimento.
     * <p/>
     * A mensagem deve ser completamente processada e repassada para a camada 
     * de cima, se necessário, antes que este metodo retorne.
     */
    protected abstract void processReceivedData(InterlayerData data);

}
