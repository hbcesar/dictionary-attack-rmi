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

/*
Receber um argumento na linha de comandos que indica o nome do arquivo que contém o vetor de bytes (com a mensagem criptografada) e outro argumento que
indica a palavra conhecida que consta da mensagem. Caso o arquivo não exista, o cliente deve gerar o vetor de bytes aleatoriamente e salvá-lo em arquivo. Em um
terceiro parâmetro pode ser especificado com o tamanho do vetor a ser gerado. Se esse terceiro parâmetro não existe, o tamanho do vetor deve ser gerado
aleatoriamente (na faixa 1000 a 100000).
 */
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

//		List<String> dictionary = readDictionary();
//recebe nome que foi associado ao mestre (para buscar no Registry)
/*String hostname = null;
if (args.length > 0) {
hostname = args[0];
}
             */
            String hostname = (args.length < 3) ? null : args[2];
// String filename = args[0];
            byte[] key = args[0].getBytes();

            String name = "mestre";
            byte[] knownword = "jolie".getBytes();
            byte[] ciphertext = encrypt(key, args[1]);

            try {
                //faz registro do mestre com o nome dado
                Registry registry = LocateRegistry.getRegistry(hostname);

                //objeto remoto que executara os metodos
                final Master stub = (Master) registry.lookup("mestre");

                //Imprime o header do CSV
                System.out.println("Tamanho do Vetor;Tempo de Execução Estático;Tempo de Execução Distribuido");

                //testa vetores (tamanho de 500 até 10ˆ6 com intervalos de 500)
                //Executa Calculo Serial Não paralelizado
//                Sequencial s = new Sequencial(ciphertext, knownword);
//                long tempoInicialEstatico = System.nanoTime();
//                s.atacar();
//                long tempoFinalEstatico = System.nanoTime();

                //Executa calculo Paralelo
                long tempoInicial = System.nanoTime();
                Guess g[] = stub.attack(ciphertext, knownword);
                long tempoFinal = System.nanoTime();

                GuessPrinter.print(g);

                //Calcula tempo gasto em ambos os casos
//                tempoExecucaoEstatico = 0; //(tempoFinalEstatico - tempoInicialEstatico);
                long tempoExecucao = (long) ((tempoFinal - tempoInicial) / 1000000000.0);

//                System.out.println(vetorInicial.size() + ";" + tempoExecucaoEstatico + ";" + tempoExecucao);
                System.out.println(tempoExecucao);
            } catch (RemoteException | NotBoundException e) {
                System.err.println("Erro encontrado (cliente): " + e.toString());
                e.printStackTrace();
            }
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
	Invocar o mestre passando o vetor de bytes, e imprimir chaves candidatasencontradas (se houver). Cada mensagem candidata deve ser colocada num
	arquivo com o nome da chave e a extensão .msg (por exemplo “house.msg” se a chave for house.)
     */
}
