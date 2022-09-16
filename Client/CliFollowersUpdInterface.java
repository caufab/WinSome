

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * Interfaccia del client che definisce i metodi remoti (RMI) per 
 * l'aggiornamento remoto dei followers
 * @autor Fabrizio Cau
 */
public interface CliFollowersUpdInterface extends Remote {

    /**
     * Aggiunge da remoto di un singolo follower
     * @param follower follower da aggiungere
     * @throws RemoteException
     */
    public void addFollower(String follower) throws RemoteException;

    /**
     * Rimuove da remoto di un singolo follower
     * @param follower follower da rimuovere
     * @throws RemoteException
     */
    public void remFollower(String follower) throws RemoteException;

    /**
     * Aggiunge in remoto una lista di followers tutti insieme. Da usare al login 
     * oppure nel caso in cui l'aggiornamento dei followers viene fatto periodicamente
     * Non utilizzabile per aggiornamento parziale: la lista dei followers viene sovrascritta!
     * @param listOfFollowers lista dei follower da inserire
     * @throws RemoteException (catturata)
     * 
     */
    public void updateAllFollowers(Set<String> listOfFollowers) throws RemoteException;

    /**
     * Restituisce lo username con cui il client si è loggato
     * 
     */
    public String getClientId() throws RemoteException;

}
