package br.inf.ufes.pp2016_1;

import java.io.*;
import java.util.*;

public class SequencialTester {

	//dicionario
	List<String> dictionary = new ArrayList<String>();

//	public static Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		// TODO
//	}

	//mudar tipo de retorno(?)
	public static String readFile(String name) throws IOException {
		// TODO
		String content = null;
	    File file = new File(name);
	    FileReader reader = null;
	    try {
	        reader = new FileReader(file);
	        char[] chars = new char[(int) file.length()];
	        reader.read(chars);
	        content = new String(chars);
	        reader.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if(reader != null) {
	        	reader.close();
	        }
	    }

	    return content;
	}
}