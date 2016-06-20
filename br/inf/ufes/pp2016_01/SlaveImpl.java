package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlaveImpl implements Slave {

    private static List<String> dictionary = new ArrayList<>();
    private String name;
    private int id;
    private long currentIndex;

    private SlaveManager myManager;

    /**
     * **** Getters and Setters *******
     * 
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
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
            System.out.println("Escravo " + this.getId() + ": arquivo de dicionário não encontrado.");
        }
    }

    /**
     * Tenta realizar descriptografia dados texto criptografado e chave
     */
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
            System.out.println("Escravo " + this.getId() + ": erro na descriptografia.");
            return null;
        }
    }

    /**
     * Checa se resultado da criptografia pode ser considerado válido ou não
     */
    private boolean checkGuess(byte[] decrypted_message, byte[] knowntext) {
        String d_message = new String(decrypted_message);
        String k_text = new String(knowntext);

        return d_message.toLowerCase().contains(k_text.toLowerCase());
    }

    /**
     * Inicia o sub-ataque
     */
    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) throws RemoteException {
        String key;
        byte[] decrypted_message;
        long begin = (long) initialwordindex;
        long end = (long) finalwordindex;

        /**
         * Código que realiza checkpoint 
         * Baseado em: http://www.tutorialspoint.com/java/util/timer_scheduleatfixedrate_delay.htm
         */
        Timer scheduler = new Timer();
        TimerTask checkpointer = new Checkpointer();
        scheduler.scheduleAtFixedRate(checkpointer, 20000, 20000);

        for (currentIndex = begin; currentIndex <= end; currentIndex += 1) {
            key = dictionary.get((int)currentIndex);
            decrypted_message = decrypt(ciphertext, key.getBytes());

            if (checkGuess(decrypted_message, knowntext)) {
                Guess guess = new Guess();
                guess.setKey(key);
                guess.setMessage(decrypted_message);
                callbackinterface.foundGuess(currentIndex, guess);
            }
        }
    }

    // Desregistra o escravo da lista do Mestre em caso de termino.
    public void attachShutDownHook(final Master master) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    master.removeSlave((int) getId());
                } catch (RemoteException ex) {
                    Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    //Procura pelo mestre 
    private Master searchMaster(String masterName){
        try {
            Registry registry = LocateRegistry.getRegistry(masterName);
            Master mestre = (Master) registry.lookup("ReferenciaMestre");
            return mestre;
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    //Registre o escravo no mestre a cada 30s
    private void registerSlave(){
        Timer scheduler = new Timer();
        TimerTask masterRegister = new MasterRegister(this);
        scheduler.scheduleAtFixedRate(masterRegister, 30000, 30000);
    }
    
    
    //Inner class que realiza checkpoint a cada 20s
    private class Checkpointer extends TimerTask {
        @Override
        public void run() {
            try {
                myManager.checkpoint((long) SlaveImpl.this.getCurrentIndex());
            } catch (RemoteException ex) {
                Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //Inner class que realiza o registro do mestre a cada 30s
    private class MasterRegister extends TimerTask {
        private final SlaveImpl s;
        
        public MasterRegister(SlaveImpl s){
            this.s = s;
        }

        @Override
        public void run() {
            try {
                Master m = SlaveImpl.this.searchMaster("master");
                final Slave stub = (Slave) UnicastRemoteObject.exportObject(this.s, 0);
                this.s.setId(m.addSlave(stub, name));

            } catch (RemoteException ex) {
                Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
