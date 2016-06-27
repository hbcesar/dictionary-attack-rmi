package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;

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
      slave.startSubAttack(ciphertext, knowntext, initialwordindex, finalwordindex, callbackinterface);
    } catch (RemoteException e) {
      System.out.println("Terminando thread pois escravo finalizou.");
      e.printStackTrace();
    }
  }
}