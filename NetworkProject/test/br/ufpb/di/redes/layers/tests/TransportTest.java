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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import junit.framework.TestCase;

/**
 *
 * @author Thiago
 */
public class TransportTest extends TestCase {

    public void setUp() throws IOException {

        Properties config = new Properties();
        config.load(new FileInputStream("topology.properties"));

        Machine machines[] = new Machine[new Integer(config.getProperty("machine.count"))];
        SubNetwork networks[] = new SubNetwork[new Integer(config.getProperty("network.count"))];

        initMachines(machines, config);
        initSubnetworks(networks, machines, config);

        configureCheaterPhysicals(machines, config);

        interNetwork = new InterNetwork();
        interNetwork.allMachines = machines;
        interNetwork.networks = networks;

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
            String currentNetworkPrefix = networkPrefix+(i+1)+".";
            ipArray[i] = new Integer(topology.getProperty(currentNetworkPrefix+"ip"));
        }

        machine.network = getNetworkLayer(machineId, machine.datalink, ipArray);

    }

    private void initTransport(Machine machine, int machineId) {
        machine.transport = getTransportLayer(machineId, machine.network);
    }

    private void initSubnetworks(SubNetwork[] networks, Machine allMachines[], Properties config) {
        for (int i = 0; i < networks.length; ++i) {
            networks[i] = new SubNetwork();
            String rawMachines = config.getProperty("network."+(i+1)+".machines");
            String splitMachines[] = rawMachines.split(",");
            
            Machine machines[] = new Machine[splitMachines.length];
            
            for (int j = 0; j < machines.length; ++j) {
                machines[j] = allMachines[(new Integer(splitMachines[j])) - 1];
            }

            networks[i].machines = machines;
        }
    }

    private void configureCheaterPhysicals(Machine[] machines, Properties config) {
        for (int i = 0; i < machines.length; ++i) {
            Machine curMachine = machines[i];
            for (int j = 0; j < curMachine.physical.length; ++j) {
                Physical curPhysical = curMachine.physical[j];

                if (curPhysical instanceof CheaterPhysicalLayer) {
                    CheaterPhysicalLayer asCheater = (CheaterPhysicalLayer) curPhysical;
                    String out = config.getProperty("machine."+(i+1)+".datalink."+(j+i)+".out");
                    String splitOut[] = out.split(".");

                    int outMachine = new Integer(splitOut[0]);
                    int outPhysical = new Integer(splitOut[1]);

                    asCheater.forward = machines[outMachine].physical[outPhysical];
                    
                }
            }
        }
    }

    public InterNetwork interNetwork;

    public void testListenConnect() {



    }

    private CheaterPhysicalLayer getPhysicalLayer(int id) {
        return new CheaterPhysicalLayer(16, 16, null);
    }

    private DataLink getDataLinkLayer(int machineId, int id, Physical downLayer, int mac) {
        return new CheaterDataLink(downLayer, id, 10, 50, null, mac);
    }

    private Network getNetworkLayer(int machineId, DataLink[] datalink, int[] ipArray) {
        return new CheaterNetwork(datalink, 10, 50, ipArray, null);
    }

    private Transport getTransportLayer(int machineId, Network downLayer) {
        return new CheaterTransport(downLayer, 10, 50);
    }

}
