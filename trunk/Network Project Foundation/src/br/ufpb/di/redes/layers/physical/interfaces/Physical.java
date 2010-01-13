/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.physical.interfaces;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.all.DefaultValues;
import br.ufpb.di.redes.layers.all.Layer;
import br.ufpb.di.redes.layers.all.NullLayer;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Esta e' a super-classe de toda e qualquer entidade do nivel fisico. Entidades
 * de camadas superiores podem usar o metodo send() para solicitar a
 * escrita de dados para o meio fisico. Dados vindos do meio fisico sao
 * repassados para a entidade de nivel de enlace que esta amarrada ao topo desta
 * camada, por meio do metodo DataLink.received().
 * <p/>
 *
 * @author Thiago
 */
public abstract class Physical extends Layer <DataLink, NullLayer> {

    /**Logger para esta classe*/
    private static final Logger logger = LoggerFactory.getLogger(Physical.class);

    /**Buffer de mensagens.*/
    private ArrayBlockingQueue<InterlayerData> downBuffer;

    /**
     * Thread que executa o envio de mensagens, repetidamente esvaziando
     * downBuffer. Se o buffer esta vazio, a thread espera a chegada de
     * mensagens antes de continuar.
     */
    private Thread downThread;

    public Physical() {
        super(new NullLayer());
        downBuffer = new ArrayBlockingQueue<InterlayerData>(downBufferSize, true);
    }

    private static int downBufferSize = DefaultValues.DOWN_BUFFER_SIZE;

    /**
     * Define a quantidade de mensagens em downBuffer.
     * <p/>
     * So deve ser usado durante a fase de configuracao do sistema.
     *
     * @param newSize novo tamanho do buffer. Deve ser maior que zero.
     */
    public static synchronized final void setDownBufferSize(int newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("newSize deve ser maior que 0! " +
                    "newSize = " + newSize );
        }
        downBufferSize = newSize;
    }

    /**
     * Armazena data em downBuffer, se houver espaco imediatamente. Se nao
     * houver espaco imediatamente, bloqueia ate que haja espaco disponivel.
     *
     * @param data PDU a ser enviada.
     *
     * @throws IllegalStateException Sera lancada se nao houver entidade
     * amarrada ao topo desta entidade.
     */
    public synchronized final void send(InterlayerData data)
            throws IllegalStateException {

        EntityState state = getState();
        if (state == EntityState.RUNNING) {
            logger.debug("Dados recebidos do enlace. Para a fila!");
            try {
                downBuffer.put(data);
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
     * Repassa data, para a camada de cima.
     *
     * @param data dados a ser repassados.
     */
    protected synchronized void bubbleUp (InterlayerData data) {
        logger.debug("Repassado dados para o enlace.");
        //if (upLayer != null) //isso nao deve acontecer normalmente
        upLayer.received(data);
    }

    @Override
    protected void finalize() throws Throwable {

        downThread.interrupt();

        super.finalize();
    }

    /**
     * Este metodo e' chamado automaticamente, pela thread de recebimento.
     * <p/>
     * A mensagem deve ser completamente processada e enviada, antes que este
     * metodo retorne.
     */
    protected abstract void processSentData (InterlayerData data);

    /**
     * Esvazia o buffer e inicia a thread de recebimento de mensagens.
     */
    @Override
    public synchronized void start() {
        logger.info("Iniciando servico");
        downBuffer.clear();
        downThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        processSentData(downBuffer.take());
                    }
                } catch (InterruptedException e) {
                }
                logger.debug("Thread de recebimento interrompida.");
            }
        };
        downThread.start();
    }

}
