package br.ufpb.di.redes.layers.network.impl;


import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.tables.Table;
import br.ufpb.di.redes.layers.network.interfaces.Network;

/**
 *
 * Implementacao da camada de rede. Essa classe extende a classe abstrata Network.
 *
 * @author Joedson Marques - joedson7[at]hotmail.com
 * @author Elenilson Vieira - elenilson[at]elenilsonvieira.com
 *
 * @since 15 de janeiro de 2009
 */
public class NetworkImpl extends Network {
    
    private int [] source_ips;

    /**
     * PATERN_IP_POSITION - Usado para quando nenhum dos meus IPs concincidir com o IP recebido de cima
     */
    public enum Constants {HEADER_LENGHT(8), NETWORK_LENGHT_OF_IP(2), STATION_LENGHT_OF_IP(2),
        PATERN_IP_POSITION(0), NETWORK_FULL_ADDRESS_SIZE(NETWORK_LENGHT_OF_IP.getValue() + STATION_LENGHT_OF_IP.getValue());
        private int value;

        private Constants(int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }

    public enum MyTables{ARP_TABLE, ROUTE_TABLE}
    
    /**
     * Chave - Rede
     * Valor - Ip de quem envia para essa rede
     */
    private Table route_table = new Table();

    /**
     * Chave - Ip
     * Valor - Mac correspondente a esse ip
     */
    private Table arp_table = new Table();
    
    /**
     * Construtor da classe
     *
     * Formato do cabecalho:
     * 0 ao 3 bit source_ip (bits mais significativos a direita)
     * do 4 ao 7 bit destination_ip (bits mais significativos a direita)
     * 
     * @param downLayers as camadas inferiores
     * @param source_ips os ips origem que cada posicao DEVE esta associoada a cada posicao do array de enlaces
     */
    public NetworkImpl(DataLink[] downLayers, int [] source_ips) {
        super(downLayers);
        this.source_ips = source_ips;
    }

