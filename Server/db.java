
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


    

/**
 * Classe db definisce la struttura dati che contiene post e utenti
 * e i metodi per leggere e scrivere le strutture dati da file Json
 * @author Fabrizio Cau
 * 
 */
public class db {
   
    public static Map<String, User> UsersList; 
    public static Map<Integer, Post> PostsList; 

    // Ultimo id da cui partire quando si crea un post
    public static volatile Integer LastPostId;

    public static ServerFollowersUpdImpl RmiCallbackService = null;

    /**
     * Inizializza le strutture dati di post e utenti e recupera i dati dai file
     * 
     */
    public db() {

        UsersList = new ConcurrentHashMap<>();
        PostsList = new ConcurrentHashMap<>();
        readUsersFromJson();
        readPostFromJson();
        LastPostId = 0;
        
        Log.printInfo("Il server ha recuperato "+UsersList.size()+" Utenti e "+PostsList.size()+" Post");
    }

    
    /**
     * Lettura dati della classe User dal file Json (Deserializzazione)
     * utilizza la libreria Gson
     */
    public void readUsersFromJson() {
        try {
            FileInputStream is = new FileInputStream(Config.usersFile);
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            Type userType = new TypeToken<User>(){}.getType();
            reader.beginArray();
            User usr;
            while(reader.hasNext()) {
                usr = new Gson().fromJson(reader, userType);
                if(UsersList.put(usr.getUser(),usr) != null)
                    // stampa errore nel log del  server
                    Log.printErr("Errore nel recuperare un utente dal file JSON");
            }  
            if(is!=null)
                is.close();
        } 
        catch(EOFException e) {
            Log.printInfo("File Json degli utenti vuoto",e);
        }
        catch (Exception e) { 
            Log.printErr("Errore lettura file JSON Users ",e); 
        }
        
    }

    /**
     * Lettura dati della classe Post dal file Json (Deserializzazione)
     * utilizza la libreria Gson
     */
    public static void readPostFromJson() {
        try {
            FileInputStream is = new FileInputStream(Config.postsFile);
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            Type userType = new TypeToken<Post>(){}.getType();
            reader.beginArray();
            Post pst;
            while(reader.hasNext()) {
                pst = new Gson().fromJson(reader, userType);
                if(PostsList.putIfAbsent(pst.getId(),pst) != null)
                    Log.printErr("Errore nel recuperare un post dal file JSON");
            } 
            if(is!=null)
                is.close(); 
        } 
        catch(EOFException e) {
            Log.printInfo("File Json dei post vuoto",e);
        }
        catch (Exception e) { 
            Log.printErr("Errore lettura file JSON Post ",e); 
        }
    }

    
    /**
     * Scrittura dei dati della classe User nel file Json (Serializzazione)
     * utilizza la libreria Gson
     */
    public static void writeUsersToJson() { 
        Gson gsonWrite = new GsonBuilder().setPrettyPrinting().create();
        boolean firstElement = true;
        
        try (
            FileOutputStream os = new FileOutputStream(Config.usersFile); 
            FileChannel oc = os.getChannel();
        ) { 
            ByteBuffer buffer = ByteBuffer.allocate(Config.bufSize); // JAVA NIO??
            for(Map.Entry<String, User> e : db.UsersList.entrySet()) {
                byte[] data = gsonWrite.toJson(e.getValue()).getBytes();
                buffer.clear();
                if(!firstElement) buffer.put(",".getBytes()); // dopo il primo elemento mette la , prima dell'oggetto
                else buffer.put("[".getBytes()); // Mette '['' per indicare l'inizio dell'array di oggetti su JSON
                firstElement = false;
                buffer.put(data);
                buffer.flip();
                oc.write(buffer);
            }     
            // Mette la ']' alla fine del file per indicare la fine della lista di oggetti JSON
            buffer.clear();
            buffer.put("]".getBytes());
            buffer.flip();
            oc.write(buffer);
            
            if(os!=null)
                os.close(); 

        }
        catch (Exception e) { Log.printErr("Errore: backup dei post su file JSON non possibile ",e); } 
        
        }

    /**
     * Scrittura dei dati della classe Post nel file Json (Serializzazione)
     * utilizza la libreria Gson
     */
    public static void writePostsToJson() {
        Gson gsonWrite = new GsonBuilder().setPrettyPrinting().create();
        boolean firstElement = true;
        try (
            FileOutputStream os = new FileOutputStream(Config.postsFile); 
            FileChannel oc = os.getChannel();
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(Config.bufSize); 
            
            for(Map.Entry<Integer, Post> e : db.PostsList.entrySet()) {
                byte[] data = gsonWrite.toJson(e.getValue()).getBytes();
                buffer.clear();
                if(!firstElement) buffer.put(",".getBytes()); // dopo il primo elemento mette la , prima dell'oggetto
                else buffer.put("[".getBytes()); // Mette '['' per indicare l'inizio dell'array di oggetti su JSON
                firstElement = false;
                buffer.put(data);
                buffer.flip();
                oc.write(buffer);
            }
            // Mette la ']' alla fine del file per indicare la fine della lista di oggetti JSON
            buffer.clear();
            buffer.put("]".getBytes());
            buffer.flip();
            oc.write(buffer);

            if(os!=null)
                os.close(); 
        }
        catch (Exception e) { Log.printErr("Errore: backup dei post su file JSON non possibile",e); } 
        
    }

}
