package br.inf.ufes.pp2016_01;

import java.io.*;
import java.rmi.registry.*;
import java.util.Scanner;
import java.util.Random;
import java.util.List;
/*
Receber um argumento na linha de comandos que indica o nome do arquivo que contém o vetor de bytes (com a mensagem criptografada) e outro argumento que
indica a palavra conhecida que consta da mensagem. Caso o arquivo não exista, o cliente deve gerar o vetor de bytes aleatoriamente e salvá-lo em arquivo. Em um
terceiro parâmetro pode ser especificado com o tamanho do vetor a ser gerado. Se esse terceiro parâmetro não existe, o tamanho do vetor deve ser gerado
aleatoriamente (na faixa 1000 a 100000).
*/
public class Client {
	public static void main(String[] args) {

		List<String> dictionary = new ArrayList<String>();
	    FileManager filemanager;
	    File filename = new File("/tmp/dictionary.txt");

	    try {
	            scanner = new Scanner(new File(filename));

	            while(scanner.hasNext()){
	                String line = scanner.nextLine();
	                dictionary.add(line);
	            }
	            scanner.close();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            System.out.println("Arquivo nao enconrado.");
	        }

	    //recebe nome que foi associado ao mestre (para buscar no Registry)
	   /*String hostname = null;
	    if (args.length > 0) {
	        hostname = args[0];
	    }
	*/
	    String hostname = (args.length < 3) ? null : args[2];
	    String filename = args[0];
	    String knownword = args[1];
	    String name = "mestre";
	    byte[] ciphertext;


	}


	/*
	Invocar o mestre passando o vetor de bytes, e imprimir chaves candidatasencontradas (se houver). Cada mensagem candidata deve ser colocada num
	arquivo com o nome da chave e a extensão .msg (por exemplo “house.msg” se a chave for house.)
	*/
}
