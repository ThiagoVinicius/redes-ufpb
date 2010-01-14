package br.ufpb.di.redes.layers.datalink.impl;


import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author J. Marques
 */
public class NetworkImpl extends Network {
    
    private int [] source_ips;
    public static final byte HEADER_LENGHT = 8;//Tamanho do cabecalho usado para redimensionar o rray
    public static final byte NETWORK_LENGHT_OF_IP = 2;
    public static final byte STATION_LENGHT_OF_IP = 2;
    public static final byte NETWORK_FULL_ADRESS_SIZE=NETWORK_LENGHT_OF_IP+STATION_LENGHT_OF_IP;//Tamanho completo do ip

    /**
     * Chave - IP de destino
     * Valor - Proximo salto
     */
    private Map<Integer, Integer> route_table = new HashMap<Integer, Integer>();
    
    /**
     * Chave - IP
     * Valor - MAC
     */
    private Map<Integer, Integer> arp_table = new HashMap<Integer, Integer>();

    //Usado para quando nenhum dos meus IPs concincidir com o IP recebido de cima
    public static final byte PATERN_IP_POSITION = 0;//Indice do array de IPs que contem o endereco origem padrao
    
    //Formato do cabecalho:
    //do 0 ao 3 bit source_ip (bits mais significativos a direita)
    //do 4 ao 7 bit destination_ip (bits mais significativos a direita)
    
    //Coloca o q vc precisar, como por exemplo o IP, no caso um arrays de IPs
    public NetworkImpl(DataLink[] downLayers, int [] source_ips) {
        super(downLayers);
        this.source_ips = source_ips;
    }

    @Override
    protected void processReceivedData(InterlayerData data, int soruce_mac, int datalink_id) {

        //Pega o IP destino e verifica se e o nosso IP, se nao for para nos repessa o pacote para o enlace
        if(containsIp(data.takeInfo(NETWORK_FULL_ADRESS_SIZE, NETWORK_FULL_ADRESS_SIZE))){
            //Obs: Errado source_mac manda de volta para quem enviou,
            bubbleDown(data, soruce_mac, datalink_id);
            return;
        }
        
        //Se for para nos, cria um novo pacate para manda para camada de cima
        InterlayerData dataToTransport = new InterlayerData(data.length - HEADER_LENGHT);

        //Obs: Total de bits que ele vai ler: "data.length - HEADER_LENGHT', se for menor q zero ferrou
        //Copia de bits
        InterlayerData.copyBits(dataToTransport, data,(int)HEADER_LENGHT,data.length - HEADER_LENGHT,0);

        //Obtem o ip
        int source_ip = data.takeInfo(0, NETWORK_FULL_ADRESS_SIZE);
        bubbleUp(dataToTransport, source_ip);//manda para cima
    }

    @Override
    protected void processSentData(InterlayerData data, int dest_ip) {
        //Array de bits suficiente para anexar o nosso cebecalho
        InterlayerData dataToDataLink = new InterlayerData(data.length + HEADER_LENGHT);

        //Coloca o endereco destino no arrays de bits que sera enviado para o enlace
        int source_ip = getIp(dest_ip);
        
        //Adiciona os respectivos IPs origem e destino
        dataToDataLink.putInfo(0,NETWORK_FULL_ADRESS_SIZE,source_ip);
        dataToDataLink.putInfo(NETWORK_FULL_ADRESS_SIZE,NETWORK_FULL_ADRESS_SIZE,dest_ip);

        //Copia dos bits
        InterlayerData.copyBits(dataToDataLink,data,0,data.length,HEADER_LENGHT);
        
        bubbleDown(dataToDataLink, getMac(dest_ip), getIdDataLink(source_ip));
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
        return source_ips[PATERN_IP_POSITION];
    }

    private int getMac(int dest_ip) {
        return arp_table.get(dest_ip);
    }

    private int getIdDataLink(int source_ip) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //Verifico se tem algum ip origem na mesma rede do ip destino, caso contrario uso retorno o ip padrao
    private int getIp(int dest_ip) {
        
        InterlayerData network_dest_ip = new InterlayerData(NETWORK_LENGHT_OF_IP);
        network_dest_ip.putInfo(0, NETWORK_LENGHT_OF_IP, dest_ip);
        int dest_network = network_dest_ip.takeInfo(0, NETWORK_LENGHT_OF_IP);

        InterlayerData network_source_ip = new InterlayerData(NETWORK_LENGHT_OF_IP);

        for(int ip: source_ips){

            network_source_ip.putInfo(0, NETWORK_LENGHT_OF_IP, ip);
            int source_network = network_source_ip.takeInfo(0, NETWORK_LENGHT_OF_IP);
            if(source_network == dest_network)
                return ip;
        }

        return source_ips[PATERN_IP_POSITION];
    }

    //Metedo que define se o ip passado como parametro esta ou nao no array de ips
    private boolean containsIp(int ip) {

        for(int pos : source_ips){
            if(pos == ip)
                return true;
        }

        return false;
    }

}
