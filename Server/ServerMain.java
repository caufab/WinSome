
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * Classe ServerMain implementa il metodo Main del server
 * Avvia i costruttori delle classi 
 * Avvia i principali thread per la gestione dei task necessari al funzionamento del server,
 * quali il gestore delle connessioni con i client, il Rewarder e il Backuper 
 * 
 * @author Fabrizio Cau
 * Parameter 
 */
public class ServerMain {
 
 


    public static void main(String[] args)  {
        if(Config.ClsAtStart) cls(); // Clean console
 

        String line = "";
        Scanner inputScanner = null;
  
        // Lancio il costruttore della classe database e config
        
        new Config();
        new Log();
        new db();

       // Avvio del servizio di RMI Callback per l'aggiornamento dei followers
        startRMICallbackService();

        // Attivazione comandi RMI 
        try { 
            RMIComandList list = new RMIComandList();
            RMICommandListInterface stub = (RMICommandListInterface) UnicastRemoteObject.exportObject(list, 0); 
            LocateRegistry.createRegistry(Config.RMIport);
            Registry r = LocateRegistry.getRegistry(Config.RMIport);
            r.rebind(Config.RMIserviceName, stub); // Pubblicazione dello stub nel registry
            Log.printInfo("Server: pubblicato il servizio "+Config.RMIserviceName+" alla porta registry "+Config.RMIport);
        }
        catch(RemoteException e) { Log.printErr("Errore remote",e); }

        
        // Thread che gestisce la connessione con i clients
        Thread CliRec = new Thread(new ClientsReception());
        CliRec.start();

        // Thread Rewarder (Calcolo del reward dei post)
        Thread Rwrder = new Thread(new Rewarder());
        Rwrder.start();

        // Thread Backuper (Backup di post e utenti)
        Thread Bkper = new Thread(new Backuper());
        Bkper.start();
        
        
        System.out.println("Digita 'exit' per uscire");
        // Interazione con la console per la chiusura del server
        while(true) {
            inputScanner = new Scanner(System.in);
            try { line = inputScanner.nextLine(); }// Leggo input dalla tastiera
            catch (NoSuchElementException e) { Log.printErr("Errore lettura comand line",e); System.exit(1); }
            if(line.equals("exit")) break;
            else Log.printCom("Comando non valido");
        }   
        // Chisura del server: lancia le interruzioni ai vari thread e attende la loro terminazione
        inputScanner.close();
        Rwrder.interrupt();
        Bkper.interrupt();
        CliRec.interrupt();
        try { 
            Rwrder.join();
            Bkper.join(); 
            CliRec.join(); 
        } catch (InterruptedException e) { Log.printErr("Errore attesa join durante la terminazione",e); }
        Log.logToFile();
        Log.printInfo("Server chiuso");
        System.exit(1);

    }


    /* FUNZIONI DI SUPPORTO */
 
    
    
    /**
     * Crea e avvia il servizio di RMI Callback per l'invio delle notifiche 
     * asincrone sui nuovi follow/unfollow ai client connessi
     */
    private static void startRMICallbackService() {
        try {
            db.RmiCallbackService = new ServerFollowersUpdImpl();
            ServFollowersUpdInterface servStub = (ServFollowersUpdInterface) UnicastRemoteObject.exportObject(db.RmiCallbackService, 39000);
            LocateRegistry.createRegistry(Config.RMICbPort);
            Registry r = LocateRegistry.getRegistry(Config.RMICbPort);
            r.bind(Config.callbackServiceName, servStub);
        }
        catch(Exception e) {
            Log.printErr("Errore aperura servizio RMI callback",e);
        }
    }

    /**
     * Clean console 
     */
    public static void cls() {  
        System.out.print("\033\143"); 
    }


}
