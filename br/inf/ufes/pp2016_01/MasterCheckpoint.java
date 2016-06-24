/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hbcesar
 */
public class MasterCheckpoint extends Thread {

    private MasterImpl master;
    private Map<Integer, SlaveData> slaves;

    public MasterCheckpoint(MasterImpl master, Map<Integer, SlaveData> slaves) {
        this.master = master;
        this.slaves = slaves;
    }

    @Override
    public void run() {
        while (!master.isDone()) {
            try {
                Thread.sleep(1000);
                for (Map.Entry<Integer, SlaveData> e : slaves.entrySet()) {
                    SlaveData s = e.getValue();
                    long currentTime = System.nanoTime();
                    long lastCheckedTime = (long) s.getTime();
                    long TimeBetweenCheckpoints = currentTime - lastCheckedTime;
                    if (TimeBetweenCheckpoints > 20.0) {
                        master.removeSlave((int) s.getId());
                    }
                }
            } catch (RemoteException | InterruptedException ex) {
                Logger.getLogger(MasterCheckpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
