

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe ClientHandler implementa il task che gestisce la comunicazione con un client
 * @author Fabrizio Cau
 * 
 */
public class ClientHandler implements Runnable {

    private SocketChannel client;

    
    /**
     * Costruttore della classe ClientHandler
     * @param c parametro SocketChannel con la connessione avviata con il client
     * 
     */
    public ClientHandler(SocketChannel c) {   
        client = c;
    }


    public void run() {

        ByteBuffer buf = ByteBuffer.allocate(Config.bufSize);
        String[] params;
        String LoggedUser = ""; // Username dell'utente loggato
        String Response = ""; // Valore di ritorno da restituire al client
        String ClientNetInfo = ""; // Indirizzo remoto del client connesso (per log)
        // Dati ricevuti da client:
        int RequestLength = 0;
        byte[] RequestBytes = null;
        String Request = "";
        
        // Recupera indirizzo e porta remota del client (a fini di log)
        try { ClientNetInfo = client.getRemoteAddress().toString(); } 
        catch (IOException e) { Log.printErr("Errore: impossibile recuperare indirizzo del client",e); }
        boolean clientIsIn = true;

        // Loop di richiesta/risposta 
        while(clientIsIn) {
            buf.clear();
            Response = "";
            
            // Lettura dati ricevuti dal client 
            try { client.read(buf); } 
            catch (IOException e) {  
                Log.printErr("Errore lettura dati da client"); 
                break;
            }
            
            buf.flip();
            try {
                RequestLength = buf.getInt(); // Valore iniziale indica la dimensione del messaggio
                RequestBytes = new byte[RequestLength];
                buf.get(RequestBytes);   
            }
            catch (BufferUnderflowException | BufferOverflowException e) {
                Log.printErr("Errore lettura dati dal client",e);
                break;
            }
            Request = new String(RequestBytes);

            // Stampa il comando ricevuto dall'utente (a fini di log)
            Log.printCom("["+ClientNetInfo+" as '"+LoggedUser+"']: "+ Request.toUpperCase());

            params = Request.split(" "); 

            // Gestione comandi possibli
            switch(params[0].toLowerCase()) {
                case "login": {  // login <userId> <password>
                    String username = params[1].toLowerCase();
                    
                    User u = db.UsersList.get(username);
                    if(u != null) {
                        if(u.isPasswordCorrect(params[2])) {
                            // Login success
                            LoggedUser = username;
                            Response = "Login success"; 
                            Log.printCom(LoggedUser+" is now logged"); 
                            break;
                        } 
                    } 
                    Response = "Login failure";
                } break;
                case "logout": {
                    LoggedUser = "";
                    Response = "Logout success";
                } break;
                case "list": switch(params[1].toLowerCase()) {
                    case "users": {      // list users      
                        List<User> lu = getFollowableUsers(LoggedUser);
                        Response = String.format(" %-20s| %-30s\n----------------------------------------------\n","Utente","Tags");
                        if(lu.size() > 0)
                            for(User u : lu) 
                                Response += String.format(" %-20s| %-30s\n",u.getUser(),u.getTagString()); 
                        else 
                            Response += "Nessun utente con tag comuni trovato\n"; 
                        Response += "----------------------------------------------\n";
                    } break;
                    case "following": {  // list following  
                        Set<String> f = db.UsersList.get(LoggedUser).getFollowing(); 
                        if(f.size() > 0)
                            for(String e : f)
                                Response += e+"\n";
                        else Response = "Non segui alcun utente";
                    } break;
                } break;
                case "follow": {     // follow <userId>
                    // Controllo se esiste l'utente indicato nel parametro o se è lo stesso user loggato
                    String username = params[1].toLowerCase();
                    if(db.UsersList.containsKey(username) && !username.equals(LoggedUser)) {
                        // Aggiorno lista di following e follower di diascuno
                        db.UsersList.get(username).addFollower(LoggedUser);
                        Response = db.UsersList.get(LoggedUser).addFollowing(username);
                        // Aggiorno il client con una callback
                        try { db.RmiCallbackService.addFollowerUpdate(LoggedUser, username); } 
                        catch (RemoteException e) { 
                            Log.printErr("Errore: impossibile inviare aggiornamento follower ("+LoggedUser+"->"+username+")",e); 
                        }
                    } else 
                        Response = "Utente non valido";
                } break;
                case "unfollow": {   // unfollow <userId>
                        // Controllo se esiste l'utente indicato nel parametro
                        String username = params[1].toLowerCase();
                        if(db.UsersList.containsKey(username)) {
                            // Aggiorno lista di following e follower di diascuno
                            db.UsersList.get(username).removeFollower(LoggedUser);
                            Response = db.UsersList.get(LoggedUser).removeFollowing(username);
                            // Aggiorno il client con una callback
                            try { db.RmiCallbackService.removeFollowerUpdate(LoggedUser, username); } 
                            catch (RemoteException e) { 
                                Log.printErr("Errore: impossibile inviare aggiornamento follower ("+LoggedUser+"-/>"+username+")",e); 
                            }
                        } else Response = "Utente "+username+" non esiste";
                    } break;
                case "blog": {         
                    for(Map.Entry<Integer,Post> p : db.PostsList.entrySet())
                        // if autore del post = utente loggato OR post è tra quelli rewinned del utente loggato
                        if(p.getValue().getAutor().equals(LoggedUser) || 
                            p.getValue().getRewinnedList().contains(LoggedUser))
                            Response += p.getValue().getPostPreview()+"\n";
                    Response = printPostPreviewTable(Response);
                } break;
                case "post": { // post <"title"> <"text">
                    // Provo ad estrapolare titolo e testo all'interno dei doppi apici
                    Pattern pat = Pattern.compile("\"([^\"]*)\"");
                    Matcher mat = pat.matcher(Request);
                    String title, msg;
                    try {
                        mat.find();
                        title = mat.group(1);
                        mat.find();
                        msg = mat.group(1);
                    }
                    catch(IllegalStateException e) {
                        Response = "Errore: titolo o testo non compresi tra doppi apici";
                        break;
                    }
                    // Verifico la lunghezza massima del titolo e del messaggio
                    if(title.length()==0 || title.length() > 20 || msg.length()==0 || msg.length() > 500) {
                        Response = "Titolo o messaggio non validi";
                        break;
                    }       
                    // Entro in un blocco sincronizzato per aggiungere il post e aggiornare l'ultimo id
                    synchronized (db.LastPostId) {
                        while(true) {
                            if(db.PostsList.putIfAbsent(db.LastPostId, new Post(db.LastPostId, LoggedUser, title, msg)) == null) {
                               break;
                            }
                            db.LastPostId++;
                        }
                    }
                    Response = "Nuovo post pubblicato!";
                    
                } break;
                case "show": switch(params[1].toLowerCase()) {
                    case "feed": {  // show feed
                        List<Post> lp = getFeedPosts(LoggedUser);
                        for(Post p : lp)
                            Response += p.getPostPreview()+"\n";
                        Response = printPostPreviewTable(Response);                         
                    } break;
                    case "post": { // show post <postId>
                    try {
                        // Controllo se il postId passato esiste
                        Post id = db.PostsList.get(Integer.parseInt(params[2]));
                        if((id) != null) {
                            Response = id.getCompletePost();    }
                        else { Response = "PostId non valido"; }
                            
                    } catch (NumberFormatException e) { Response = "Parametro <postId> non valido";}        
                    } break;    
                } break;
                case "delete": { // delete <postId>
                    try {
                        int id = Integer.parseInt(params[1]);
                        Post p = db.PostsList.get(id);
                        // Controllo se il postId passato esiste
                        if((p) != null) {
                            // Controllo se LoggedUser è l'autore del post
                            if(p.getAutor().equals(LoggedUser)) {
                                db.PostsList.remove(id);
                                db.LastPostId = id;
                                Response = "Ok";
                            } else Response = "Non si possono eliminare post di cui non si e' l'autore";
                        }
                        else Response = "PostId non valido"; 
                    } catch (NumberFormatException e) { Response = "Parametro <postId> non valido";}
                    } break;
                case "rewin": {  // rewin <postId> 
                    try {
                        Post p = db.PostsList.get(Integer.parseInt(params[1]));
                        // Controllo se il post postId passato esiste ed è nel feed dell'utente loggato
                        if(p != null) {
                            if(getFeedPosts(LoggedUser).contains(p))
                                Response = p.addRewin(LoggedUser);
                            else 
                                Response = "Non puoi fare rewin di un post che non appartiene al tuo feed";
                        } else Response = "Post "+params[2]+" non trovato";
                    } catch (NumberFormatException e) { Response = "Parametro <postId> non valido";}  
                } break;
                case "rate": {   // rate <postId> <vote>   
                    try {
                        Post p = db.PostsList.get(Integer.parseInt(params[1]));
                        // Controllo se il post postId passato esiste ed è nel feed dell'utente loggato
                        if(p != null) {
                            if(getFeedPosts(LoggedUser).contains(p))
                                Response = p.ratePost(LoggedUser, Integer.parseInt(params[2]));
                            else 
                                Response = "Non puoi votare un post che non appartiene al tuo feed";
                        } else 
                            Response = "Post "+params[1]+" non trovato";  
                    } catch (NumberFormatException e) { Response = "Parametri non validi";}  
                } break;
                case "comment": {// comment <postId> <text>
                    try {
                        Post p = db.PostsList.get(Integer.parseInt(params[1]));
                        // Controllo se il post postId passato esiste ed è nel feed dell'utente loggato
                        if(p != null) {
                            if(!p.getAutor().equals(LoggedUser)) {
                                if(getFeedPosts(LoggedUser).contains(p)) {
                                    String t = Request.substring(params[1].length()+8);
                                    if(t.length() <= 500)
                                        Response = p.addComment(new Comment(LoggedUser, t));
                                    else Response = "Il commento supera i 500 caratteri";
                                } else 
                                    Response = "Non puoi commentare un post che non appartiene al tuo feed";
                            } else 
                                Response = "Non puoi commentare un tuo post";
                        }
                        else Response = "Post "+params[1]+" non trovato";    
                    } catch (NumberFormatException e) { Response = "Parametro <postId> non valido";}  
                } break;
                case "wallet": switch(Request) {
                    case "wallet" : {       // wallet
                        LocalDateTime t = null;
                        Response = String.format("Portafoglio: %.6f Wincoin\nLista transazioni: \n"+
                                                "---------------------------------------\n", 
                                                db.UsersList.get(LoggedUser).getWallet());
                        for(Transaction tr : db.UsersList.get(LoggedUser).getTransactions()) {
                            t = tr.getDate();
                            Response += String.format(" %6.6f | %ta %td %tB %tY - %tH:%tM:%tS\n", tr.getValue(), t, t, t, t, t, t, t);
                        } 
                    } break;
                    case "wallet btc": {    // wallet btc
                        Double w = db.UsersList.get(LoggedUser).getWallet();
                        Double b = WincoinToBtc(w);
                        LocalDateTime t = LocalDateTime.now();
                        if(b==0 && w>0)
                            Response = "Impossibile recuperare il tasso di cambio in BTC in questo momento";
                        else
                            Response = String.format("Il tuo portafoglio ha un valore pari a: %.6f Wincoin\n"+
                            "Attualmente equivalgono a %.6f BTC (%ta %td %tB %tY - %tH:%tM:%tS)", w, b, t, t, t, t, t, t, t); 
                            
                    } break;
                    default : { 
                        Response = "Comando non valido"; 
                    } break;
                } break;
                case "request_followers" : {    // Comando interno post login per l'invio dei followers
                    // Invio all'utente loggato, tramite RMI callback, della lista completa dei suoi follower
                    try { db.RmiCallbackService.sendFollowers(db.UsersList.get(LoggedUser).getFollowers(),LoggedUser); } 
                    catch (RemoteException e) { 
                       Response = "Errore nel invio della lista dei follower";
                       Log.printErr(Response,e); 
                       break;
                    }
                    Response = "Followers ok";
                } break;
                case "request_multicast" : {    // Commando interno post login per l'invio dei riferimenti multicast
                    Response = Config.MulticastAddress+":"+Config.MulticastPort;
                } break;
                case "client_quit" : {          // Comando interno: il client annuncia l'uscita
                    LoggedUser = "";
                    clientIsIn = false;
                } break;
                default : { // Comando non valido
                    Response = "Comando non valido"; 
                } break;
                
            }
            // Invio della risposta al client
            try {    
                buf.clear();
                buf.putInt(Response.getBytes().length);
                buf.put(Response.getBytes());
                buf.flip();
                client.write(buf);
            }
            catch (BufferOverflowException | IOException e) {
                Log.printErr("Errore scrittura risposta al client",e);
                break;
            }
        } // endwhile clientIsIn = client is out

        // Chiusura connessione con il client
        if(client!=null) {
            if(client.isOpen()) {
                try { client.close(); } 
                catch (IOException e) { Log.printErr("Errore chiusura struttura client"); }
            }
        }
        Log.printInfo("Client ["+ClientNetInfo+"] disconnesso"); 
    }


