
package br.ufpb.di.redes.layers.application;

import br.ufpb.di.redes.layers.transport.interfaces.Connection;
import br.ufpb.di.redes.layers.transport.interfaces.Transport;
import java.io.*;
import java.net.*;
import java.util.* ;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Servidor{

    private static Logger logger = LoggerFactory.getLogger(Servidor.class);

    ArrayList clientOutputStreams;
    
    Transport transport;

    public Servidor(Transport transport) {
        this.transport = transport;
    }




    public ArrayList getClientes () {
        return clientOutputStreams;
    }

//    public static void main (String[] args){
//        new Servidor().go();
//    }

    public void go(){
        clientOutputStreams = new ArrayList() ;

        try{
            //ServerSocket serverSocket = new ServerSocket(9800);
            int port = 0;


            while(true){
                Connection clientSocket = transport.listen(port);
                DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());

                clientOutputStreams.add(writer);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start() ;

                System.out.println("get a connection");
                logger.debug("Conectado:\n{}", clientSocket);
                ++port;
            }

        }catch (Exception ex){
                ex.printStackTrace();
        }

    }//close go

    public void tellEveryone(String message) {
        Iterator it = clientOutputStreams.iterator();

        while(it.hasNext()){
            try {
                DataOutputStream writer = (DataOutputStream) it.next();
                writer.writeUTF(message);
                //writer.println(message);
                //writer.flush() ;
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }//end while
    }//close tellEveryooe


        //thread escrita
    public class ClientHandler implements Runnable {
        DataInputStream reader;
        Connection sock;
        String nome;

        public String getNome(){
            return nome;
        }

        public void setNome(String nome){
            this.nome = nome;
        }

        public ClientHandler (Connection clientSocket) {

            try {

                sock = clientSocket;
                setNome(JOptionPane.showInputDialog("Digite seu nick: "));
                
                reader = new DataInputStream(sock.getInputStream()) ;
                //reader = new BufferedReader(isReader);

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }//construct

        public void run () {
            String message;

            try{
                while (true) {
                    message = reader.readUTF();

                    System.out.println( "read " + message) ;
                    tellEveryone(nome + ": " + message);
                }
                // close while
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }// close run




    }// close inner class

}