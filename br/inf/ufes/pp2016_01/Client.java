package br.inf.ufes.pp2016_01;

import java.rmi.registry.*;
/*
Receber um argumento na linha de comandos que indica o nome do arquivo que contém o vetor de bytes (com a mensagem criptografada) e outro argumento que
indica a palavra conhecida que consta da mensagem. Caso o arquivo não exista, o cliente deve gerar o vetor de bytes aleatoriamente e salvá-lo em arquivo. Em um
terceiro parâmetro pode ser especificado com o tamanho do vetor a ser gerado. Se esse terceiro parâmetro não existe, o tamanho do vetor deve ser gerado
aleatoriamente (na faixa 1000 a 100000).
*/
public class Client {


	public static void main(String[] args) {

//		List<String> dictionary = readDictionary();



	    //recebe nome que foi associado ao mestre (para buscar no Registry)
	   /*String hostname = null;
	    if (args.length > 0) {
	        hostname = args[0];
	    }
	*/
	    String hostname = (args.length < 3) ? null : args[2];
	    // String filename = args[0];
	    String knownword = args[0];
	    String name = "mestre";
            String msg = "A pipa do vovô não sobe mais.";
	    byte[] ciphertext = msg.getBytes();

                    try {
            //faz registro do mestre com o nome dado
            Registry registry = LocateRegistry.getRegistry(hostname);

            //objeto remoto que o qual executara os metodos
            final Master stub = (Master) registry.lookup("mestre");

            //Imprime o header do CSV
            System.out.println("Tamanho do Vetor;Tempo de Execução Estático;Tempo de Execução Distribuido");

            //testa vetores (tamanho de 500 até 10ˆ6 com intervalos de 500)

                //Executa Calculo Serial Não paralelizado
                Sequencial s = new Sequencial(ciphertext, knownword.getBytes());
                long tempoInicialEstatico = System.nanoTime();
                s.atacar();
                long tempoFinalEstatico = System.nanoTime();

                //Executa calculo Paralelo
                long tempoInicial = System.nanoTime();
                Guess g[] = stub.attack(ciphertext, knownword.getBytes());
                long tempoFinal = System.nanoTime();
                
                GuessPrinter.print(g);

                //Calcula tempo gasto em ambos os casos
//                tempoExecucaoEstatico = 0; //(tempoFinalEstatico - tempoInicialEstatico);
                long tempoExecucao = (long)((tempoFinal - tempoInicial)/1000000000.0);

//                System.out.println(vetorInicial.size() + ";" + tempoExecucaoEstatico + ";" + tempoExecucao);
                System.out.println(tempoExecucao);
            } catch (Exception e) {
            System.err.println("Erro encontrado (cliente): " + e.toString());
            e.printStackTrace();
        }
        }



	/*
	Invocar o mestre passando o vetor de bytes, e imprimir chaves candidatasencontradas (se houver). Cada mensagem candidata deve ser colocada num
	arquivo com o nome da chave e a extensão .msg (por exemplo “house.msg” se a chave for house.)
	*/
}
