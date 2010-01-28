/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes;

import br.ufpb.di.redes.layers.application.Cliente;
import br.ufpb.di.redes.layers.application.Servidor;
import br.ufpb.di.redes.layers.datalink.datalink1.src.DataLink1;
import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.network.impl.NetworkImpl;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.source.TCP;
import java.io.FileInputStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import physicalLayer.Principal.Fisica;

/**
 *
 * @author Thiago
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        Physical physical[];
        DataLink datalink[];
        Network network;
        Transport transport;

        Properties config = new Properties();
        config.load(new FileInputStream("deploy.properties"));

        int count = 1;
        int mac[] = new int[count];
        int ip[] = new int[count];
        int types[] = new int[count];

        count = new Integer(config.getProperty("datalink.count"));
        loadConfiguration(config, mac, ip, types, count);

        physical = new Physical[count];
        datalink = new DataLink[count];
        for (int i = 0; i < count; ++i) {
            physical[i] = getPhysical(types[i]);
            datalink[i] = new DataLink1(physical[i], i, mac[i]);
            physical[i].attach(datalink[i]);
        }
        
        network  = new NetworkImpl(datalink, ip, 80);
        for (int i = 0; i < count; ++i) {
            datalink[i].attach(network);
        }

        transport = new TCP(network);

        network.attach(transport);

        configRoutes(config, network, count);

        for (int i = 0; i < count; ++i) {
            physical[i].start();
            datalink[i].start();
        }

        network.start();
        transport.start();


        String runServer = config.getProperty("server.run").trim();
        if (runServer.equals("true")) {
            final Servidor server = new Servidor(transport);
            new Thread () {
                @Override
                public void run() {
                    server.go();
                }
            }.start();
        }

        int yes = JOptionPane.showConfirmDialog(null, "Rodar aplicacao?", 
                "Sim? Nao?", JOptionPane.YES_NO_OPTION);

        if (yes == JOptionPane.YES_OPTION) {
            while (true) {
                try {
                    int server_ip = new Integer(JOptionPane.showInputDialog(null,
                            "Ip do servidor?", "IP?!", JOptionPane.INFORMATION_MESSAGE));
                    int server_port = new Integer(JOptionPane.showInputDialog(null,
                            "Porta do servidor?", "Porta?!", JOptionPane.INFORMATION_MESSAGE));

                    final Cliente client = new Cliente(transport, server_port, server_ip);

                    client.run();

                    break;
                } catch (NumberFormatException ex) {
                }
            }
        }

    }

    private static void loadConfiguration(Properties config, int mac[],
            int ip[], int types[], int count) {

        for (int i = 0; i < count; ++i) {
            mac[i] = new Integer (config.getProperty("datalink."+i+".mac"));
            ip[i] = new Integer (config.getProperty("datalink."+i+".ip"));
            types[i] = new Integer (config.getProperty("physical."+i+".type"));
        }

    }

    private static Physical getPhysical (int type) {

        switch (type) {
            case 0: return new Fisica();
            default: return null;
        }

    }

    private static void configRoutes(Properties config, Network network, int count) {
        String rawConfig = config.getProperty("route.entries").trim();
        String allRoutes[] = rawConfig.split("[,]");
        for (String route : allRoutes) {
            if (route.length() > 0) {

                String splitRoute[] = route.split("[-][>]");
                int local_ip = new Integer(splitRoute[0]);
                int remote_ip = new Integer(splitRoute[1]);
                network.putRouteEntry(local_ip, remote_ip);

            }
        }

        String defRoute = config.getProperty("route.default");
        network.putDefaultRoute(new Integer(defRoute));

        for (int id = 0; id < count; ++id) {

            String rawArp = config.getProperty("arp."+id+".entries");
            String allArp[] = rawConfig.split("[,]");
            for (String route : allArp) {

                if (route.length() > 0) {

                    String splitRoute[] = route.split("[-][>]");
                    int ip = new Integer(splitRoute[0]);
                    int mac = new Integer(splitRoute[1]);
                    network.putArpEntry(ip, id, mac);

                }
            }

        }

    }

    
}
