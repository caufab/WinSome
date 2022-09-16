
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Classe che implementa il metodo main del client
 * Definisce tutte le strutture per la connessione con il server
 * e avvia i thread di supporto
 * @author Fabrizio Cau
 */
public class ClientMain {

  

    // Lista degli username dei followers (aggiornata via RMI callback)
    private static Set<String> Followers = null;
    
    // Strutture dati usate nel client
    private static Scanner inputScanner = new Scanner(System.in);
    private static RMICommandListInterface commandList = null; // Comandi RMI
    private static ServFollowersUpdInterface serverRemoteRef = null; // Comandi RMI Callback
    private static CliFollowersUpdInterface stub = null; // Stub del client per la registrazione del RMI Callback
    private static SocketChannel sc = null;
    private static Thread McListener = null; // Thread dedicato alla ricezione dei messaggi multicast
    private static CliFollowersUpdInterface remObj = null;

    // Flag condiviso tra i thread per sapere se l'utente è loggato
    public volatile static AtomicBoolean logged;
    private static boolean isCallbackRegistrered = false;


    public static void main(String args[]) {
        if(Config.ClsAtStart) cls(); // Clean console

        logged = new AtomicBoolean(false);
        Followers = new HashSet<>();

        new Config(); // Recupero file di configurazione
        new ComTools();
        
        String line = "";
        String ServerAnswer = "";
        int k = 1; // Contatore tentativi di connessione  

        // Connessione TCP con il server           
        while(true) {
            k++;
            try {
                sc = SocketChannel.open();
                sc.socket().connect(new InetSocketAddress(Config.serverAddr, Config.TCPport), Config.ServerWaitingTime);
                // Se la connect si sblocca entro ServerWaitingTime il client è connesso, esce dal loop
                if(sc.isConnected()) {                    
                    System.out.println("Connessione TCP col server avvenuta con successo!");
                    break;
                }
            } 
            // Viene sollevata un eccezione (anche per timeout): controlla i tentativi:
            // Se li ha superati esce, altrimenti continua il loop con un nuovo tentativo
            catch (IOException e) { 
                if(k>=Config.ServerWaitingAttempts) {
                    System.err.println("Impossibile connettersi con il server. Riprovare più tardi ");
                    quit();
                }
                else System.err.println("Tentativo di connessione in corso... ");
            }
        }
        
        // Ricerca registry per RMI (registrazione di un nuovo utente)
        try {
            Registry RMIreg = LocateRegistry.getRegistry(Config.RMIport);
            commandList = (RMICommandListInterface) RMIreg.lookup(Config.RMIserviceName);
        }   
        catch(Exception e) { 
            System.err.println("Errore: impossibile stabilire una connessione per la registrazione di un nuovo utente"); 
            quit();
        } 

        // Ricerca registry per RMI callback (aggiornamento asincrono dei followers)
        try {
            Registry RMICbReg = LocateRegistry.getRegistry(Config.RMICbPort);
            serverRemoteRef = (ServFollowersUpdInterface) RMICbReg.lookup(Config.callbackServiceName);
        }
        catch (Exception e) { 
            System.err.println("Errore: impossibile stabilire una connessione per l'aggiornamento remoto dei follower"); 
            quit();
        }
 
        System.out.println("Digita help per conoscere i commandi disponbili");

        // Loop sui messaggi del client (termina alla volonta del client di uscire)
        while(true) {
            System.out.printf("> ");       
            
            try { line = inputScanner.nextLine(); } // Lettura input dalla tastiera
            catch (NoSuchElementException e) { 
                System.out.println("Errore lettura comand line"); 
                quit(); 
            }
            
            String[] params = line.split(" "); // Divisione del comando per spazi
            params[0] = params[0].toLowerCase(); // Prima 'parola' sicuramente è un comando

            if(line.equals("exit")) {
                sendCommand("client_quit", sc); // Invia al server il comando interno per l'uscita
                break;
            }
                   
            /* CONTROLLO COMANDI */
            if(!ComTools.validCom(line)) { // Comando non valido
                System.err.println("Comando non valido");
                continue;
            }       
            if(params[0].equals("help")) { // Richiesta comandi disponibili
                System.out.println(ComTools.helpCom(logged.get()));
                continue;
            }     
            
            if(!logged.get() && ComTools.loggedCom(params[0])) { // Comando necessita login
                System.out.println("Prima di usare il commando "+params[0]+", effettua il login");
                continue;
            }
            if(logged.get() && !ComTools.loggedCom(params[0])) { // Comando necessita logout
                System.out.println("Risulta un utente già connesso, effettua prima il logout");
                continue;
            }
        
            // Comando list followers 
            if(params[0].equals("list")) {
                if(params[1].equals("followers")) {
                    System.out.println("< Followers: ");
                    for(String f : Followers)
                        System.out.println("<  "+f);
                    continue;
                }
            }

            // Registrazione di un nuovo utente (via RMI)
            if(params[0].equals("register")) {     
                try {
                    // Chiamata al metodo remoto per la registrazione e stampa della risposta
                    ServerAnswer = commandList.register(line);
                    System.out.println("< "+ServerAnswer);
                } catch(RemoteException e) { 
                    System.err.println("Errore: impossibile richiedere la registrazione"); 
                }
                continue;
            }

            // Comandi via TCP (login, list, show, post, ...)
            else {    
                ServerAnswer = sendCommand(line, sc); // Invia il comando intero al server
                
                if(ServerAnswer.equals("Login success")) { 
                    System.out.println("< Login effettuato");
                    logged.set(true);
                    // Registrazione per le callback di aggiornamento dei followers
                    try {
                        remObj = new CliFollowersUpdImpl(Followers, params[1]);
                        stub = (CliFollowersUpdInterface) UnicastRemoteObject.exportObject(remObj, 0);
                        serverRemoteRef.registerForCallback(stub);
                        isCallbackRegistrered = true;
                        
                    } catch (RemoteException e) { System.err.println("Errore richiesta registrazione di callback "); }
                    
                    // Client invia un comando (interno) per richiedere la lista dei follower
                    String rf = sendCommand("request_followers", sc);
                    
                    // Server conferma l'invio dei followers
                    if(rf.equals("Followers ok")) {

                        // Richiesta riferimenti multicast
                        String mc = sendCommand("request_multicast", sc);
                        try {
                            String[] mr_arr = mc.split(":");
                            String mc_address = mr_arr[0];
                            int mc_port = Integer.parseInt(mr_arr[1]);

                            // Avvio del thread che riceverà i pacchetti UDP del gruppo multicast
                            McListener = new Thread(new ClientMulticastListener(mc_address, mc_port , logged));
                            McListener.start();
                        } catch (NumberFormatException e) {
                            System.err.println("Impossibile recuperare i dati multicasti: non si potranno ricevere notifiche dal Rewarder");
                            continue; 
                        }
                        continue;
                    } else {
                        System.err.println("Errore server: "+rf);
                    }
                
                } else if(ServerAnswer.equals("Logout success")) {
                    System.out.println("Logout effettuato");
                    logged.set(false);     
                    // Cancellazione registrazione delle callback per l'aggiornamento followers
                    try { serverRemoteRef.unregisterForCallback(stub); } 
                    catch (RemoteException e) {
                        System.err.println("Errore cancellazione della registrazione di callback");
                    }
                    McListener.interrupt();
                }

                // Altre risposte dal server
                else 
                    System.out.printf("\n< %s\n", ServerAnswer.replace("\n", "\n< "));    

            } // end else (not 'register')
        } // end while
        quit();
    } // end main


