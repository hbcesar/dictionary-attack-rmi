package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MasterImpl implements Master {
    Map<Integer, SlaveImpl> slaveMap = new HashMap<Integer, SlaveImpl>();
    Map<Integer, String> slaveListIds = new HashMap<Integer, String>();
    List<Guess> guessList = new ArrayList<Guess>();

    private int nSlaves = 0;
    private long dictionaryLength;

    //metodo de Attacker
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
//        TODO
        Guess[] guessArray = null;

        return guessArray;
    }

    public Map<Integer, SlaveImpl> getSlaveMap() {
        return slaveMap;
    }

    public Map<Integer, String> getSlaveListIds() {
        return slaveListIds;
    }

    //metodos de SlaveManager
    public synchronized int addSlave(Slave s, String slavename) throws RemoteException {
    
        if(!slaveListIds.containsValue(slavename)) {
            slaveListIds.put(nSlaves, slavename);
            slaveMap.put(nSlaves, (SlaveImpl)s);

            System.out.println("Escravo adicionado.");
        }
        
        return nSlaves++;
}

    public synchronized void removeSlave(int slaveKey) throws RemoteException {
        if(slaveMap.containsKey(slaveKey)) {
            slaveListIds.remove(slaveKey);
            slaveMap.remove(slaveKey);
                
            System.out.println("Escravo "+slaveKey+ " removido.");
         } 

        //Registrar novamente a cada 30s
         
        
    }

    public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
        System.out.println("Nova palavra encontrada!");
        guessList.add(currentguess);
    }

    public void checkpoint(long currentindex) throws RemoteException {

    }

    public static void main(String[] args) throws Exception {

        try{
            //igual trab1
            String masterName = (args.length < 1) ? "mestre" : args[0];

            if (args.length > 0) {
                System.setProperty("java.rmi.server.hostname", args[0]);
            }

            Master master = new MasterImpl();
            Master stubRef = (Master) UnicastRemoteObject.exportObject(master,2001);

            final Registry registry = LocateRegistry.getRegistry();
            registry.rebind(masterName, stubRef);

            System.out.println("Mestre está pronto...");
        
        }catch(Exception e){
          System.err.println("Mestre gerou exceção.");  
          e.printStackTrace();
        }
/*
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            //
            }
        } */
    }
}

