

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia che definisce il metodo per la registrazione di un utente da remoto
 * @author Fabrizio Cau
 */
public interface RMICommandListInterface extends Remote {

    /** 
     * Metodo RMI per richiedere la registrazione di un nuovo Utente
     * @param line comando intero da parsare 
     * @return Restituisce sempre un messaggio di conferma/errore
     */
    public String register(String line) throws RemoteException;
}
