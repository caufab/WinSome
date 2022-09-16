

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
/**
 * Classe che definisce il tipo di dato che rappresenta il post 
 * @author Fabrizio Cau
 * 
 */
public class Post {
    
    // ID del post (copia della chiave nell'hashmap per comodità di implementazione)
    private Integer id;
    // Username dell'autore, titolo e testo del post
    private String autorId, title, text; 
    // Lista di username (userId) di utenti che hanno fatto up/downVote il post
    private Set<String> upVotes, downVotes;
    // Lista di username che hanno rewinnato questo post
    private Set<String> rewinnedUserIds;
    // Lista di commenti associato all'autore del commento
    private List<Comment> comments;
    // Numero di volte che il Rewarder ha valutato questo post
    private Integer RewardedIteration;
    // Flag che indica se il post è stato modificato (Votes/commenti) dall'ultima iterazione di reward
    private boolean Changed;


    
    /**
     * Costruttore classe Posts: crea un nuovo post e inizializza le strutture dati interne
     * @param postId id del post
     * @param autorId username dell'autore del post
     * @param title titolo del post
     * @param text contenuto del post
     */
    public Post(Integer postId, String autorId, String title, String text) {
        this.id = postId;
        this.autorId = autorId;
        this.title = title;
        this.text = text;
        this.upVotes = new HashSet<>();
        this.downVotes = new HashSet<>();
        this.rewinnedUserIds = new HashSet<>();
        this.comments = new ArrayList<>();
        this.RewardedIteration = 0;
        this.Changed = false;
    }

    
    /**
     * @return Restituisce l'id del post
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return Restituisce username dell'autore del post
     */
    public String getAutor() {
        return autorId;
    }

    
    
    /**
     * @return Restituisce l'anteprima del post (Id, Titolo, testo, autore e numero di rewin)
     */
    public String getPostPreview() {
        return String.format(" %-5s | %-20s | %-20s | %-5s", id, autorId, title, rewinnedUserIds.size());
    }   

    // 
    /**
     * @return Restituisce il post completo (con voti, lista dei rewinner, e commenti)
     */
    public String getCompletePost() {
        String s = "Titolo: '"+title+
                    "'\nTesto:'"+text+
                    "'\nAutore: "+autorId+
                    "\nVoti: "+upVotes.size()+" positivi, "+downVotes.size()+
                    " negativi\nRewinned by: ";
        for(String r : rewinnedUserIds)
            s += r+", ";
        s += "\nCommenti:\n";
        for(Comment k : comments) 
            s += k.getComment()+"\n"; 
        return s;
    }


    /**
     * @return Restituisce numero di voti positivi del post
     */
    public Set<String> getUpVotes() {
        return this.upVotes;
    }

    /**
     * @return Restituisce numero di voti negativi del post
     */
    public Set<String> getDownVotes() {
        return this.downVotes;
    }

    /**
     * @return Restituisce lista dei commenti del post
     */
    public List<Comment> getComments() {
        return this.comments;
    }

    /**
     * Aggiunge un commento al post
     * @param c commento da aggiugnere
     * @return Restituisce un messaggi di conferma
     */
    public String addComment(Comment c) {
        comments.add(c);
        Changed = true;
        return "Nuovo commento aggiunto";
    }

    
    /**
     * Aggiunge l'utente nella lista dei voti +/- in base a v
     * @param u username da aggiungere 
     * @param v voto {1,-1}
     * @return Restituisce un messaggio di conferma/errore
     */
    public String ratePost(String u, int v) {
        // Controlla se l'utente u che vuole votare è l'autore del post
        if(!u.equals(this.autorId)) { 
            if(v == 1) { // upVote
                // Controlla se l'utente ha già votato
                if(!upVotes.contains(u)) {
                    // Se chiede di votare + ma aveva già votato -, rimuovo il voto -
                    downVotes.remove(u);
                    upVotes.add(u);
                    Changed = true;
                    return "UpVote registrato";
                } else return "Hai gia' votato questo post";
            } 
            else if(v == -1) { // downVote
                // Controlla se l'utente ha già votato
                if(!downVotes.contains(u)) {
                    // Se chiede di votare - ma aveva già votato +, rimuovo il voto +
                    upVotes.remove(u);
                    downVotes.add(u);
                    Changed = true;
                    return "DownVote registrato";
                }
                else return "Hai gia' votato questo post";
            } 
            else return "Parametro voto non valido";
        }
        else return "Non si puo' votare il proprio post";
    }

    
    /**
     * @return Restituisce la lista degli user che hanno rewinnato questo post
     */
    public Set<String> getRewinnedList() {
        return rewinnedUserIds;
    }

    
    /**
     * Aggiunge l'utente alla lista degli utenti che hanno rewinnato questo post
     * @param u username dell'utente da aggiungere
     * @return Restituisce un messaggio di conferma/errore
     */
    public String addRewin(String u) {
        if(rewinnedUserIds.contains(u))
            return "Hai gia' fatto rewin su questo post";
        else if(autorId.equals(u))
            return "Non puoi fare rewin dei post di cui sei autore";
         
        rewinnedUserIds.add(u);
        return "Rewin registrato";
    }

    
    /**
     * @return Restituisce il numero di volte che il Rewarder ha valutato questo post
     */
    public int getRewardIt() {
        return RewardedIteration;
    }

    
    /**
     * Incrementa il numero di volte che il Rewarder ha valutato questo post
     */
    public void incRewardIt() {
        RewardedIteration++;
    }

    
    
    /**
     * @return Restituisce il flag Changed
     */
    public boolean hasChanged() {
        return Changed;
    }

    
    /**
     * Setta il flag Unchanged al valore u
     * @param u valore da assegnare al flag Changed
     */
    public void setChanged(boolean u) {
        this.Changed = u;
    }


    /**
     * Crea una deep copy del post
     * @return nuovo oggetto post con stessi valori
     */
    public Post getDeepCopy() {
        Post n = new Post(id, autorId, title, text);
        for(String v : upVotes)
            n.ratePost(v, 1);
        for(String v : downVotes)
            n.ratePost(v, -1);
        for(Comment c : comments)
            n.addComment(c);
        for(String r : rewinnedUserIds) 
            n.addRewin(r);
        n.setRewardedIt(RewardedIteration);
        n.setChanged(Changed);
        return n;
    }

    /**
     * Setta il numero di volte che il Rewarder ha valutato questo post
     * (Solo uso locale per deepcopy) 
     */
    private void setRewardedIt(int r) {
        this.RewardedIteration = r;
    }


}





