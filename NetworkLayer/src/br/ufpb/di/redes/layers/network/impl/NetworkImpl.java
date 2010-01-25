package br.ufpb.di.redes.layers.network.impl;


import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.tables.Table;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final int SMALLER_DATALINK_MAX_PACKET_SIZE;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkImpl.class);

    /**
     * PATERN_IP_POSITION - Usado para quando nenhum dos meus IPs concincidir com o IP recebido de cima
     */
    public enum Constants {NETWORK_LENGHT_OF_IP(2), STATION_LENGHT_OF_IP(2), 
        PATERN_IP_POSITION(0), SEQUENCY_NUMBER(2), TTL(2),
        NETWORK_FULL_ADDRESS_SIZE(NETWORK_LENGHT_OF_IP.value + STATION_LENGHT_OF_IP.value), NETWORK_DEFAULT(-1),
        HEADER_LENGHT_IMPL_1(NETWORK_FULL_ADDRESS_SIZE.value * 2),
        HEADER_LENGHT_IMPL_2((NETWORK_FULL_ADDRESS_SIZE.value * 2) + SEQUENCY_NUMBER.value + TTL.value);
        private int value;

        private Constants(int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s - %d", super.toString(), value);
        }
    }

//    static {
//        for(Constants constants : Constants.values()){
//            System.out.println(constants);
//        }
//    }
    
     /**
     * Chave - Rede ou Estacao
     * Valor - Ip de quem envia para essa rede ou estacao
     */
    private Map <Integer, Integer> route_table = new HashMap<Integer, Integer>();

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
    public NetworkImpl(DataLink[] downLayers, int [] source_ips, int smaller_datalink_max_packet_size) {
        super(downLayers);
        this.SMALLER_DATALINK_MAX_PACKET_SIZE = smaller_datalink_max_packet_size;
        this.source_ips = source_ips;
    }

    @Override
    protected void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id) {
        int ip_dest = data.takeInfo(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());
        LOGGER.debug("Abri pacote de destino {}.", ip_dest);
        
        //Pega o IP destino e verifica se e' o nosso IP, se o pacote nao for para nos repessa o pacote para o enlace
        if(!containsIp(ip_dest)){
            LOGGER.debug("Pacote nao e' para mim, roteando.");
            /*
             * Estava assim
             * int dest_mac = getMacToSendToIp(ip_dest, datalink_id);
             * bubbleDown(data, dest_mac, datalink_id);
             */

            /**
             * AQUI!
             *
             * Falta ajeitar pois o metodo makesRouting supoe que ainda nao tem cabecalho, mas os dados recebidos aqui
             * sao da camada de enlace e tem o nosso cabecalho ja.
             */

            makesRouting(data, ip_dest, false);
            return;
        }
        
        //Se for para nos, cria um novo pacate para manda para camada de cima
        InterlayerData dataToTransport = new InterlayerData(data.length - Constants.HEADER_LENGHT_IMPL_1.getValue());

        //Obs: Total de bits que ele vai ler: "data.length - HEADER_LENGHT', se for menor q zero ferrou
        //Copia de bits
        InterlayerData.copyBits(dataToTransport, data, (int) Constants.HEADER_LENGHT_IMPL_1.getValue(),
                data.length - Constants.HEADER_LENGHT_IMPL_1.getValue(), 0);

        //Obtem o ip
        int source_ip = data.takeInfo(0, Constants.NETWORK_FULL_ADDRESS_SIZE.getValue());

        LOGGER.debug("Pacote e' para mim, origem {}", source_ip);

        bubbleUp(dataToTransport, source_ip);//manda para cima
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_ip) {
        LOGGER.debug("Enviando pacote para o ip {}", dest_ip);

        //Se for para mim, mando para cima de volta
        if(containsIp(dest_ip)){
            bubbleUp(data, dest_ip);
            return;
        }

        makesRouting(data, dest_ip, true);
    }

    @Override
    public int maxPacketSize() {
        /*
         * So posso quebrar o pacote recebido no
         * numero maximo que posso representar no numero de sequencia 'x' o menor maximo dos enlaces.
         */
        return SMALLER_DATALINK_MAX_PACKET_SIZE - Constants.HEADER_LENGHT_IMPL_1.value;
    }

    @Override
    public int minPacketSize() {
        return Constants.HEADER_LENGHT_IMPL_1.value;
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
        int dest_network = splitIP(dest_ip)[0];

        for(int ip: source_ips){
            int source_network = splitIP(ip)[0];
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

        //Comecado a modificacao
        int map_size = route_table.size();

        //O maximo de loops que eu darei sera o tamanho da tabela. Evita loop infinito!
        for(int i = 0; i < map_size; i++){
            //int ip_sender = route_table.get(my_ip, network_dest);
            int ip_sender = getInRouteTable(network_dest);
            int network_sender = splitIP(ip_sender)[0];

            if(network_sender == my_network || ip_sender==my_ip)
                return arp_table.get(my_ip, ip_sender);//Retorna o MAC e ip_sender(quem envia para ip_dest)

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
     * @param my_ip o ip correspondente a tabela. Se nao existir tabela para esse ip, uma nova sera criada.
     * @param ip o ip a ser inserido na tabela
     * @param mac o mac correspondente ao ip passado como argumento
     */
    public void setInArpTable(int my_ip, int ip, int mac){
         arp_table.set(my_ip, ip, mac);
    }

    /**
     * Remove o par chave/valor da tabela do ip passado como argumento.
     *
     * @param my_ip o ip correspondente a tabela.
     * @param ip o ip a ser removido da tabela juntamente com seu mac
     *9
     * @return o valor removido ou -1 se nao existia
     */
    public int removeInArpTable(int my_ip, int ip){
        int value = -1;

        value = arp_table.remove(my_ip, ip);
      
        return value;
    }

    /**
     * Retorna o valor da tabela do ip passado como argumento.
     *
     * @param my_ip o ip correspondente a tabela.
     * @param ip o ip a ser buscado seu mac na tabela
     *
     * @return o valor ou -1 se nao existia
     */
    public int getInArpTable(int my_ip, int ip){
        int value = -1;

        value = arp_table.get(my_ip, ip);

        return value;
    }
    /**
     * Retorna o ip (sender) que envia para o ip passado como parâmetro.
     *
     * @param to_send_ip ip a cujo enviador será buscado na tabela
     *
     * @return o ip caso o mesmo sejá encontrado ou -1 caso contrário
     */
    public int getInRouteTable(int to_send_ip){

        //Se encontramos a chave retornamos a mesma caso contrario returnamos -1mac
        if(route_table.containsKey(to_send_ip))
            return route_table.get(to_send_ip);//Retorna que envia para o determinado IP

        return -1;
    }

    /**
     * Altera a rota passada como parametro ou adiciona, caso a rota nao existe.
     *
     * @param network uma rede destino a ser inserido na tabela
     * @param sender_to_network o ip responsavel por enviar para a rede passada como argumento
     */
    public void setInRouteTable(int network, int sender_to_network){
        route_table.put(network, sender_to_network);
    }

    /**
     *
     * Obtem o identificador do enlace dado o ip
     *
     * @param source_ip
     *
     * @return o identificador do enlace
     */
    private int getIdDatalinkFromIp(int source_ip) {
        for(int i=0;i<source_ips.length;i++){

            if(source_ips[i] == source_ip)
                return i;

        }

        return Constants.PATERN_IP_POSITION.getValue();
    }

    @Override
    public String getName(){
        return "ESTACAO " + getIp();
    }

    @Override
    public void putArpEntry(int ip, int datalinkId, int mac) {
        int my_ip = getIpFromDataLinkId(datalinkId);
        setInArpTable(my_ip, ip, mac);
    }

    @Override
    public void putRouteEntry (int local_ip, int remote_ip) {
        int network = splitIP(remote_ip)[0];

        setInRouteTable(network, local_ip);
    }

    @Override
    public void putDefaultRoute (int local_ip) {
        setInRouteTable(Constants.NETWORK_DEFAULT.value, local_ip);
    }

    /**
     * Faz o roteamento necessario para enviar o pacote para o ip destino.
     *
     * @param data o pacote a ser enviado
     * @param dest_ip o ip destino
     * @param isUpperLayer indica se o pacote foi enviado pela camada de cima (true) ou pela camada de baixo (false).
     *                     Isso e' uma gambiarra porque eu to sem paciencia de ajeitar!
     */
    private void makesRouting(InterlayerData data, int dest_ip, boolean isUpperLayer){
        //Array de bits suficiente para anexar o nosso cebecalho
        InterlayerData interlayerData = isUpperLayer ? 
            new InterlayerData(data.length + Constants.HEADER_LENGHT_IMPL_1.getValue()) : data;

        int network_dest = splitIP(dest_ip)[0];
        LOGGER.debug("Network de destino e' {} do ip de destino {}!", network_dest, dest_ip);

        Integer sender = route_table.get(network_dest);

        //Caso nao encontre rota, busca pela rota default
        if(sender == null){
            LOGGER.debug("Ip nao achado. Vou para a rota default!");
            sender = route_table.get(Constants.NETWORK_DEFAULT.value);
        }

        int my_ip = getIp(sender);
        
        LOGGER.debug("Rede do meu ip e' {} e rede do sender e' {}", splitIP(my_ip)[0], splitIP(sender)[0]);

        LOGGER.debug("Meu ip e' {} correspondente ao ip destino {}.", my_ip, dest_ip);
        LOGGER.debug("Enviado via sender de ip {}", sender);

        //Se o sender foi eu mesmo, mando pro mac destino
        if (containsIp(sender)) {
            sender = dest_ip;
        }

        int sender_mac = arp_table.get(my_ip, sender);

        //Adiciona os respectivos IPs origem e destino
        if(isUpperLayer) {
            interlayerData.putInfo(0, Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), getIp());
            interlayerData.putInfo(Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), Constants.NETWORK_FULL_ADDRESS_SIZE.getValue(), dest_ip);
            InterlayerData.copyBits(interlayerData, data, 0, data.length, Constants.HEADER_LENGHT_IMPL_1.getValue());
        }
            
        //Pega o id do enlace do meu ip
        int datalink_id = getIdDatalinkFromIp(my_ip);

        LOGGER.debug("Enviando para id {}, mac {}.", datalink_id, sender_mac);

        bubbleDown(interlayerData, sender_mac, datalink_id);
    }

}
