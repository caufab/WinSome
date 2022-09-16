

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * Implementazione dell'interfaccia RMI per la registrazione di un utente tramite RMI
 * @author Fabrizio Cau
 * 
 */
public class RMIComandList implements RMICommandListInterface {


    public RMIComandList() {
    }
    
    
    /** 
     * Metodo RMI per richiedere la registrazione di un nuovo Utente
     * @param line comando intero da parsare "register <username> <password> <tags>"
     * @see RMICommandListInterface#register(java.lang.String)
     * @return Restituisce sempre un messaggio di conferma/errore sia al chiamante remoto che al server (log)
     */
    public String register(String line) throws RemoteException {
        // Parsing del comando
        String[] lineCom = line.split(" ");
        for(int i=3;i<lineCom.length;i++) lineCom[i] = lineCom[i].toLowerCase(); // tag lower case 
        List<String> tags = Arrays.asList(Arrays.copyOfRange(lineCom, 3, lineCom.length));
        // Controlla se username esiste già e se rispetta la lunghezza massima (20 char)
        String username = lineCom[1].toLowerCase();
        if(username.length()>20) 
            return "Username troppo lungo (max 20 caratteri)";

        // Calcola l'hash della password
        String HashPass = null;
        if( (HashPass=Hash.toHash(lineCom[2])) == null) {
            Log.printErr("Errore: impossibile generare l'hash della password");
            return "Errore: impossibile registrare l'utente";
        }
        // Inserisce il nuovo utente nella lista
        if(db.UsersList.putIfAbsent(username, new User(username, HashPass, tags)) != null) {
            Log.printCom("Username '"+username+"' gia' presente\n"); 
            return "Esiste gia' lo username "+username;
        } else {
            Log.printCom("Username '"+username+"' è stato registrato\n"); 
            return "ok";
        }
    }
    
   

}
