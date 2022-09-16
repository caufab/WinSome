/**
 *
 *  @file Hash.java
 *	@author Matteo Loporchio
 *  
 *	Questo file contiene l'implementazione di una semplice
 *	funzione di hashing basata sull'algoritmo SHA-256.
 *
 *	Fonte: https://www.mkyong.com/java/java-sha-hashing-example/
 */
 

import java.security.*;


public class Hash {

	public static String toHash(String s) {
		return bytesToHex(sha256(s));
	}


	/**
	 *	Metodo che calcola il valore hash SHA-256 di una stringa
	 * 	@param s la stringa di input
	 *  @return i byte corrispondenti al valore hash dell'input
	 */
	public static byte[] sha256(String s) {
		MessageDigest md = null;
		try { md = MessageDigest.getInstance("SHA-256"); } 
        catch (NoSuchAlgorithmException e) { 
			Log.printErr("Errore: impossibile generare l'hash della password",e); 
			return null;
		}
        md.update(s.getBytes());
		return md.digest();
	}

	/**
	 *	Metodo per convertire un array di byte in una stringa esadecimale
	 *	@param hash un array di byte
	 *	@return una stringa esadecimale leggibile
	 */
	public static String bytesToHex(byte[] hash) {
		if(hash==null) return null;
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
