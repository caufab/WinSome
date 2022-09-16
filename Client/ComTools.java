

import java.util.HashMap;

/**
 * Classe che definisce una serie di strumenti per la verifica
 * della correttezza del comando inviato
 * @author Fabrizio Cau
 * 
 */
public class ComTools {

    // Struttura dati che mappa i comandi disponibili con il numero 
    // di parametri minimi e massimi
    private static HashMap<String,String> hm;


    /**
     * Costruttore: inizializza la struttura dei comandi disponibili
     * mappando ad ogni commando il numero di parametri minimo e massimo
     * considerando l'intera stringa passata
     */
    public ComTools() {    
        hm = new HashMap<>();
        hm.put("register", "4 8");
        hm.put("login", "3 3");
        hm.put("logout", "1 1");
        hm.put("list users", "2 2");     // param[0]+param[1]
        hm.put("list followers", "2 2"); // param[0]+param[1]
        hm.put("list following", "2 2"); // param[0]+param[1]
        hm.put("follow", "2 2");
        hm.put("unfollow", "2 2");
        hm.put("blog", "1 1");
        hm.put("post", "3 0");
        hm.put("show post", "3 3"); // param[0]+param[1]
        hm.put("show feed", "2 2"); // param[0]+param[1]
        hm.put("delete", "2 2");
        hm.put("rewin", "2 2");
        hm.put("rate", "3 3");
        hm.put("comment", "3 0");
        hm.put("wallet", "1 2"); 
        hm.put("help", "1 1"); 
        
    }
       
    /**
     * Metodo che verifica se il comando è valido, cioè
     * tra quelli disponibli e se rispetta il numero di 
     * parametri consentiti
     * @param line comando intero 
     * @return true se il comando è valido, false altrimenti
     * 
     */
    public static boolean validCom(String line) {
        String[] c = line.toLowerCase().split(" ");
        String k;

        if((k = hm.get(c[0])) == null) { // Se non è un comando single-word
            if(c.length>1) {
                if((k = hm.get( c[0].concat(" "+c[1]) )) == null) // Se non è neanche un comando double-word
                    return false;
            } else return false;
        }
        int min, max;
        min=Integer.parseInt(k.split(" ")[0]);
        max=Integer.parseInt(k.split(" ")[1]);
        return (c.length >= min && (c.length <= max || max == 0));
        
    }
    
    
    /**
     * @param c comando da verificare
     * @return Restituisce true se il comando c necessita prima il login
     */
    public static boolean loggedCom(String c) {
        return !(c.equals("register") || c.equals("login"));
    }


    /**
     * @param logged indica se l'utente è loggato oppure no
     * @return Restituisce la stringa contenente il messaggio di help con i comandi disponibili
     */
    public static String helpCom(Boolean logged) {
        if(!logged)
            return "\n< LISTA COMANDI:\n"+
            "<  login <username> <password>\n"+
            "<  register <username> <password> <tags> (max 5 tags)\n"+
            "<  exit (chiusura applicazione)\n";
        else
            return "< LISTA COMANDI:\n"+
            "<  logout\n"+
            "<  list users                 : Lista degli utenti tag comuni\n"+
            "<  list followers             : Lista degli utenti che ti seguono\n"+
            "<  list following             : Lista degli utenti che segui\n"+
            "<  follow <username>          : Segui un utente\n"+
            "<  unfollow <username>        : Smetti di seguire un utente\n"+
            "<  blog                       : Mostra i tuoi post\n"+
            "<  post <titolo> <content>    : Pubblica un post\n"+
            "<  show feed                  : Mostra il tuo feed\n"+
            "<  show post <postId>         : Mostra un post\n"+
            "<  delete <postId>            : Elimina un tuo post\n"+
            "<  rewin <postId>             : Rewin di un post\n"+
            "<  rate <postId> <vote>       : Vota un post (up: 1, down: -1)\n"+
            "<  comment <postId> <comment> : Commenta un post\n"+
            "<  exit                       : Esci\n";
    }



}
