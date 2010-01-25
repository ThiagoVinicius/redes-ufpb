/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package physicalLayer.UteisSom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Jaduy e Hugo
 */
public class Som {

    public static SourceDataLine openLineToWrite(float sampleRate, int sampleSizeInBits,
            int channels, boolean signed, boolean bigEndian) {

        SourceDataLine linha = null;
        AudioFormat formato = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);/*little-endian*/
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, formato);
        try {
            linha = (SourceDataLine) AudioSystem.getLine(info);
            linha.open(formato);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        linha.start();
        return linha;
    }

    public static TargetDataLine openLineToRead(float sampleRate, int sampleSizeInBits,
                  int channels, boolean signed, boolean bigEndian) {
        TargetDataLine linha = null; //Cria um objeto alvo para captura de sinais sonoros.
        AudioFormat formato = new AudioFormat(48000,8,1,true,true); /*Cria-se o formato especificado; bigEndian*/
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,formato); //Recebe informa??es do sistema de som.
        if(AudioSystem.isLineSupported(info)){ //Verifica se h? uma linha dispon?vel.
            try{
                linha = (TargetDataLine) AudioSystem.getLine(info); /*Recebe a linha.*/
                linha.open(formato); /*Abre a linha.*/
            }catch(LineUnavailableException e){
                e.printStackTrace();
            }
        }
        /*Inicializa a linha de recepção.*/
        linha.start();
        return linha;
    }
}
