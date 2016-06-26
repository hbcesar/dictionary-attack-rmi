package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

// public class SlaveAttacker extends Thread {
public class SlaveAttacker {

    private static List<String> dictionary = new ArrayList<>();
    private long currentIndex;

    private byte[] ciphertext;
    private byte[] knowntext;
    private long initialwordindex;
    private long finalwordindex;
    private SlaveManager callbackinterface;

    public long getInitialwordindex() {
        return initialwordindex;
    }

    public void setInitialwordindex(long initialwordindex) {
        this.initialwordindex = initialwordindex;
    }

    public long getFinalwordindex() {
        return finalwordindex;
    }

    public void setFinalwordindex(long finalwordindex) {
        this.finalwordindex = finalwordindex;
    }

    public SlaveAttacker(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) {
        this.ciphertext = ciphertext;
        this.knowntext = knowntext;
        this.initialwordindex = initialwordindex;
        this.finalwordindex = finalwordindex;
        this.callbackinterface = callbackinterface;
    }

    /**
     * **** Getters and Setters *******
     *
     */
    public long getCurrentIndex() {
        return currentIndex;
    }

    /**
     * *** Realiza Leitura do arquivo de dicionario *****
     */
    private void readDictionary() {
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("dictionary.txt"));

            //palavra lida
            String word;

            while ((word = br.readLine()) != null) {
                dictionary.add(word);
            }

            br.close();
        } catch (IOException ex) {
            System.out.println("Escravo: arquivo de dicionário não encontrado.");
        }
    }

    /**
     * Tenta realizar descriptografia dados texto criptografado e chave
     */
    private byte[] decrypt(byte[] key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(this.ciphertext);

            return decrypted;

        } catch (javax.crypto.BadPaddingException e) {
            // System.out.println("Senha invalida.");
            return null;

        } catch (Exception e) {
            System.out.println("Escravo: erro na descriptografia.");
            return null;
        }
    }

    /**
     * Checa se resultado da criptografia pode ser considerado válido ou não
     */
    private boolean checkGuess(byte[] decrypted_message) {
        String d_message = new String(decrypted_message);
        String k_text = new String(this.knowntext);

        return d_message.toLowerCase().contains(k_text.toLowerCase());
    }

    /**
     * Inicia o sub-ataque
     */
    public void startSubAttack() throws RemoteException {
        String key;
        byte[] decrypted_message;
        long begin = initialwordindex;
        long end = finalwordindex;
        readDictionary();

        if (dictionary.size() == 0) {
            System.out.println("Erro leitura dicionario!");
        }

        /**
         * Código que realiza checkpoint Baseado em:
         * http://www.tutorialspoint.com/java/util/timer_scheduleatfixedrate_delay.htm
         */
        Timer scheduler = new Timer();
        TimerTask checkpointer = new Checkpointer(this.callbackinterface, this);
        scheduler.scheduleAtFixedRate(checkpointer, 0, 10000);
        System.out.println(begin);

        for (currentIndex = begin; currentIndex < end; currentIndex += 1) {
            key = dictionary.get((int) currentIndex);  //Arrayoutofbounds
            decrypted_message = decrypt(key.getBytes());

            if (decrypted_message != null && checkGuess(decrypted_message)) {
                Guess guess = new Guess();
                guess.setKey(key);
                guess.setMessage(decrypted_message);
                callbackinterface.foundGuess(currentIndex, guess);
                System.out.println("Achei: " + key);
            }
        }

        System.out.println("Done");
        checkpointer.cancel();
        scheduler.cancel();
    }

    // @Override
    // public void run() {
    //   try {
    //     startSubAttack();
    //   } catch (RemoteException e) {
    //     e.printStackTrace();
    //   }
    // }
    //Inner class que realiza checkpoint a cada 20s
    private class Checkpointer extends TimerTask {

        private SlaveManager callbackinterface;
        private SlaveAttacker slaveAttacker;

        public Checkpointer(SlaveManager c, SlaveAttacker slaveAttacker) {
            this.callbackinterface = c;
            this.slaveAttacker = slaveAttacker;
        }

        @Override
        public void run() {
            try {
                callbackinterface.checkpoint((long) this.slaveAttacker.getCurrentIndex());
            } catch (RemoteException ex) {
                System.out.println("Escravo teve problemas no checkpointer");
//                Logger.getLogger(SlaveAttacker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
