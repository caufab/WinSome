

/**
 * Classe che definisce il tipo Comment: un Comment possiede 
 * un Autore e un testo
 * @author Fabrizio Cau
 * 
 */
public class Comment {
   
    // Username dell'utente autore del commento
    private String autorId;
    // Testo del commento
    private String text;

    
    /**
     * Costruttore per la classe Comment: costruisce il commento mediante
     * i due parametri passati
     * @param autorId username dell'autore
     * @param text testo del commento
     */
    public Comment(String autorId, String text) {
            this.autorId = autorId;
            this.text = text;
        }

    /**
     * Restituisce il commento formattato come "Autore: commento"
     * @return
     */
    public String getComment() {
        return autorId+": "+text;
    }

    
    
    /**
     * Restituisce l'autore del commento
     * @return username dell'autore
     */
    public String getAutor() {
        return this.autorId;
    }


    /**
     * Restituisce una deepcopy del commento (nuovo oggetto)
     * @return
     */
    public Comment gerDeepCopy() {
        return new Comment(autorId, text);
    }
}
