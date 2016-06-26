/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author hbcesar
 */
public class Sequencial {

    private List<String> dictionary = new ArrayList<>();
    private long currentIndex;

    private byte[] ciphertext;
    private byte[] knowntext;
    private Guess[] guesses;
    private List<Guess> guessList = new ArrayList<>();

    public Sequencial(byte[] ciphertext, byte[] knowntext) {
        this.ciphertext = ciphertext;
        this.knowntext = knowntext;
    }

    private byte[] decrypt(byte[] key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(this.ciphertext);

            return decrypted;

        } catch (javax.crypto.BadPaddingException e) {
//            System.out.println("Senha invalida.");
            return null;

        } catch (Exception e) {
            System.out.println("Sequencial: erro na descriptografia.");
            return null;
        }
    }

    private boolean checkGuess(byte[] decrypted_message) {
        String d_message = new String(decrypted_message);
        String k_text = new String(this.knowntext);

        return d_message.toLowerCase().contains(k_text.toLowerCase());
    }

    public void startSubAttack() {
        String key;
        byte[] decrypted_message;
        long begin = 0;
        long end = dictionary.size();

        for (currentIndex = begin; currentIndex < end; currentIndex += 1) {
            key = dictionary.get((int) currentIndex);
            decrypted_message = decrypt(key.getBytes());

            if (decrypted_message != null && checkGuess(decrypted_message)) {
                Guess guess = new Guess();
                guess.setKey(key);
                guess.setMessage(decrypted_message);
                guessList.add(guess);
            }
        }
        
        guesses = new Guess[guessList.size()];
        guesses = guessList.toArray(guesses);
    }

    public void readDictionary() {

        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("dictionary.txt"));

            //palavra lida
            String word;

            while ((word = br.readLine()) != null) {
                this.dictionary.add(word);
            }

            br.close();

        } catch (IOException ex) {
            System.out.println("Mestre: arquivo de dicionário não encontrado.");
        }

    }

    public void atacar() {
        readDictionary();
        startSubAttack();
        GuessPrinter.print(guesses);
    }

}
