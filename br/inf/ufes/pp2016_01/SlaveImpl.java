package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SlaveImpl implements Slave {

    private static List<String> dictionary = new ArrayList<>();
    private int id;

    public SlaveImpl() {

    }

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
            System.out.println("Escravo " + this.getId() + ": arquivo de dicionário não encontrado.");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) throws RemoteException {
        String key;
        byte[] decrypted_message;
        Integer begin = (int) (long) initialwordindex;
        Integer end = (int) (long) finalwordindex;
        
        //chamar thread que faz checkpoint

        for (int i = begin; i <= end; i += 1) {
            key = dictionary.get(i);
            decrypted_message = decrypt(ciphertext, key.getBytes());

            if (checkGuess(decrypted_message, knowntext)) {
                Guess guess = new Guess();
                guess.setKey(key);
                guess.setMessage(decrypted_message);
                callbackinterface.foundGuess(i, guess);
            }
        }
    }

    private byte[] decrypt(byte[] ciphertext, byte[] key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(ciphertext);

            return decrypted;

        } catch (javax.crypto.BadPaddingException e) {
            System.out.println("Senha invalida.");
            return null;

        } catch (Exception e) {
            System.out.println("Escravo " + this.getId() + ": erro descriptografia.");
            return null;
        }
    }

    private boolean checkGuess(byte[] decrypted_message, byte[] knowntext) {
        String d_message = new String(decrypted_message);
        String k_text = new String(knowntext);

        return d_message.toLowerCase().contains(k_text.toLowerCase());
    }
    
    // Desregistra o escravo da lista do Mestre em caso termino.
    public void attachShutDownHook(final Master mestre) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
//                    mestre.removerFilaEscravos(id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