    @Override
    protected void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id) {
        int ip_dest = data.takeInfo(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());
        
        //Pega o IP destino e verifica se e' o nosso IP, se o pacote nao for para nos repessa o pacote para o enlace
        if(!containsIp(ip_dest)){
            int dest_mac = getMacToSendToIp(ip_dest, datalink_id);
            bubbleDown(data, dest_mac, datalink_id);
            return;
        }
        
        //Se for para nos, cria um novo pacate para manda para camada de cima
        InterlayerData dataToTransport = new InterlayerData(data.length - Constants.HEADER_LENGHT.getValue());

        //Obs: Total de bits que ele vai ler: "data.length - HEADER_LENGHT', se for menor q zero ferrou
        //Copia de bits
        InterlayerData.copyBits(dataToTransport, data, (int) Constants.HEADER_LENGHT.getValue(),
                data.length - Constants.HEADER_LENGHT.getValue(), 0);

        //Obtem o ip
        int source_ip = data.takeInfo(0, Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());
        bubbleUp(dataToTransport, source_ip);//manda para cima
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_ip) {
        //Array de bits suficiente para anexar o nosso cebecalho
        InterlayerData dataToDataLink = new InterlayerData(data.length + Constants.HEADER_LENGHT.getValue());

        //Coloca o endereco destino no arrays de bits que sera enviado para o enlace
        int source_ip = getIp(dest_ip);
        
        //Adiciona os respectivos IPs origem e destino
        dataToDataLink.putInfo(0, Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), source_ip);
        dataToDataLink.putInfo(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), dest_ip);

        //Copia dos bits
        InterlayerData.copyBits(dataToDataLink, data, 0, data.length, Constants.HEADER_LENGHT.getValue());
        
        bubbleDown(dataToDataLink, arp_table.get(getIp(dest_ip), dest_ip), getIdDataLink(source_ip));
    }

    @Override
    public int maxPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int minPacketSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIp() {
        return source_ips[Constants.PATERN_IP_POSITION.getValue()];
    }

    /**
     * Procura o id do enlace dado o ip origem
     *
     * @param source_ip o ip origem
     *
     * @return o id do enlace relacionado com o ip origem
     */
    private int getIdDataLink(int source_ip) {
        for(int posicao = 0; posicao < source_ips.length; posicao++)
            if(source_ips[posicao] == source_ip)
                return posicao;

        return -1;
    }

    /**
     * Procura no array de ips o ip correspondente ao id do enlace.
     *
     * @param id_dataLink o id do enlace
     *
     * @return o ip correspondente ao id do enlace
     */
    private int getIpFromDataLinkId(int id_dataLink){
        return source_ips[id_dataLink];
    }

    /**
     * Verifica se tem algum ip origem na mesma rede do ip destino,
     * caso contrario uso retorno o ip padrao
     *
     * @param dest_ip o ip destino
     *
     * @return o ip origem relacionado com o ip destino
     */
    private int getIp(int dest_ip) {
        
        InterlayerData network_dest_ip = new InterlayerData(Constants.NETWORK_LENGHT_OF_IP.getValue());
        network_dest_ip.putInfo(0, Constants.NETWORK_LENGHT_OF_IP.getValue(), dest_ip);
        int dest_network = network_dest_ip.takeInfo(0, Constants.NETWORK_LENGHT_OF_IP.getValue());

        InterlayerData network_source_ip = new InterlayerData(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());

        for(int ip: source_ips){

            network_source_ip.putInfo(0, Constants.NETWORK_LENGHT_OF_IP.getValue(), ip);
            int source_network = network_source_ip.takeInfo(0, Constants.NETWORK_LENGHT_OF_IP.getValue());
            if(source_network == dest_network)
                return ip;
        }

        return source_ips[Constants.PATERN_IP_POSITION.getValue()];
    }

    /** 
     * Define se o ip passado como parametro esta ou nao no array de ips
     *
     * @param ip o ip a ser procurado
     *
     * @return um boolean indicando se o ip esta contido.
     */
    private boolean containsIp(int ip) {

        for(int pos : source_ips){
            if(pos == ip)
                return true;
        }

        return false;
    }
    
    /**
     * Retorna o mac que deve ser usado para enviar o pacote para o ip passado 
     * como argumento.
     * 
     * @param ip_dest o ip
     * 
     * @return o mac a ser usado para enviar o pacote ou -1 se nada foi encontrado
     */
    private int getMacToSendToIp(int ip_dest, int id_dataLink) {
        int network_dest = splitIP(ip_dest)[0];

        int my_ip = getIpFromDataLinkId(id_dataLink);
        int my_network = splitIP(my_ip)[0];

        int map_size = route_table.sizeMap(my_ip);

        //O maximo de loops que eu darei sera o tamanho da tabela. Evita loop infinito!
        for(int i = 0; i < map_size; i++){
            int ip_sender = route_table.get(my_ip, network_dest);
            int network_sender = splitIP(ip_sender)[0];

            if(network_sender == my_network)
                return arp_table.get(my_ip, ip_sender);

            network_dest = splitIP(ip_sender)[0];
        }

        return -1;
    }

    /**
     * Retira o ip do InterlayerData e retorna o endereco de rede e da estacao.
     *
     * @param interlayerData o pacote
     *
     * @return um array onde a primeira posicao indica o endereco de rede e a segunda indica o endereco da estacao
     */
    private int[] splitIP(InterlayerData interlayerData){
        int network = interlayerData.takeInfo(0, Constants.NETWORK_LENGHT_OF_IP.getValue());
        int station = interlayerData.takeInfo(Constants.NETWORK_LENGHT_OF_IP.getValue(), Constants.STATION_LENGHT_OF_IP.getValue());

        return new int[]{network, station};
    }

    /**
     * Retira do ip o endereco de rede e da estacao.
     *
     * @param ip o ip
     *
     * @return um array onde a primeira posicao indica o endereco de rede e a segunda indica o endereco da estacao
     */
    private int[] splitIP(int ip){
        InterlayerData interlayerData = new InterlayerData(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());

        interlayerData.putInfo(0, Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), ip);

        return splitIP(interlayerData);
    }

    /**
     * Seta os valores em uma das tabelas.
     *
     * @param table a tabela a ser adicionado os valores
     * @param my_ip o ip correspondente a tabela. Se nao existir tabela para esse ip, uma nova sera criada.
     * @param key a chave na tabela correspondente ao ip passado como argumento
     * @param value o valor correspondente 'a chave na tabela correspondente ao ip passado como argumento
     */
    public void setInTable(MyTables table, int my_ip, int key, int value){
        switch(table){
            case ARP_TABLE: arp_table.set(my_ip, key, value); break;
            case ROUTE_TABLE: route_table.set(my_ip, key, value); break;
        }
    }

    /**
     * Remove o par chave/valor da tabela do ip passado como argumento.
     *
     * @param table a tabela a ser adicionado os valores
     * @param my_ip o ip correspondente a tabela.
     * @param key a chave na tabela correspondente ao ip passado como argumento
     *
     * @return o valor removido ou -1 se nao existia
     */
    public int removeInTable(MyTables table, int my_ip, int key){
        int value = -1;

        switch(table){
            case ARP_TABLE: value = arp_table.remove(my_ip, key); break;
            case ROUTE_TABLE: value = route_table.remove(my_ip, key); break;
        }

        return value;
    }

    /**
     * Retorna o valor da tabela do ip passado como argumento.
     *
     * @param table a tabela a ser adicionado os valores
     * @param my_ip o ip correspondente a tabela.
     * @param key a chave na tabela correspondente ao ip passado como argumento
     *
     * @return o valor ou -1 se nao existia
     */
    public int getInTable(MyTables table, int my_ip, int key){
        int value = -1;

        switch(table){
            case ARP_TABLE: value = arp_table.get(my_ip, key); break;
            case ROUTE_TABLE: value = route_table.get(my_ip, key); break;
        }

        return value;
    }



}
