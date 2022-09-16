

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia per la registrazione e cancellazione della registrazione
 * dei servizi RMI callback per l'aggiornamento asincrono dei followers
 * @author Fabrizio Cau
 * 
 */
public interface ServFollowersUpdInterface extends Remote {

    
    /**
     * Permette la registrazione al servizio di callback
     * @param stub stub del client che si vuole registrare
     * @throws RemoteException
     */
    public void registerForCallback(CliFollowersUpdInterface stub) throws RemoteException;

    /**
     * Permette la cancellazione della registrazione al servizio di callback
     * @param stub stub del client che richiede di cancellare la registrazione
     * @throws RemoteException
     */
    public void unregisterForCallback(CliFollowersUpdInterface stub) throws RemoteException;
    
}
