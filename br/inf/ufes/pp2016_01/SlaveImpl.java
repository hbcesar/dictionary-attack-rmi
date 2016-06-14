package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import static java.util.concurrent.TimeUnit.*;

public class SlaveImpl implements Slave {

  private static List<String> dictionary = new ArrayList<>();
  private String name;
  private int id;

  private SlaveManager myManager;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String setName(String name) {
    this.name = name;
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


  @Override
  public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) throws RemoteException {
    // String key;
    // byte[] decrypted_message;
    // Integer begin = (int) (long) initialwordindex;
    // Integer end = (int) (long) finalwordindex;
    //
    // //chamar thread que faz checkpoint
    //
    // for (int i = begin; i <= end; i += 1) {
    //   key = dictionary.get(i);
    //   decrypted_message = decrypt(ciphertext, key.getBytes());
    //
    //   if (checkGuess(decrypted_message, knowntext)) {
    //     Guess guess = new Guess();
    //     guess.setKey(key);
    //     guess.setMessage(decrypted_message);
    //     callbackinterface.foundGuess(i, guess);
    //   }
    // }

    //TODO
    //inicia a thread passando as informacoes pra ela realizar ataque
    //inicia o checkpointer
  }



  // Desregistra o escravo da lista do Mestre em caso termino.
  public void attachShutDownHook(final Master master) {
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
