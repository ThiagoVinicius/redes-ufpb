package br.ufpb.di.redes.layers.network.impl.tables;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * Representacao de uma tabela com a chave sendo um Integer e o valor um mapa de Integer/Integer.
 *
 * @author Joedson Marques - joedson7[at]hotmail.com
 * @author Elenilson Vieira - elenilson[at]elenilsonvieira.com
 *
 * @since 15 de janeiro de 2009
 */
public class Table {

    /**
     * Aqui temos um mapa de mapas. Cada ip da rede tem seu mapa separado dos outros.
     * No mapa do ip, a chave e' o ip destino e o valor(ex: ip para route table, mac para arp table).
     */
    private Map<Integer, Map<Integer, Integer>> table = new HashMap<Integer, Map<Integer, Integer>>();

    /**
     * Retorna uma chave do mapa de um determinado ip passado como argumento
     *
     * @param my_ip o ip para localizacao de seu mapa
     * @param key a chave no mapa do ip passado como argumento
     *
     * @return retorna o valor correspondente a chave ou -1 se nao existe mapa para esse ip
     */
    public int get(int my_ip, int key){
        if(!constainsMap(my_ip))
            return -1;

        Map<Integer, Integer> ip_map = table.get(my_ip);
        return ip_map.get(key);
    }
    
    /**
     * Seta um par chave/valor no mapa de um determinado ip
     *
     * @param my_ip o ip para localizacao de seu mapa
     * @param key a chave no mapa do ip passado como argumento
     * @param value o valor corresponde a chave
     */
    public void set(int my_ip, int key, int value){
        if(!constainsMap(my_ip))
            addMap(my_ip);

        Map<Integer, Integer> ip_map = table.get(my_ip);
        ip_map.put(key, value);
    }

    /**
     * Remove o par chave/valor do mapa de um determinado ip e retorna
     * o valor.
     *
     * @param my_ip o ip para localizacao de seu mapa
     * @param key a chave no mapa do ip passado como argumento
     *
     * @return remove o par chave/valor
     */
    public int remove(int my_ip, int key){
        if(!constainsMap(my_ip))
            return -1;

        Map<Integer, Integer> ip_map = table.get(my_ip);
        return ip_map.remove(key);
    }

    /**
     * Adiciona um mapa na tabela.
     *
     * @param ip a chave correspondente ao mapa
     */
    public void addMap(int ip){
        table.put(ip, new Hashtable<Integer, Integer>());
    }

    /**
     * Verifica se contem um mapa relacionado com o ip passado como argumento.
     *
     * @param ip o ip do qual sera procurado o mapa correspondente
     *
     * @return um boolean indicando se o mapa esta contido
     */
    public boolean constainsMap(int ip){
        return table.containsKey(ip);
    }

    /**
     * Retorna o tamanho da tabela correspondente ao ip passado como argumento.
     *
     * @param ip o ip do qual sera procurado o mapa correspondente
     *
     * @return o tamanho do mapa do ip ou -1 se nao contiver mapa para esse ip
     */
    public int sizeMap(int ip){
        return constainsMap(ip) ? table.get(ip).size() : -1;
    }


}
