

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Set;

/**
 * Classe che implemeneta i metodi remoti che userà il server per
 * l'aggiornamento asincrono dei follower
 * @author Fabrizio Cau
 * 
 */
public class CliFollowersUpdImpl extends RemoteObject implements CliFollowersUpdInterface {

    Set<String> followerList;
    String userId;

    
    /**
     * Costruttore: riceve il riferimento alla lista di followers del
     * client e il suo username
     * @param followers lista di followers
     * @param id username del client
     */
    public CliFollowersUpdImpl(Set<String> followers, String id) {
        super();
        if(followers!=null && id!=null) {
            this.followerList = followers;
            this.userId = id;
        }
        else 
            System.err.println("Errore riferimento followers (client-size)");
    }

     
    /**
     * Aggiunge un singolo follower
     * @see CliFollowersUpdInterface#addFollower(java.lang.String)
     * 
     */
    public void addFollower(String str) throws RemoteException {
        if(str != null && followerList!=null)
            this.followerList.add(str);
        else 
            System.err.println("Errore aggiunta nuovo follower");
    }

    /**
     * Rimuove un singolo follower
     * @see CliFollowersUpdInterface#remFollower(java.lang.String)
     * 
     */
    public void remFollower(String str) throws RemoteException {
        if(str != null && followerList!=null)
            this.followerList.remove(str);
        else 
            System.err.println("Errore rimozione follower");
    }

    /**
     * Aggiunge in remoto una lista di followers tutti insieme. Da usare al login 
     * oppure nel caso in cui l'aggiornamento dei followers viene fatto periodicamente
     * Non utilizzabile per aggiornamento parziale: la lista dei followers viene sovrascritta!
     * @param listOfFollowers lista dei follower da inserire
     * @throws RemoteException (catturata)
     * 
     */
    public void updateAllFollowers(Set<String> l) throws RemoteException {
        // Mi assicuro che l non sia null ne vuota
        if(l!=null && followerList!=null) {
            this.followerList.clear();
            this.followerList.addAll(l);
        } else         
            System.err.println("Errore aggiornamento followers");
    }

    
    /**
     * Restituisce lo username con cui il client si è loggato
     * @see CliFollowersUpdInterface#getClientId()
     */
    public String getClientId() throws RemoteException {
        return this.userId;
    }


   

}
