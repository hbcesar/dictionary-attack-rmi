package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Client {

    //Realiza letura de arquivo do dicionário
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

        byte[] encrypted = cipher.doFinal(message);

        saveFile(filename + ".cipher", encrypted);

        return encrypted;

    }

    public static void main(String[] args) throws Exception {

        try {

            String hostname = (args.length < 2) ? null : args[2];

            //Primeiro parametro passado é a chave de criptografia
            byte[] key = args[0].getBytes();

            //Segundo parametro passado é o caminho para arquivo com mensagem a ser criptografada
            String filename = args[1];
            byte[] ciphertext = encrypt(key, filename);

            //Palavra conhecida
            byte[] knownword = args[2].getBytes();

            //faz registro do mestre com o nome dado
            Registry registry = LocateRegistry.getRegistry(hostname);
            //objeto remoto que executara os metodos
            final Master stub = (Master) registry.lookup("mestre");

            //tamanho da mensagem (em bytes)
            System.out.print(ciphertext.length);
            System.out.print(";");

            //Executa processamento serial não paralelizado
            // Sequencial s = new Sequencial(ciphertext, knownword); //Classe que realiza processamento sequencial
            Guess g[] = null;

            // long tempoInicialEstatico = System.nanoTime();
            // s.atacar();
            // long tempoFinalEstatico = System.nanoTime();
            // long tempoExecucaoEstatico = (long) ((tempoFinalEstatico - tempoInicialEstatico) / 1000000000.0);
            
            //Executa processamento paralelo
            long tempoInicial = System.nanoTime();
            g = stub.attack(ciphertext, knownword);
            long tempoFinal = System.nanoTime();
            long tempoExecucao = (long) ((tempoFinal - tempoInicial) / 1000000000.0);
            
            System.out.print(tempoExecucao);

            //Imprime guesses encontradas
            GuessPrinter.print(g);

        } catch (Exception ex) {
            System.err.println("Erro encontrado no mestre, saindo..");
            Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}
