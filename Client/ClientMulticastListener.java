

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe che definisce il task che si occupa unirsi ad un gruppo 
 * multicast e di restare in ascolto dei messaggi 
 * @author Fabrizio Cau
 * 
 */
public class ClientMulticastListener implements Runnable {

    private static final int maxSize = 32786;
    
    private static int mc_port = 0;
    private static String mc_address = "";

    // Variabile condivisa col thread main che definisce 
    // quando smettere di ascoltare (al logout)
    private volatile AtomicBoolean KeepListening;


    /**
     * Costruttore: assegna i valori di rete del servizio multicast
     * e sincroinizza la variabile booleana dell'utente loggato
     * @param addr indirizzo del servizio multicast
     * @param port porta del servizio multicast
     * @param kl variabile booleana atomica del utente loggato
     * 
     */
    public ClientMulticastListener(String addr, int port , AtomicBoolean kl) {
        mc_address = addr;
        mc_port = port;
        KeepListening = kl;
    }

    public void run() {

        try ( MulticastSocket mc_sock = new MulticastSocket(mc_port); ) { 
            InetAddress mc_group = InetAddress.getByName(mc_address);
            if(!mc_group.isMulticastAddress())
                throw new UnknownHostException();
            else
                mc_sock.joinGroup(mc_group);
                    
            // Ricezione pacchetti
            while(KeepListening.get()) {
                                
                DatagramPacket packet = new DatagramPacket(new byte[maxSize], maxSize);
                
                mc_sock.receive(packet);
                
                if(KeepListening.get())
                   System.out.println("> " + new String(packet.getData()));
                
            }

        }
                catch(UnknownHostException e) { System.err.println("Errore: indirizzo multicasto non valido"); }
                catch(IOException e) { System.err.println("Errore: apertura socket multicast non riuscita"); }
        
    }

}