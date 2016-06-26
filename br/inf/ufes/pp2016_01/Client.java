package br.inf.ufes.pp2016_01;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Client {

    private static byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        InputStream is = new FileInputStream(file);
        long length = file.length();
        // creates array (assumes file length<Integer.MAX_VALUE)
        byte[] data = new byte[(int) length];
        int offset = 0;
        int count = 0;
        while ((offset < data.length)
                && (count = is.read(data, offset, data.length - offset)) >= 0) {
            offset += count;
        }
        is.close();
        return data;
    }

    private static void saveFile(String filename, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();
    }

    private static byte[] encrypt(byte[] key, String filename) throws Exception {

        byte[] message = readFile(filename);

        SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");

        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        System.out.println("message size (bytes) = " + message.length);

        byte[] encrypted = cipher.doFinal(message);

        saveFile(filename + ".cipher", encrypted);

        return encrypted;

    }

    public static void main(String[] args) {

        try {

            String hostname = (args.length < 3) ? null : args[2];
            byte[] key = args[0].getBytes();

            String name = "mestre";
            byte[] knownword = "personnes".getBytes();
            byte[] ciphertext = encrypt(key, args[1]);

            try {
                //faz registro do mestre com o nome dado
                Registry registry = LocateRegistry.getRegistry(hostname);

                //objeto remoto que executara os metodos
                final Master stub = (Master) registry.lookup("mestre");

                //Imprime o header do CSV
                System.out.println("Tamanho do Arquivo;Tempo de Execução Estático;Tempo de Execução Distribuido");
                System.out.print(ciphertext.length);
                System.out.print(";");
                //Executa Calculo Serial Não paralelizado
                Sequencial s = new Sequencial(ciphertext, knownword);
                long tempoInicialEstatico = System.nanoTime();
                s.atacar();
                long tempoFinalEstatico = System.nanoTime();
                long tempoExecucaoEstatico = (long) ((tempoFinalEstatico - tempoInicialEstatico) / 1000000000.0);
                System.out.print(tempoExecucaoEstatico);
                System.out.print(";");
                //Executa calculo Paralelo
                long tempoInicial = System.nanoTime();
                Guess g[] = stub.attack(ciphertext, knownword);
                long tempoFinal = System.nanoTime();
                long tempoExecucao = (long) ((tempoFinal - tempoInicial) / 1000000000.0);
                System.out.println(tempoExecucao);

                GuessPrinter.print(g);

            } catch (RemoteException | NotBoundException e) {
                System.err.println("Erro no mestre, saindo..");
            }
        } catch (Exception ex) {
            System.err.println("Erro encontrado no mestre, saindo..");
        }
    }
}
