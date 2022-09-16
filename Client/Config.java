import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Classe Config definisce i parametri di configurazione 
 * del client utili ai vari componenti 
 * @author Fabrizio Cau
 *  
 */
public class Config {

    // Path del file config
    private static final String config_file = "./config/config.txt";

    /* Dati config di default */

    // Porta del server RMI, TCP e RMI-callback e indirizzo del server
    public static int RMIport = 9988;
    public static int TCPport = 9989;
    public static int RMICbPort = 9987;
    public static String serverAddr = "localhost";
    
    // Nome dei servizi offerti dal server 
    public static String RMIserviceName = "RMICommandList";
    public static String callbackServiceName = "followerUpdService";
    
    // Tempo di attesa per ritentare la connessione con il server
    public static int ServerWaitingTime = 2000;
    public static long ServerWaitingAttempts = 10;

    // Flag di configurazione
    public static Boolean ClsAtStart = true; // Cancella la console all'avvio del client

    // Dimensione del buffer
    public static final int bufSize = 32768; 
    
    /**
     * Costruttore: richiama la lettura dal file di configurazione
     */
    public Config() {
        readConfig();
    }


    /**
     * Effettua la lettura dal file di configurazione, i parametri
     * che non possono essere letti resteranno quelli di default
     * 
     */
    private void readConfig(){

        try (BufferedReader reader = new BufferedReader(new FileReader(config_file))) {  
        
            String line ="";
            while((line=reader.readLine()) != null) {
                int x = 0;
                if(line.startsWith("#") || line.equals("")) continue; // commento
                String[] w = line.split("=", 2); 
                try {
                    switch (w[0]) {
                    case "RMIport": {
                        x=Integer.parseInt(w[1]);
                         if(x >= 0 && x<65535)
                            RMIport = x;
                    }break;

                    case "TCPport": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 0 && x<65535)
                            TCPport = x;
                    } break;
                        
                    case "RMICbPort": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 0 && x<65535)
                            RMICbPort = x;
                    } break;
                     
                    case "serverAddr": 
                        if(!w[1].equals(""))
                            serverAddr = w[1];
                    break;
                      
                    case "RMIserviceName":
                        if(!w[1].equals(""))
                            RMIserviceName = w[1];
                    break;
                    
                    case "callbackServiceName":
                        if(!w[1].equals(""))
                            callbackServiceName = w[1];
                    break;

                    case "ServerWaitingTime": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 200)
                            ServerWaitingTime = x;
                    } break;
                    
                    case "ServerWaitingAttempts": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 0 && x<100)
                            ServerWaitingAttempts = x;
                    } break;  
                    case "ClsAtStart":
                        ClsAtStart = Boolean.parseBoolean(w[1]);
                    break;  
                          
                    default:
                        break;
                }
                } catch (NumberFormatException e) { System.err.println("Errore nel file config: alcuni dati non possono essere letti. (Default value) "); } 

            }
          
        }
        
        catch (FileNotFoundException e) { System.err.println("Errore: file config non trovato. (Default value)"); } 
        catch (IOException e) { System.err.println("Errore di I/O nella lettura del file config: (Default value)"); }
              
    }


}