    /* METODI DI SUPPORTO */
    
    
    
    /**
     * Restituisce la lista degli utenti con tag in comune con logUser 
     * @param logUser username dell'utente loggato
     * @return lista degli username
     */
    public static List<User> getFollowableUsers(String logUser) {
        List<User> followableUsers = new ArrayList<>();
        List<String> t = db.UsersList.get(logUser).getTagList();
        for(Map.Entry<String,User> e : db.UsersList.entrySet()) 
            if(e.getValue().matchTags(t) && !e.getKey().equals(logUser))
                followableUsers.add(e.getValue());
        return followableUsers;
    }

    
    /**
     * Restituisce la lista dei post del feed di un utente
     * @param logUser username dell'utente 
     * @return lista dei post
     */
    public static List<Post> getFeedPosts(String logUser) {
        List<Post> lp = new ArrayList<>();
        // Utenti che l'utente loggato segue
        Set<String> loggedUserFollows = db.UsersList.get(logUser).getFollowing();
        for(Map.Entry<Integer,Post> p : db.PostsList.entrySet())
            // Se l'autore del post è nella lista dei following del utente loggato OR 
            // il post è stato rewinnato da almeno uno dei follow dell'utente loggato
            if( loggedUserFollows.contains(p.getValue().getAutor()) || 
                !Collections.disjoint(p.getValue().getRewinnedList(), loggedUserFollows))
                lp.add(p.getValue());
        return lp;
    }


    
    /**
     * Restituisce la stringa con la tabella delle anteprime dei post in postLines
     * @param postLines stringa con la lista dei post
     * @return stringa con la tabella completa
     */
    public static String printPostPreviewTable(String postLines) {
        return String.format(" %-5s | %-20s | %-20s | %-5s\n--------------------------------------------------------------\n"
        +postLines,"ID:", "Autore:", "Titolo:", "Rewins:");
    }


     
    /**
     * Restiutisce la conversione Wincoin -> Bitcoin
     * @param w valore in Wincoin
     * @return valore in Bitcoin
     */
    public static Double WincoinToBtc(Double w) {
        
        String response;
        Double btc = 0.0;

        int RndBase = 100000; 
        int min = (int) (RndBase-(RndBase*Config.WcoinBtcFluctRange));
        int max = (int) (RndBase+(RndBase*Config.WcoinBtcFluctRange));
        if(min>=max) max=min+1; // Evita l'invio di dati non permessi dal host
        String url = "https://www.random.org/integers/?num=1&min="+min+"&max="+max+"&col=1&base=10&format=plain&rnd=new";
        try ( BufferedReader in = new BufferedReader(new InputStreamReader( new URL(url).openStream())) ) {
            response = in.readLine();
            btc = w*( (Double.parseDouble(response))/RndBase);
        } 
        catch (NumberFormatException | NullPointerException e) {
            Log.printErr("Errore: impossibile recuperare numero casuale dall'host",e); 
            return 0.0;
        }
        catch (MalformedURLException e) { 
            Log.printErr("Errore: generazione URL non riucita",e); 
            return 0.0;
        }
        catch (IOException e) { 
            Log.printErr("Errore: apertura connessione HTTP non riuscita",e); 
            return 0.0;
        } 

        return btc; 
    }


}
