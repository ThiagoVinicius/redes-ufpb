/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpb.di.redes.layers.tests;

import br.ufpb.di.redes.layers.datalink.interfaces.DataLink;
import br.ufpb.di.redes.layers.datalink.test.CheaterDataLink;
import br.ufpb.di.redes.layers.network.interfaces.Network;
import br.ufpb.di.redes.layers.network.test.CheaterNetwork;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import br.ufpb.di.redes.layers.physical.test.CheaterPhysicalLayer;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import br.ufpb.di.redes.layers.transport.test.CheaterTransport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Thiago
 */
public class DefaultTest extends TestCase {

    static {
        try {
            File f = new File("log4j.properties");
            System.setProperty("log4j.configuration", f.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
        }
    }

    public void setUp() throws IOException {

        Properties config = new Properties();
        config.load(new FileInputStream("topology.properties"));

        Machine machines[] = new Machine[new Integer(config.getProperty("machine.count"))];
        Ring networks[] = new Ring[new Integer(config.getProperty("ring.count"))];

        interNetwork = new InterNetwork();
        interNetwork.allMachines = machines;
        interNetwork.networks = networks;

        initMachines(machines, config);
        initSubnetworks(networks, machines, config);

        configureCheaterPhysicals(machines, config);
        configureCheaterDataLinks(networks, machines, config);
        configureCheaterNetworks(machines);

        for (Machine i : machines) {
            i.doStarts();
        }

    }

    public void tearDown() throws Exception {
        interNetwork.allMachines = null;
        interNetwork.networks = null;
    }

    private void initMachines(Machine machines[], Properties topology) {
        for (int i = 0; i < machines.length; ++i) {
            machines[i] = new Machine();

            initDataLinks(machines[i], topology, i+1);
            initNetwork(machines[i], topology, i+1);
            initTransport(machines[i], i+1);
            machines[i].doAttachs();

        }
    }

    private void initDataLinks(Machine machine, Properties topology, int machineId) {

        String datalinkPrefix = String.format("machine.%d.datalink.", machineId);

        int count = new Integer(topology.getProperty(datalinkPrefix+"count"));

        machine.physical = new CheaterPhysicalLayer[count];
        machine.datalink = new DataLink[count];

        for (int i = 0; i < count; ++i) {
            String currentDatalinkPrefix = datalinkPrefix+(i+1)+".";
            int mac = new Integer(topology.getProperty(currentDatalinkPrefix+"mac"));

            machine.physical[i] = getPhysicalLayer(machineId);
            machine.datalink[i] = getDataLinkLayer(machineId, i, machine.physical[i], mac);
        }

    }

    private void initNetwork(Machine machine, Properties topology, int machineId) {
        String networkPrefix = String.format("machine.%d.network.", machineId);
        String datalinkPrefix = String.format("machine.%d.datalink.", machineId);

        int count = new Integer(topology.getProperty(datalinkPrefix+"count"));

        int ipArray[] = new int[count];

        for (int i = 0; i < count; ++i) {
//            String currentNetworkPrefix = networkPrefix+(i+1)+".";
            ipArray[i] = new Integer(topology.getProperty(networkPrefix+"ip."+(i+1)));
        }

        machine.network = getNetworkLayer(machineId, machine.datalink, ipArray);

    }

    private void initTransport(Machine machine, int machineId) {
        machine.transport = getTransportLayer(machineId, machine.network);
    }

    private void initSubnetworks(Ring[] networks, Machine allMachines[], Properties config) {

        for (int i = 0; i < networks.length; ++i) {
            String rawRingInfo = config.getProperty("ring."+(i+1));
            String splitRingInfo[] = rawRingInfo.split("[,]");

            networks[i] = new Ring();
            networks[i].machines = new Machine[splitRingInfo.length];
            networks[i].dataLinkIds = new int[splitRingInfo.length];

            int j = 0;
            for (String curDataLink : splitRingInfo) {
                String splitCurDataLink[] = curDataLink.split("[.]");
                int targetmachine = (new Integer(splitCurDataLink[0])) - 1;
                int targetdatalink = (new Integer(splitCurDataLink[1])) - 1;

                networks[i].machines[j] = allMachines[targetmachine];
                networks[i].dataLinkIds[j] = targetdatalink;

                ++j;

            }

        }

    }

    private void configureCheaterPhysicals(Machine[] machines, Properties config) {
        for (int i = 0; i < machines.length; ++i) {
            Machine curMachine = machines[i];
            for (int j = 0; j < curMachine.physical.length; ++j) {
                Physical curPhysical = curMachine.physical[j];

                if (curPhysical instanceof CheaterPhysicalLayer) {
                    CheaterPhysicalLayer asCheater = (CheaterPhysicalLayer) curPhysical;
                    String out = config.getProperty("machine."+(i+1)+".datalink."+(j+1)+".out");
                    String splitOut[] = out.split("[.]");

                    int outMachine = new Integer(splitOut[0]) - 1;
                    int outPhysical = new Integer(splitOut[1]) - 1;

                    asCheater.forward = machines[outMachine].physical[outPhysical];
                    
                }
            }
        }
    }

    private void configureCheaterDataLinks(
            Ring globalRings[],
            Machine machines[],
            Properties config) {

        int ringcount = new Integer(config.getProperty("ring.count"));

        for (int i = 0; i < ringcount; ++i) {
            String rawRingInfo = config.getProperty("ring."+(i+1));
            String splitRingInfo[] = rawRingInfo.split("[,]");


            Map<Integer, CheaterDataLink> sv_cheats = //just kidding
                    new HashMap<Integer, CheaterDataLink> ();

            int j = 0;
            for (String curDataLink : splitRingInfo) {
                String splitCurDataLink[] = curDataLink.split("[.]");
                int targetmachine = (new Integer(splitCurDataLink[0])) - 1;
                int targetdatalink = (new Integer(splitCurDataLink[1])) - 1;

//                globalRings[i].machineToDataLink[targetmachine] = targetdatalink;
                DataLink target = machines[targetmachine].datalink[targetdatalink];
//                globalRings[i].datalinks[j] = target;


                if (target instanceof CheaterDataLink) {
                    CheaterDataLink asCheater = (CheaterDataLink) target;

                    asCheater.others = sv_cheats;
                    sv_cheats.put(asCheater.mac, asCheater);

                }

                ++j;

            }

        }

    }

    private void configureCheaterNetworks(Machine[] machines) {

        Map<Integer, CheaterNetwork> theNetwork = new HashMap<Integer, CheaterNetwork>();

        for (Machine curMachine : machines) {
            Network curNetwork = curMachine.network;
            if (curNetwork instanceof CheaterNetwork) {

                CheaterNetwork asCheater = (CheaterNetwork) curNetwork;
                asCheater.others = theNetwork;
                for (int curIp : asCheater.ip) {
                    theNetwork.put(curIp, asCheater);
                }
            }

        }

    }

    public InterNetwork interNetwork;

    public CheaterPhysicalLayer getPhysicalLayer(int id) {
        return new CheaterPhysicalLayer(16, 16, null);
    }

    public DataLink getDataLinkLayer(int machineId, int id, Physical downLayer, int mac) {
        return new CheaterDataLink(downLayer, id, 10, 50, null, mac);
    }

    public Network getNetworkLayer(int machineId, DataLink[] datalink, int[] ipArray) {
        return new CheaterNetwork(datalink, 10, 50, ipArray, null);
    }

    public Transport getTransportLayer(int machineId, Network downLayer) {
        return new CheaterTransport(downLayer, 10, 50);
    }

}
