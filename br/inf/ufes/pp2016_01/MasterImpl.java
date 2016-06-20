package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MasterImpl implements Master {
    Map<Integer, SlaveImpl> slaveMap = new HashMap<Integer, SlaveImpl>();
    Map<Integer, String> slaveListIds = new HashMap<Integer, String>();
   
    private static List<String> dictionary = new ArrayList<>();
    private Map<Integer, SlaveData> slaves = new HashMap<>();
    private Map<Integer, SlaveData> falhas = new HashMap<>();
    List<Guess> guessList = new ArrayList<Guess>();

    private int nSlaves = 0;
    private long dictionaryLength;

    //metodo de Attacker
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
//        TODO
        Guess[] guessArray = null;
        
        long tamVetor = dictionary.size();
        long tamVetorEscravos = tamVetor / slaves.size();
        long resto = tamVetor % slaves.size();
        long range = 0;
        long inicio = 0;
        long fim = tamVetorEscravos;
        
        for (Map.Entry<Integer, SlaveData> e : slaves.entrySet()) {
            //se a divisao de vetores nao for exata, vai atribuindo +1 campo do vetor a cada escravo
            if (resto > 0) {
                fim++;
                resto--;
            }
            
            SlaveData slaveData = e.getValue();
            Slave slave = slaveData.getSlaveReference();
            
            slaveData.setBeginIndex(inicio);
            slaveData.setEndIndex(fim);
            slaveData.setTime(System.nanoTime());
            
            slave.startSubAttack(ciphertext, knowntext, inicio, fim, this);
            
            inicio = fim;
            fim += tamVetorEscravos;
            
            //Cria as threads para executar os meninos escravos (mover isso pro lado do escravo)
//            ThreadMestreEscravo exec = new ThreadMestreEscravo(e.getValue(), subVetor);
//            meninxs.add(exec);
//            Thread t = new Thread(exec);
//            threads.add(t);
//
//            t.start();
        }


        return guessArray;
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
            Slave failed = this.slaves.get(slaveKey);
            
            SlaveData remaining = new SlaveData();
            remaining.setBeginIndex(failed.getLastCheckedIndex());
            remaining.setEndIndex(failed.getEndIndex());
            
            falhas.put(nSlaves++, remaining);
            
            slaveListIds.remove(slaveKey);
            slaveMap.remove(slaveKey);
                
            System.out.println("Escravo " + slaveKey + " removido.");
         } 
    }

    public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
        System.out.println("Nova palavra encontrada!");
        guessList.add(currentguess);
    }

    public void checkpoint(long currentindex) throws RemoteException {

    }
    
    // Captura o CTRL+C
    //http://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                // Remove todos escravos da lista caso o mestre caia
                for (Map.Entry<Integer, InterfaceEscravo> escravo : listaEscravos
                        .entrySet()) {
                    listaEscravos.remove(escravo);
                }
                System.out.println(" Mestre Caiu! :(");
            }
        });
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

