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

public class ThreadMestreEscravo extends Thread {

  private byte[] ciphertext;
  private byte[] knowntext;
  private long initialwordindex;
  private long finalwordindex;
  private SlaveManager callbackinterface;
  private Slave slave;

  public ThreadMestreEscravo(Slave slave, byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface){
    this.slave = slave;
    this.ciphertext = ciphertext;
    this.knowntext = knowntext;
    this.initialwordindex = initialwordindex;
    this.finalwordindex = finalwordindex;
    this.callbackinterface = callbackinterface;
  }

    @Override
    public void run() {

        try {
            slave.startSubAttack(ciphertext, knowntext, inicio, fim, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}