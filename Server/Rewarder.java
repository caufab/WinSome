

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Classe Rewarer definisce il task che si occuperà di assegnare i reward ad ogni post
 * @author Fabrizio Cau
 * 
 */
public class Rewarder implements Runnable {

    private Map<Integer, Post> OldPosts;

    /**
     * Costruttore classe Rewarder: inizializza la struttura dei post già valutati
     * e la rende una deep copy della struttura attuale (inizio)
     */
    public Rewarder() {
        this.OldPosts = new ConcurrentHashMap<>();
        syncOldPosts(db.PostsList);
    }


    public void run() {
        while(true) {
            // Attesa periodo di loop
            try { Thread.sleep(Config.RewarderSleepTime); } 
            catch (InterruptedException e) { 
                Log.printInfo("Rewarder interrotto",e);
                break;
            }
            StartRewarding(); // Effetua il calcolo per ogni post e salva le transazioni
            Log.printInfo("Calcolo reward completato");
            // Inizializzazione servizio multicast 
            try ( DatagramSocket ds_sock = new DatagramSocket() ) {
                InetAddress ds_group = InetAddress.getByName(Config.MulticastAddress);
                if(!ds_group.isMulticastAddress())
                    throw new UnknownHostException();
                // Invio del messaggio nel gruppo multicast
                String msg = "Calcolo reward completato";
                byte[] content = msg.getBytes();
                DatagramPacket ds_packet = new DatagramPacket(content, content.length, ds_group, Config.MulticastPort);
                ds_sock.send(ds_packet);
                
            }
            catch (SocketException e) { Log.printErr("Errore: impossibile avviare la socket per l'invio multicast",e); } 
            catch (UnknownHostException e) { Log.printErr("Errore: indirizzo multicast non valido",e); } 
            catch (IOException e) { Log.printErr("Errore: invio multicast non riuscito",e); }
        }
        
    }

    /**
     * Metodo che effettua il controllo di ogni post attuale se ha ricevuto un interazione
     * rispetto all'ultima volta che il post è stato verificato oppure ai post nuovi non
     * presenti nella struttura più vecchia
     * 
     */
    private synchronized void StartRewarding() {
        for(Map.Entry<Integer, Post> e : db.PostsList.entrySet()) {
            if(e.getValue().hasChanged()) { // Il post è stato votato o commentato dall'ultimo rewarding
                Integer occ = 0;
                Double Reward = 0.0;
                Double tmp = 0.0;
                Post np = e.getValue();
                Post op = OldPosts.get(e.getKey());
                
                // Mappa gli autori dei commenti (unici) al numero di voti che hanno pubblicato per il post
                HashMap<String, Integer> UniqueCommentAutors = new HashMap<>();
                // Lista dei commenti e degli upVote users del post attuale (per l'assegnazione della ricompensa curatore)
                List<Comment> lc = new ArrayList<>(np.getComments());
                HashSet<String> RecentUpVotes = new HashSet<>(np.getUpVotes()); 
                int downV = np.getDownVotes().size();
                int upV = RecentUpVotes.size(); // Inizializzazione numero di upVotes recenti da calcolare
                
                if(op != null) { // Post presente nel vecchio calcolo 
                    // Sottrae i commenti, il numero di downVotes e upVotes (tenendo la lista di quest'ultimi) già calcolati
                    lc.removeAll(op.getComments()); 
                    downV = Integer.max((downV-op.getDownVotes().size()), 0);
                    RecentUpVotes.removeAll(op.getUpVotes());
                    upV = RecentUpVotes.size();
                }
                // Incrementa il numero di volte che il post viene calcolato e reseta il flag di cambiamento
                np.incRewardIt();
                np.setChanged(false);

                // Mappo ad ogni unico autore di commenti del post, il numero di volte che ha commentato il post
                for(Comment c : lc) {
                    if((occ=UniqueCommentAutors.get(c.getAutor())) != null)
                        UniqueCommentAutors.put(c.getAutor(), occ+1);
                    else 
                        UniqueCommentAutors.put(c.getAutor(), 1);
                }
                // Calcolo parziale dei per ogni nuovo commento
                for(Map.Entry<String, Integer> uc : UniqueCommentAutors.entrySet())
                    tmp += 2/(1+Math.exp(-(uc.getValue()-1)));

                // Calcolo totale del reward
                Reward = (Math.log(Integer.max((downV*(-1))+upV, 0)+1)+Math.log(tmp+1))/(e.getValue().getRewardIt()); 
                // Aggiunge alla lista degli upVotes (unica) la lista unica degli utenti che hanno commentato (formando la lista dei curatori)
                RecentUpVotes.addAll(UniqueCommentAutors.keySet());

                // Ripartisce il reward tra autore e curatori
                Double UsersReward = (Reward*(Config.UsersShare/100))/RecentUpVotes.size();
                Double AutorReward = Reward*(Config.AuthorShare/100);

                // Aggiunge la transazione del reward all'autore e a ciascun utente curatore
                db.UsersList.get(e.getValue().getAutor()).addTransaction(AutorReward, LocalDateTime.now());
                for(String s : RecentUpVotes) 
                    db.UsersList.get(s).addTransaction(UsersReward, LocalDateTime.now());
            }
        }
        // Sincronizza le maps degli users e dei post old = new in attesa della prossima iterazione
        syncOldPosts(db.PostsList);
    }
    
    /**
     * Sincronizza la struttura dati dei post già visitati con quelli attuali
     * @param newP struttura dati attuale
     * 
     */
    private void syncOldPosts(Map<Integer, Post> newP) {
        for(Map.Entry<Integer, Post> e : newP.entrySet()) 
            OldPosts.put(e.getKey(), e.getValue().getDeepCopy());
    }


}
