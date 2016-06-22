package br.inf.ufes.pp2016_01;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlaveImpl implements Slave {

    private static List<String> dictionary = new ArrayList<>();
    private String name;
    private int id;

    private Thread thread;
    
    public SlaveImpl(String name){
        this.name = name;
    }

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

    /**
     * Inicia o sub-ataque
     */
    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) throws RemoteException {
        SlaveAttacker exec = new SlaveAttacker(ciphertext, knowntext, initialwordindex, finalwordindex, callbackinterface);
        Thread thread = new Thread(exec);
        this.thread = thread;
        thread.start();
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

                //TODO
                //Corrigir: só procura no registry se nao achar o mestre
            } catch (RemoteException ex) {
                Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
     /*
         Cria instância da interface do mestre,
         essa operação é necessária para que o escravo consiga se
         registrar na fila gerenciada pelo mestre.
      */
     Master mestre;

     //Para execução distribuida: java SlaveImpl IPESCRAVO IPMESTRE
     //Aqui o escravo recebe sua propria referencia
     String host = null;
     if (args.length > 0) {
         host = args[0];
     }

     //Escravo recebe referencia para o mestre
     //if (args.length > 0) {
     //System.setProperty("java.rmi.server.hostname", args[0]);
     //}

     try {
         //Procura Mestre no Registry
         System.out.println(host);
         Registry registry = LocateRegistry.getRegistry(host);
         mestre = (Master) registry.lookup("ReferenciaMestre");
         SlaveImpl escravo = new SlaveImpl("ceso");

         //cria stub do escravo
         Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 0);

         //De acordo com especificação, escravo deve se registrar no menino mestre
         mestre.addSlave(stub, escravo.getName());

         //"Attach" o metodo que executa operacoes necessarias caso o escravo finalize
         escravo.attachShutDownHook(mestre);
         escravo.registerSlave();

     } catch (RemoteException | NotBoundException e) {
         e.printStackTrace();
     }
 }
}
