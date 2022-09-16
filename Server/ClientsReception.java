

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Classe ClientsReception implementa il task che gestisce lo 'smistamento' delle 
 * connessioni con i client che cercano di connettersi al server, inviando ciascuno
 * al threadpool dove verrà gestito da un thread dedicato
 * @author Fabrizio Cau
 * 
 */
public class ClientsReception implements Runnable {

    private static final int PoolTerminationDelay = 3000;

    ServerSocketChannel ssc = null;
    SocketChannel client = null;
    ExecutorService pool = null;

    public ClientsReception() {
    }

    
    public void run() {
            
        pool = Executors.newCachedThreadPool(); 
        
        // Apertura ServerSocketChannel per connessione TCP con il client
        try { 
            ssc = ServerSocketChannel.open(); 
            ssc.bind(new InetSocketAddress(Config.serverAddr, Config.TCPport));
            ssc.socket().setReuseAddress(true);
       
            Log.printInfo("Server pronto nella porta: "+ Config.TCPport);

            // Loop di gestione delle richieste di connessioni ed invio dei task al threadpool
            while(true) { 
                if((client = ssc.accept()) != null) {
                    Log.printInfo("Nuovo client connesso");
                    pool.execute(new ClientHandler(client));
                }    
            }
        }
        // In caso di ClosedByInterruptException avvio la chiusura di questo task
        catch (ClosedByInterruptException e) { 
            Log.printInfo("Chiusura ClientReception",e); 
            CloseReception(); 
        }
        catch (IOException e) { Log.printErr("Errore IO ServerSocketChannel",e); }

        
    }

    
    /**
     * Metodo per la chiusura delle strutture usate in questa classe
     * 
     */
    private void CloseReception() {
        try { 
            // Chiusura pool
            if(pool!= null) {
                try {
                    pool.shutdown();
                    if(!pool.awaitTermination(PoolTerminationDelay, TimeUnit.MILLISECONDS))
                        pool.shutdownNow();
                }
                catch (InterruptedException e) { pool.shutdownNow(); }
            }
            // Chiusura Socket ed eventuale client   
            if(client!=null) 
                if(client.isOpen()) 
                    client.close();
            if(ssc!=null) 
                if(ssc.isOpen()) 
                    ssc.close();
        } catch (IOException e) { Log.printErr("Errore nella chiusura della ServerSocketChannel",e); }
    }
    
}
