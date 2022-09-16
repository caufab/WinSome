
import java.time.LocalDateTime;
/**
 * Classe Transaction definisce il tipo di dato relativo alla transazione
 * forato dunque da due parametri: valore e timestamp della transazione
 * @author Fabrizio Cau
 */
public class Transaction {

    public Double value;
    public LocalDateTime timestamp;


    /**
     * Costruttore: restituisce un oggetto Transaction con i seguenti parametri:
     * @param v valore della transazione
     * @param t timestamp della transazione
     */
    public Transaction(Double v, LocalDateTime t) {
        this.value = v;
        this.timestamp = t;
    }


    /**
     * @return Restituisce il timestamp della transazione
     */
    public LocalDateTime getDate() {
        return this.timestamp;
    }

    /**
     * @return Restituisce il valore della transazione
     */
    public Double getValue() {
        return this.value;
    }

}
