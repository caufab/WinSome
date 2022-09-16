

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe che implementa i metodi remoti per registrare un servizio di callback
 * per ricevere aggiornamenti asincroni sui nuovi followers o unfollows
 * @author Fabrizio Cau
 * 
 */
public class ServerFollowersUpdImpl extends RemoteObject implements ServFollowersUpdInterface {

    // Struttura che mantiene gli utenti registrati come coppia <username, lista di stub>
    // Può registrare più stub dello stesso utente per permettere l'accesso da più client
    private Map<String, Set<CliFollowersUpdInterface>> clients;

    /**
     * Costruttore: inizializza la struttura che mantiene gli stub dei clients
     */
    public ServerFollowersUpdImpl() {
        super();
        clients = new ConcurrentHashMap<>();
    }


    
    /**
     * Registra la callback di un client. Se l'utente era già presente aggiunge
     * il suo stub alla lista mappata al suo username
     * @see ServFollowersUpdInterface#registerForCallback(CliFollowersUpdInterface)
     * @throws RemoteException
     */
    public synchronized void registerForCallback(CliFollowersUpdInterface clientStub) throws RemoteException {
        Set<CliFollowersUpdInterface> c = null;
        if(clients != null && clientStub!=null) {
            String username = clientStub.getClientId();
            if((c=clients.get(username)) != null)
                c.add(clientStub);
            else {
                c = new HashSet<>();
                c.add(clientStub);
                clients.putIfAbsent(username, c);
            }
                
        }
        else
            Log.printErr("Errore registrazione callback del client");
    }

    
    /**
     * Cancella la registrazione della callback di un client. Se l'utente era 
     * già presente rimuove il suo stub dalla lista mappata al suo username
     * @see ServFollowersUpdInterface#unregisterForCallback(CliFollowersUpdInterface)
     * @throws RemoteException
     * 
     */
    public synchronized void unregisterForCallback(CliFollowersUpdInterface clientStub) throws RemoteException {
        Set<CliFollowersUpdInterface> c = null;
        if(clients != null && clientStub!=null) {
            String username = clientStub.getClientId();
            if((c=clients.get(username)) != null) {
                c.remove(clientStub);
            }
        }
        else
            Log.printErr("Errore cancellazione registrazione callback del client");
    }

    
    /**
     * Aggiorna in modo remoto della aggiunta di un follower all'utente
     * @param follower username del follower
     * @param clientUsername username del followed
     * @throws RemoteException
     * 
     */
    public synchronized void addFollowerUpdate(String follower, String clientUsername) throws RemoteException {
        // Controllo se il follower o il client passati sono null
        if(follower!=null && clientUsername!=null) {
            // Recupero lo stub dei client registrati
            Set<CliFollowersUpdInterface> CliList = clients.get(clientUsername);
            if(CliList != null) { // Utente clientUsername non registrato
                for(CliFollowersUpdInterface c : CliList) {
                   if(c != null) 
                       c.addFollower(follower);
                }
            }
        } else Log.printErr("Errore aggiornamento follower: follower o client sono null");
    }    

    /**
     * Aggiorna in modo remoto della rimozione di un follower dall'utente
     * @param follower username del (ex)follower
     * @param clientUsername username del (ex)followed
     * @throws RemoteException
     * 
     */
    public synchronized void removeFollowerUpdate(String follower, String clientUsername) throws RemoteException {
        // Controllo se il follower o il client passati sono null
        if(follower!=null && clientUsername!=null) {
            // Recupero lo stub dei client registrati
            Set<CliFollowersUpdInterface> CliList = clients.get(clientUsername);
            if(CliList != null) { // Utente clientUsername non registrato
                for(CliFollowersUpdInterface c : CliList) {
                    if(c != null)
                        c.remFollower(follower);
                }
            }
        } else Log.printErr("Errore aggiornamento follower: follower o client sono null"); 
    }


    // 
    /**
     * Aggiorna in modo remoto il client dell'utente clientUsername con una lista di followers
     * Da usare al login oppure (scopi futuri) per un aggiornamento periodico (completo)
     * @param followers lista dei follower
     * @param clientUsername username del followed
     * @throws RemoteException
     * 
     */
    public synchronized void sendFollowers(Set<String> followers, String clientUsername) throws RemoteException {
        // Controllo se il follower o il client passati sono null
        if(followers!=null && clientUsername!=null) {
            // Recupero lo stub dei client registrati
            Set<CliFollowersUpdInterface> CliList = clients.get(clientUsername);
            if(CliList != null) { // Utente clientUsername non registrato
                for(CliFollowersUpdInterface c : CliList) {
                    if(c != null)
                        c.updateAllFollowers(followers);
                }
                return;
            }
        } 
        Log.printErr("Errore aggiornamento follower: lista followers o client sono null");

    }


}
