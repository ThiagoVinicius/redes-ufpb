/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.all;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Esta classe representa instancias (denominadas "entidades") de uma 
 * determinada camada na arquitetura de rede. Ha 4 camadas derivadas desta
 * classe, mas cada uma delas pode ser implementada como um conjunto de
 * entidades.
 * <p/>
 * Tudo o que e' comum a todas as camadas esta implementado aqui (ao menos,
 * eu tentei).
 * <p/>
 * PS: Onde esta o pessoal de documentacao?
 *
 * @author Thiago
 */
public abstract class Layer <U extends Layer, D extends Layer> implements Joint<U> {

    public static enum EntityState {

        /**Indica que a entidade esta em pleno funcionamento*/
        RUNNING,
        /**
         * Indica que a entidade esta em funcionamento, mas nao ha camada usando
         * seus servicos.
         */
        HANGING,
        /**
         * A entidade esta se preparando para entrar em funcionamento.
         */
        SETUP,
        /**
         * A entidade esta desligada.
         */
        @Deprecated
        HALTED,
    }

    /**Mantem o estado atual da camada*/
    private volatile EntityState state;

    /**
     * Logger para esta classe. E' privado mesmo, cada classe que precise deve
     * ter o seu proprio.
     */
    private Logger logger = LoggerFactory.getLogger(Layer.class);

    /**
     * Uma referencia para a camada de cima.
     *
     * Esta referencia sera diferente de null, se e somente se uma chamada a
     * getState() retorna EntityState.RUNNING
     */
    protected U upLayer;

    /**
     * Uma referencia para a camada de baixo;
     */
    protected D downLayer;

    /**
     * Este construtor inicializa a camada com estado EntityState.HANGING
     */
    protected Layer(D downLayer) {
        state = EntityState.HANGING;
        upLayer = null;
        this.downLayer = downLayer;
    }

//    /**
//     * Desfaz a ligacao com a camada superior. Esta chamada se propagara pilha
//     * acima, causando o detach de todas as camadas acima desta.
//     */
//    public synchronized void detach() {
//        EntityState curState = getState();
//        if (curState == EntityState.RUNNING) {
//            upLayer.detach();
//            logger.info("Executando detach ({})", getClass());
//            state = EntityState.HANGING;
//            upLayer.detach();
//            upLayer = null;
//            stop();
//        } else {
//            logger.warn("Ignorando detach: estado era {}", curState);
//        }
//    }

    /**
     * Cria uma amarracao com uma entidade da camada superior.
     *
     * @param upLayer Entidade a ser amarrada
     *
     * @throws IllegalStateException Se ja ha uma entidade amarrada no topo
     * desta entidade.
     */
    public synchronized void attach(U upLayer) throws IllegalStateException {

        EntityState curState = getState();

        if (curState == EntityState.HANGING) {
            logger.info("Executando attach ({})", getClass());
            state = EntityState.RUNNING;
            this.upLayer = upLayer;
        } else {
            logger.warn("Attach falhou: Estado invalido: {}", curState);
            throw new IllegalStateException("Tentando executar attach em " +
                    "estado invalido: "+curState.name());
        }
    }

    /**
     * @return O atual estado da camada.
     */
    public EntityState getState () {
        return state;
    }

    /**
     * Este metodo sera chamado logo apos a execucao de um attach bem sucedido,
     * informando que a camada superior ja esta conectada. Subclasses podem
     * fazer override deste metodo, para tomar as devidas providencias, se
     * houver necessidade.
     * <p/>
     * Este metodo so sera chamado uma unica vez.
     */
    public void start () {
    }

    /**
     *
     * @return Tamanho minimo do pacote desta camada, em bits.
     */
    public abstract int minPacketSize();

    /**
     *
     * @return Tamanho maximo do pacote desta camada, em bits.
     */
    public abstract int maxPacketSize();

    /**
     * Retorna o identificador da camada, para debug.
     * @return
     */
    public String getName() {
        return "SEM NOME";
    }

}
