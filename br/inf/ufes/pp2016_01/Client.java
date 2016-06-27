package br.inf.ufes.pp2016_01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

        // System.out.println(message.length);

        byte[] encrypted = cipher.doFinal(message);

        saveFile(filename + ".cipher", encrypted);

        return encrypted;

    }

    private static List<String> readMessage(String filename) {
        List<String> msg = new ArrayList<>();

        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader(filename));

            //palavra lida
            String word;

            while ((word = br.readLine()) != null) {
                msg.add(word);
            }

            br.close();
        } catch (IOException ex) {
            System.out.println("Escravo: arquivo de mensagem não encontrado.");
        }

        return msg;
    }

    public static void createNewMsgFile(String filename, List<String> msg, int end) {
        try {
            String file = filename;
            file = file.replace(".txt", "");
            file = "testsMSG/" + file + "_" + end + ".txt";
            String[] mensagem = new String[msg.size()];
            mensagem = msg.toArray(mensagem);
            mensagem = Arrays.copyOfRange(mensagem, 0, end);

            FileWriter fw = new FileWriter(file);

            for (int i = 0; i < mensagem.length; i++) {
                fw.write(mensagem[i]);
            }

            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        try {

            String hostname = (args.length < 2) ? null : args[2];

            //Primeiro parametro passado é a chave de criptografia
            byte[] key = args[0].getBytes();

            //Segundo parametro passado é o caminho para arquivo com mensagem a ser criptografada
//            byte[] ciphertext = encrypt(key, args[1]);
            String filename = args[1];

            //Palavra conhecida
//            byte[] knownword = args[2].getBytes();
            //Mensagem conhecida
            List<String> msg = readMessage(filename);

            try {
                //faz registro do mestre com o nome dado
                Registry registry = LocateRegistry.getRegistry(hostname);
                //objeto remoto que executara os metodos
                final Master stub = (Master) registry.lookup("mestre");
                if (msg.size() < 1000) {
                    System.out.println("Mensagem menor que 10k");
                } else {
                    //Imprime resultados em formato CSV
                    System.out.println("Tamanho do Arquivo;Tempo de Execução Estático;Tempo de Execução Distribuido");

                    long[] timeSequencial = new long[2];;
                    // long[] timeParalelo = new long[2];

                    for (int i = 20000; i <= 50000; i += 10000) {
                        createNewMsgFile(filename, msg, i);

                        String sfilename = filename.replace(".txt", "");
                        sfilename = "testsMSG/" + sfilename + "_" + i + ".txt";
                        byte[] ciphertext = encrypt(key, sfilename);
                        //tamanho da mensagem (em bytes)
                        System.out.print(ciphertext.length);
                        System.out.print(";");

                        //Executa processamento serial não paralelizado
                        Sequencial s = new Sequencial(ciphertext, "jesus".getBytes()); //Classe que realiza processamento sequencial
                        Guess g[] = null;

                        for(int j = 0; j < 2; j++){
                            long tempoInicialEstatico = System.nanoTime();
                            s.atacar();
                            long tempoFinalEstatico = System.nanoTime();
                            long tempoExecucaoEstatico = (long) ((tempoFinalEstatico - tempoInicialEstatico) / 1000000000.0);
                            timeSequencial[j] = tempoExecucaoEstatico;

                            //Executa processamento paralelo
                            // long tempoInicial = System.nanoTime();
                            // g = stub.attack(ciphertext, "jesus".getBytes());
                            // long tempoFinal = System.nanoTime();
                            // long tempoExecucao = (long) ((tempoFinal - tempoInicial) / 1000000000.0);
                            // timeParalelo[j] = tempoExecucao;
                        }

                        long mediaSequencial = 0;
                        // long mediaParalelo = 0;
                        for(int j = 0; j < 2; j++){
                            mediaSequencial += timeSequencial[j];
                            // mediaParalelo += timeParalelo[j];
                        }

                        System.out.print(mediaSequencial/5.0);
                        //System.out.print(";");
                        // System.out.println(mediaParalelo/2.0);

                        //Imprime guesses encontradas
                        // GuessPrinter.print(g);
                    }
                }

            } catch (RemoteException | NotBoundException e) {
                System.err.println("Erro no mestre, saindo..");
            }
        } catch (Exception ex) {
            System.err.println("Erro encontrado no mestre, saindo..");
            Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}
