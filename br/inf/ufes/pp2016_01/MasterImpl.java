package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class MasterImpl implements Master {
    private Map<String, SlaveImpl> slaveMap = new HashMap<String, SlaveImpl>();
    private Map<Integer, String> slaveIds = new HashMap<Integer, String>();

    private int nSlaves = 0;
    private long dictionaryLength;

    //metodo de Attacker
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
//        TODO
    }

    //metodos de SlaveManager
    public synchronized int addSlave(Slave s, String slavename) throws RemoteException {
        if(!slaveMap.containsKey(slavename)) {
            slaveMap.put(slavename, (SlaveImpl)s);
            slaveIds.put(nSlaves, slavename);
            // TODO
            System.out.println("Escravo " +(nSlaves+1)+ "adicionado.");
        }

        return nSlaves++;
    }

    public synchronized void removeSlave(int slaveKey) throws RemoteException {
        if(slaveMap.containsKey(slaveKey))
            slaveIds.remove(slaveKey);
//          TODO
    System.out.println("Escravo " +slaveKey+ "removido.");
    }

    public void foundGuess(long currentindex, Guess currentguess)throws RemoteException {

    }

    public void checkpoint(long currentindex) throws RemoteException {

    }
}
