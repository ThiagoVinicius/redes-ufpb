/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Principal;

import AgentesDeSom.CaptadorDeSom;
import AgentesDeSom.TransmissorDeSom;
import Sinal.Sinal;
import UteisGraficos.GraficoTxt;
import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lucas
 */
public class Fisica extends Physical{

    TransmissorDeSom transmite = new TransmissorDeSom();
    CaptadorDeSom capta = new CaptadorDeSom();
    
    private Thread downThread;
    
    private static final Logger logger = LoggerFactory.getLogger(Fisica.class);

    public Fisica() {
        iniciaCaptura();
    }



    public static void main(String[] args) {

        byte[] Um = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL];
        System.out.println("Pora" + Um.length);
        System.out.println("Pora Pica" + Sinal.QUANTIDADEAMOSTRAPORSINAL);
        Sinal.escreveUm(Um, 0);
        GraficoTxt.escreveOndaTxt(Um, "Um.txt");


        byte[] Zero = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL];
        Sinal.escreveZero(Zero, 0);
        GraficoTxt.escreveOndaTxt(Zero, "Zero.txt");


        CaptadorDeSom cdp = new CaptadorDeSom();
        TransmissorDeSom tds = new TransmissorDeSom();
        //cdp.start();
        //tds.start();
    }

    @Override
    protected void processSentData(InterlayerData data) {
        transmite.enviaMensagem(data);
    }

    @Override
    public int minPacketSize() {
        return Sinal.QUANTIDADEDESINAIS;
    }

    @Override
    public int maxPacketSize() {
        return Sinal.QUANTIDADEDESINAIS;
    }

    private void iniciaCaptura() {
        downThread = new Thread() {
            @Override
            public void run() {

                byte[] bytes;
                boolean b = true;

                InterlayerData data = new InterlayerData(1);

                while(b)
                {
                    bytes = capta.captaMensagem();

                    if(bytes == null)
                    {
                        try {
                            Thread.sleep(5);
                        } catch (Exception e) {
                        }
                    }
                    else
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            if (bytes[i] == 1) {
                                data.setBit(i);
                            }
                            else
                            {
                                data.clearBit(i);
                            }
                        }

                        bubbleUp(data);
                    }
                }

                logger.debug("Thread de recebimento interrompida.");
            }
        };
        downThread.start();
    }
}
