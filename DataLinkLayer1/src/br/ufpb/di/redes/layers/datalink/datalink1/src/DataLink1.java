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
     * Tamanho dos quadros de dados e de permissao e endereçamento.
     */
    private static final int TAMQUADRODEDADOS = 16;
    private static final int TAMQUADROPERMISSAOENDERECAMENTO = 16;
    
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
    private ArrayList<InterlayerData> mensagemAtual;

    /** Campo referente ao MAC deste enlace. */
    private int mac;

    public DataLink1 (Physical downLayer, int id, int mac) {
        super(downLayer, id);
        this.mac = mac;
    }

    /**
     * Método para calcular o CRC de 4 bits a partir de dados com 12 bits
     * de comprimento. Se a mensagem tiver mais de 12 bits, será avaliado o
     * CRC dos 12 primeiros.
     * @param dados dados a partir dos quais será calculado o CRC.
     * @return valor do CRC.
     */
    private int calculaCRC4 (InterlayerData dados) {
        return 0; // só para testes, of course! =)
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
            logger.warn("Retornando um quadro vazio!");
            return aux;
        }
 
        defineControle(controle, aux);
        aux.putInfo(TAMCONTROLE, BITSDADOS, dados.takeInfo(0, BITSDADOS));
        int CRC = calculaCRC4(aux);
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
        InterlayerData aux = new InterlayerData(TAMQUADROPERMISSAOENDERECAMENTO);
        defineControle(CTRLQUADROPERMISSAOEENDERECAMENTO, aux);
        aux.putInfo(TAMCONTROLE, TAMMAC, mac);
        aux.putInfo(TAMCONTROLE+TAMMAC, TAMMAC, dest_mac);
        aux.putInfo(aux.length-(TAMCRC+2), 1, bitDeDados);
        aux.putInfo(aux.length-(TAMCRC+1), 1, bitDePermissao);
        int CRC = calculaCRC4(aux);
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
        ArrayList<InterlayerData> quadros = new ArrayList<InterlayerData>();
        InterlayerData aux;
        for (int i = 0; i < dados.length; i += BITSDADOS) {
            aux = new InterlayerData (BITSDADOS);
            aux.putInfo(0, BITSDADOS, dados.takeInfo(i, BITSDADOS));
            aux = criaQuadroDeDados ((i + BITSDADOS < dados.length) ? 
                CTRLQUADRODEDADOSINTERMEDIARIO : CTRLQUADRODEDADOSFINAL, aux);
            quadros.add(aux);
        }

        return quadros;
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
            if (!verificaCRC(msg.get(i))) {
                logger.warn("Erro na verificação de CRC - mensagem perdida.");
                return null;
            }
            aux.putInfo(i*BITSDADOS, BITSDADOS,
                    desenquadra(msg.get(i)).takeInfo(0, BITSDADOS));
        }

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
    protected void processSentData(InterlayerData data, int dest_mac) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void processReceivedData(InterlayerData data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int minPacketSize() {
        return 8;
    }

    @Override
    public int maxPacketSize() {
        return 80; // 80 é overkill, porra!
    }
}
