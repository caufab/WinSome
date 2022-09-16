

/**
 * Classe Backuper implementa il task che ogni BackuperSleepTime e ad ogni interruzione 
 * (generata nel main alla chiusura) chiama le funzioni per la scrittura dei post e utenti
 * sui file Json
 * 
 * @author Fabrizio Cau 
 * 
 */
public class Backuper implements Runnable {

    public Backuper() {
    }

    public void run() {
        while(true) {
            try { Thread.sleep(Config.BackuperSleepTime); } 
            catch (InterruptedException e) { 
                if(Config.BackupBeforeClosing) {
                    db.writeUsersToJson(); 
                    db.writePostsToJson();
                    break;
                } 
            }
            db.writeUsersToJson();
            db.writePostsToJson();
                
            Log.printInfo("Backup database su Json effettuato");
        }
    }
    


}
