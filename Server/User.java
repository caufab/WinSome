
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe che definisce il tipo User contenente tutte le informazioni
 * sull'utente registrato nel sistema: username, password, lista di tag,
 * lista di utenti che segue, lista di follower, lista delle transazioni 
 * del suo portafoglio e valore attuale del portafoglio in Wincoin
 * @author Fabrizio Cau
 * 
 */
public class User {
 
    private String username;
    private String password;
    // Lista di tag
    private List<String> tags;
    // Lista di username che questo utente segue e da cui è seguitp
    private Set<String> FollowingUsersId;
    private Set<String> FollowerUsersId;
    // Lista delle transazioni del portafoglio dell'utente
    private List<Transaction> transactionsList;
    // Valore del portafoglio dell'utente
    private double wallet;


    
    /**
     * Costruttore: restituisce un nuovo User con i seguenti parametri e
     * inizializza le strutture dati interne ad esso associate
     * @param username username del utente
     * @param password password hashata
     * @param tags lista di tag (massimo 5)
     */
    public User(String username, String password, List<String> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.FollowingUsersId = new HashSet<>();
        this.FollowerUsersId = new HashSet<>();
        this.transactionsList = new ArrayList<>();
        
    }

    
    /**
     * @return Restituisce username di questo user
     */
    public String getUser() {
        return username;
    }


    
    /**
     * Verifica se la password passata come parametro (in chiaro) 
     * equivale alla stessa memorizzata alla registrazione
     * @param p password (in chiaro)
     * @return true se la password è uguale, false altrimenti
     * 
     */
    public Boolean isPasswordCorrect(String p) {
        return (password.equals(Hash.toHash(p)));
    }

    
    /**
     * @return Restituisce la lista di tag del utente
     */
    public List<String> getTagList() {
        return this.tags;
    }

    /**
     * @return Restituisce la lista di tag del utente come stringa
     */
    public String getTagString() {
        return this.tags.toString().replace("[", "").replace("]", ""); // togliere le [ ]
    }

    
    /**
     * Aggiungere un utente alla lista dei seguiti
     * @param u username dell'utente seguito
     * @return Restituisce un messaggio di conferma/errore
     * 
     */
    public String addFollowing(String u) {
        if(FollowingUsersId.contains(u))
            return "Segui già l'utente "+u;
        else {
            FollowingUsersId.add(u);
            return "ok";
        }
            
    }

    /**
    * Rimuove un utente dalla lista dei seguiti
    * @param u username dell'utente non più seguito
    * @return Restituisce un messaggio di conferma/errore
    * 
    */
    public String removeFollowing(String u) {
        if(FollowingUsersId.contains(u)) {
            FollowingUsersId.remove(u);
            return "ok";
        } else  
            return "Non seguivi l'utente "+u;
        
    }

    /**
    * Aggiunge un utente dalla lista dei follower
    * @param u username dell'utente che ha iniziato a seguire
    * @return Restituisce un messaggio di conferma/errore
    * 
    */
    public void addFollower(String u) {
        FollowerUsersId.add(u);           
    }

    /**
    * Rimuove un utente dalla lista dei follower
    * @param u username dell'utente che non segue più
    * @return Restituisce un messaggio di conferma/errore
    * 
    */
    public void removeFollower(String u) {
        FollowerUsersId.remove(u);
    }


    
    /**
     * Metodo che controlla la corrispondenza dei tag con 
     * quelli passati nell'argomento
     * @param t lista di tag da controllare
     * @return true se i tag combaciano, false altrimenti
     * 
     */
    public boolean matchTags(List<String> t) {
        return !Collections.disjoint(t,tags);
    }

    
    /**
     * @return Restituisce la lista degli username che questo utente segue
     */
    public Set<String> getFollowing() {
        return this.FollowingUsersId;
    }

    /**
     * @return Restituisce la lista degli username seguiti da questo utente
     */
    public Set<String> getFollowers() {
        return this.FollowerUsersId;
    }

    /**
     * @return Restituisce la lista delle transazioni
     */
    public List<Transaction>getTransactions() {
        return this.transactionsList;
    }

    
    /**
     * Aggiunge una transazione alla lista se il valore non è 0
     * @param val valore della transazione
     * @param ts timestamp della transazione
     */
    public void addTransaction(Double val, LocalDateTime ts) {
        if(val==0) return;
        transactionsList.add(new Transaction(val, ts));
        wallet += val;
    }

    
    /**
     * @return Restituisce il valore del portafoglio
     */
    public Double getWallet() {
        return this.wallet;
    }



}
