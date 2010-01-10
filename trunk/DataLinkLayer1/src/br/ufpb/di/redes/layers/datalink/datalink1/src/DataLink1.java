/**
 * Implementacao da camada DataLink1.
 *
 * @author Amanda Barreto Cavalcanti
 * @author Gutenberg Pessoa Botelho Neto
 */

package br.ufpb.di.redes.layers.datalink.datalink1.src;

import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;

public class DataLink1 extends DataLink {
    public DataLink1 (Physical downLayer, int id) {
        super(downLayer, id);
    }

    /**
     * Método para calcular o CRC de 4 bits a partir de dados com 12 bits
     * de comprimento.
     * @param dados dados a partir dos quais será calculado o CRC.
     * @return valor do CRC.
     */
    private int calculaCRC4 (InterlayerData dados) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Método para criar um quadro de dados.
     * @param controle valor do campo de controle deste quadro (2 ou 3)
     * @param dados serao encapsulados no quadro.
     * @return quadro contendo os dados, o controle e o CRC.
     */
    private InterlayerData criaQuadroDeDados (int controle,
                    InterlayerData dados) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retira os bits inseridos pelo enlace neste quadro para que apenas
     * os dados relevantes à camada de rede possam ser repassados.
     * @param quadro criado pelo enlace, de onde serao retiradas informacoes
     * irrelevantes para as camadas superiores.
     * @return dados sem os campos inseridos pelo enlace.
     */
    private InterlayerData desenquadra (InterlayerData quadro) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Verifica se o CRC contido em determinado quadro está correto.
     * @param quadro cujo CRC será avaliado.
     * @return valor booleano dizendo se o CRC está correto (true) ou não.
     */
    private boolean verificaCRC(InterlayerData quadro) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return 32;
    }
}
