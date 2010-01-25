package br.ufpb.di.redes.layers.network.impl;

import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;

/**
 *
 * Implementacao da camada de rede. Essa classe extende a classe NetworkImpl e adiciona outras
 * funcionalidades da camada de rede, como TTL e Segmentacao de pacotes.
 *
 * @author Joedson Marques - joedson7[at]hotmail.com
 * @author Elenilson Vieira - elenilson[at]elenilsonvieira.com
 *
 * @since 25 de janeiro de 2009
 */
public class NetworkImpl3 extends NetworkImpl{
    
    /**
     * Construtor da classe
     *
     * Formato do cabecalho:
     * 0 ao 3 bit - source_ip (bits mais significativos a direita)
     * do 4 ao 7 bit - destination_ip (bits mais significativos a direita)
     * 8 e 9 bit - ttl
     * 10 e 11 bit - numero de sequencia
     *
     * @param downLayers as camadas inferiores
     * @param source_ips os ips origem que cada posicao DEVE esta associoada a cada posicao do array de enlaces
     * @param smaller_datalink_max_packet_size o menor maximo de todos os enlaces
     */
    public NetworkImpl3(DataLink[] downLayers, int[] source_ips, int smaller_datalink_max_packet_size) {
        super(downLayers, source_ips, smaller_datalink_max_packet_size);
    }

    @Override
    public int maxPacketSize() {
        return SMALLER_DATALINK_MAX_PACKET_SIZE + Constants.HEADER_LENGHT_IMPL_2.getValue();
    }

    

}
