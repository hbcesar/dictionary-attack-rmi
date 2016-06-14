class AttackThread extends Thread {
  private long checkpoint;
  // private long beginIndex;
  // private long endIndex;

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
}
