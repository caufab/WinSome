import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Classe Config definisce i parametri di configurazione del server utili 
 * ai vari componenti 
 * @author Fabrizio Cau
 *  
 */
public class Config {
  
    // File di configurazione 
    private static final String config_file = "./config/config.txt";
     
    // VALORI DI DEFAULT 

    // Connessione
    public static int RMIport = 9988; // Porta servizio RMI
    public static int TCPport = 9989; // Porta connessione TCP
    public static int RMICbPort = 9987; // Porta servizio RMI Callback
    public static int MulticastPort = 9986; // Porta servizio multicast
    public static String MulticastAddress = "225.5.10.28"; // Indirizzo servizio multicast
    public static String serverAddr = "localhost"; // Indirizzo server 

    // Nome del servizio RMI offerto dal server
    public static String RMIserviceName = "RMICommandList";
    public static String callbackServiceName = "followerUpdService";

    // Dimensione del buffer 
    public static int bufSize = 32768;
    
    // Nomi del file 
    public static String usersFile = "./database/users.json";
    public static String postsFile = "./database/posts.json";
    public static String logFile = "./log/logFile.txt";
    
    // Tempi di attesa di Rewarder e Backuper
    public static long RewarderSleepTime = 1000000;
    public static long BackuperSleepTime = 500000;

    // Valori di configurazione del Rewarder
    public static Double UsersShare = 0.3; // Parte del reward per gli utenti curatori
    public static Double AuthorShare = 0.7; // Parte del reward per l'autore
    public static Double WcoinBtcFluctRange = 0.2; // Valore di fluttuazione del tasso di cambio [0,1]
    
    // Flag interni
    public static Boolean BackupBeforeClosing = true; // Esegue il backup di post e utenti prima di uscire
    public static Boolean DebugMode = false; // Insieme ai messaggi di errore stampa il messaggio dell'eccezione
    public static Boolean ColoredConsole = true; // Usa colori (Blu e Rosso) per stampare su console informazioni ed errori
    public static Boolean PrintCom = true; // Stampa anche i messaggi di comunicazione
    public static Boolean ClsAtStart = true; // Clean console all'avvio
    
    /**
     * Costruttore della classe Config: avvia il recupero dei parametri di configurazione
     * dal file ./config/config.txt
     * Se il recupero non andasse a buon fine restano validi i parametri di default
     * 
     */
    public Config () {
        readConfig();
    }
 
  
    /**
     * Avvia il recupero dei dati di configurazione dal file
     * 
     */
    private void readConfig(){

        try (BufferedReader reader = new BufferedReader(new FileReader(config_file))) {  
            
            
            String line = "";
            while((line=reader.readLine()) != null) {
                int x = 0;
                Double y = 0.0;
                Long z;
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
                     
                    case "MulticastPort": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 0 && x<65535)
                            MulticastPort = x;
                    } break;

                    case "bufSize": {
                        x=Integer.parseInt(w[1]);
                        if(x >= 32768)
                            bufSize = x;
                    }break;
                    
                    case "MulticastAddress": {
                        InetAddress ia = InetAddress.getByName(w[1]);
                        if(ia.isMulticastAddress())
                            MulticastAddress = w[1];    
                    } break;
                    
                    case "serverAddr": 
                        if(!w[1].equals(""))
                            serverAddr = w[1];
                    break;
                        
                    case "usersFile":
                        if(!w[1].equals(""))
                            usersFile = w[1];
                        break;
                    
                    case "postsFile":
                        if(!w[1].equals(""))
                            postsFile = w[1];
                        break;
                        
                    case "logFile":
                        if(!w[1].equals(""))
                            logFile = w[1];
                        break;
                        
                    case "RMIserviceName":
                        if(!w[1].equals(""))
                            RMIserviceName = w[1];
                        break;
                    
                    case "callbackServiceName":
                        if(!w[1].equals(""))
                            callbackServiceName = w[1];
                        break;
                        
                    case "RewarderSleepTime": {
                        z=Long.parseLong(w[1]);
                        if(z >= 200)
                            RewarderSleepTime = z;
                    } break;
                    
                    case "BackuperSleepTime": {
                        z=Long.parseLong(w[1]);
                        if(z >= 200)
                            BackuperSleepTime = z;
                    }break;
                        
                    case "UsersShare": {
                        y=Double.parseDouble(w[1]);
                        if(y > 0.0 && y <= 1.0)
                        UsersShare = y;
                    }break;
                    
                    case "AuthorShare":{
                        y=Double.parseDouble(w[1]);
                        if(y > 0.0 && y <= 1.0)
                        AuthorShare = y;
                    } break;
                    case "WcoinBtcFluctRange" : {
                        y=Double.parseDouble(w[1]);
                        if(y > 0.0 && y <= 1.0)
                        WcoinBtcFluctRange = y;
                    } break;
                        
                    case "BackupBeforeClosing":
                        BackupBeforeClosing = Boolean.parseBoolean(w[1]);
                        break;
                    case "DebugMode":
                        DebugMode = Boolean.parseBoolean(w[1]);
                        break;
                        
                    case "ColoredConsole":
                        ColoredConsole = Boolean.parseBoolean(w[1]);
                        break;
                        
                    case "PrintCom":
                        PrintCom = Boolean.parseBoolean(w[1]);
                            break;
                    case "ClsAtStart":
                        ClsAtStart = Boolean.parseBoolean(w[1]);
                            break;
                            
                    default:
                        break;
                }
                }catch (NumberFormatException e) { Log.printErr("Errore nel file config: alcuni dati non possono essere letti. (Default value) ",e); } 
            } // end while
          
            if(AuthorShare+UsersShare != 1.0) {
                AuthorShare = 0.7;
                UsersShare = 0.3;
            }
            
        }
        
        catch (FileNotFoundException e) { Log.printErr("Errore: file config non trovato. (Default value)",e); } 
        catch (IOException e) { Log.printErr("Errore di I/O nella lettura del file config: (Default value)",e); }
          
        
    }

}