    /* FUNZIONI DI SUPPORTO */

    
    /**
     * Chiude tutte le strutture usate in questa classe e termina
     */
    private static void quit() {
        // Annullamento registrazione RMI Callback
        if(serverRemoteRef!=null) {
            if(isCallbackRegistrered) {
                try { serverRemoteRef.unregisterForCallback(stub); } 
                catch (RemoteException e) {
                    System.err.println("Errore cancellazione della registrazione di callback");
                }
            }
        }
        // Interruzione thread per l'ascolto dei messaggi multicast
        if(McListener!=null)
            if(McListener.isAlive()) 
                McListener.interrupt();
        // Chiusura scanner input CLI
        inputScanner.close();
        // Chiusura canale socket con il server
        if(sc!=null) {
            try { sc.close(); } 
            catch (IOException e) {
                System.err.println("Errore chiusura SocketChannel");
                System.exit(1);
            }
        }    
        System.out.println("Client chiuso");
        System.exit(1); // Uscita
    }


    
    /**
     * Invia al server il commando 'line' e restituisce la risposta
     * @param line stringa da inviare al canale socket
     * @param sc socket connesso con il server
     * @return Restituisce il messaggio ricevuto dal server
     * 
     */
    private static String sendCommand(String line, SocketChannel sc) {
        ByteBuffer buf = ByteBuffer.allocate(Config.bufSize); 
        int repLen = 0;
        byte[] repByte = null; 
        try {
            buf.clear();
            buf.putInt(line.getBytes().length);
            buf.put(line.getBytes());
            buf.flip();
            
            sc.write(buf);  // Scrittura del buffer sulla SocketChannel col server
            buf.clear();
            sc.read(buf);   // Lettura dalla SocketChannel nel buffer
            
            // RISPOSTA DEL SERVER         
            buf.flip();
            repLen = buf.getInt();
            repByte = new byte[repLen];
            buf.get(repByte);
        }
        // In caso di errori del buffer o di IO della socket connessa al server, il client termina
        catch (BufferOverflowException | BufferUnderflowException | IOException e) {
            System.err.println("Errore connessione con il server");
            quit();
        }
        return new String(repByte);
    }


    /**
     * Clean console 
     */
    public static void cls() {  
        System.out.print("\033\143"); 
    }  

  
    
}

