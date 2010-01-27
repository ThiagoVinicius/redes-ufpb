/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physicalLayer.Principal;

import physicalLayer.AgentesDeSom.CaptadorDeSom;
import physicalLayer.AgentesDeSom.TransmissorDeSom;
import physicalLayer.Sinal.Sinal;
import br.ufpb.di.redes.layers.all.InterlayerData;
import br.ufpb.di.redes.layers.physical.interfaces.Physical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Janduy e Hugo
 */

public class Fisica extends Physical{

    TransmissorDeSom transmite = new TransmissorDeSom();
    CaptadorDeSom capta = new CaptadorDeSom();
    
    private Thread capturaThread;
    
    private static final Logger logger = LoggerFactory.getLogger(Fisica.class);    

    @Override
    public void start()
    {
        super.start();
        iniciaCaptura();
    }

    /*public static void main(String[] args) {

        byte[] Um = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL];
        Sinal.escreveUm(Um, 0);
        GraficoTxt.escreveOndaTxt(Um, "Um.txt");


        byte[] Zero = new byte[Sinal.QUANTIDADEAMOSTRAPORSINAL];
        Sinal.escreveZero(Zero, 0);
        GraficoTxt.escreveOndaTxt(Zero, "Zero.txt");


        CaptadorDeSom cdp = new CaptadorDeSom();
        TransmissorDeSom tds = new TransmissorDeSom();
        //cdp.start();
        //tds.start();
    }*/

    @Override
    protected void processSentData(InterlayerData data) {
        transmite.enviaMensagem(data);
    }

    @Override
    public int minPacketSize() {
        return Sinal.QUANTIDADEDESINAIS-4;
    }

    @Override
    public int maxPacketSize() {
        return Sinal.QUANTIDADEDESINAIS-4;
    }

    private void iniciaCaptura() {
        capturaThread = new Thread() {
            @Override
            public void run() {

                byte[] bytes;
                boolean b = true;

                InterlayerData data;

                logger.info("Thread de recebimento iniciada.");

                while(b)
                {
                    data = new InterlayerData(16);
                    bytes = capta.captaMensagem();

                    if(bytes == null)
                    {
                        /*try {
                            Thread.sleep(5);
                        } catch (Exception e) {
                            logger.error("Excessao ", e);
                        }*/
                    }
                    else
                    {
                        for(int i = 2; i < bytes.length-2; i++)
                        {
                            if (bytes[i] == 1) {
                                data.setBit(i-2);
                            }
                            else
                            {
                                data.clearBit(i-2);
                            }
                        }

                        bubbleUp(data);
                    }
                }

                logger.info("Thread de recebimento interrompida.");
            }
        };
        capturaThread.start();
    }
}
