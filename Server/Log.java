
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 *  Classe Log implementa dei metodi per salvare il log dell'attività del server
 *	@author Fabrizio Cau
 * 
 */  
public class Log {

    // Definizione codici ASCII per colorare la console
    private static String RESET = "\u001B[0m";
    private static String RED = "\u001B[31m";
    private static String BLUE = "\u001B[34m";

    // Stringa che mentiene il log da scrivere sul file
    private static String log = "";

    /**
     * Costruttore classe Log: a seconda del parametro di configurazione ColoredConsole
     * lascia invariato o azzera i codici ascii per colorare la console
     * 
     */
    public Log() {
        if(!Config.ColoredConsole) {
            RESET = "";
            RED = "";
            BLUE = "";
        }
    }
    
    /**
     * Messaggio di Errore: 
     * aggiunge al log e stampa in rosso il messaggio 
     * @param s stringa contenente il messaggio
     * 
     */
    public static void printErr(String s) {
        log += now()+s+"\n";
        System.out.println(RED+s+RESET);
    }

    /**
     * Messaggio di Errore: 
     * aggiunge al log e stampa in rosso il messaggio insieme al messaggio
     * relativo all'eccezione lanciata
     * @param s stringa contenente il messaggio
     * @param e eccezione lanciata
     */
    public static void printErr(String s, Exception e) {
        log += now()+s+e.getMessage()+"\n";
        if(Config.DebugMode)
            printErr(s+": "+e.getMessage());
        else 
            printErr(s);
    }

    /**
     * Messaggio di Informazione: 
     * aggiunge al log e stampa in blu il messaggio 
     * @param s stringa contenente il messaggio
     * 
     */
    public static void printInfo(String s) {
        log += now()+s+"\n";
        System.out.println(BLUE+s+RESET);
    }

    /**
     * Messaggio di Informazione: 
     * aggiunge al log e stampa in blu il messaggio insieme al messaggio
     * relativo all'eccezione lanciata
     * @param s stringa contenente il messaggio
     * @param e eccezione lanciata
     */
    public static void printInfo(String s, Exception e) {
        log += now()+s+": "+e.getMessage()+"\n";
        if(Config.DebugMode)
            printInfo(s+": "+e.getMessage());
        else 
            printInfo(s);
    }

    /**
     * Messaggio di comunicazione con il client: 
     * aggiunge al log e stampa il messaggio 
     * @param s stringa contenente il messaggio
     * 
     */
    public static void printCom(String s) {
        log += now()+s+"\n";
        if(Config.PrintCom) 
            System.out.println(s);
    }

    /**
     * Aggiunge al file di log il log costruito durante l'esecuzione 
     * (Scrive in modalità APPEND) 
     * 
     */
    public static void logToFile() {
        try { Files.write(Paths.get(Config.logFile), log.getBytes(), StandardOpenOption.APPEND); } 
        catch (IOException e) {
            printErr("Errore: impossibile salvare il file log", e);
        }
    }


    /**
     * @return Una stringa con la formattazione dell'ora attuale per i messaggi di log
     * 
     */
    private static String now() {
        LocalDateTime t = LocalDateTime.now();
        return String.format("[%tY-%tC-%td %tH:%tM:%tS]:", t, t, t, t, t, t);

    }

}
