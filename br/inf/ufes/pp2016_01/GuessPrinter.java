package br.inf.ufes.pp2016_01;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuessPrinter {

    //recebe vetor com guesses e imprime cada uma em arquivo separado
    public static void print(Guess[] guesses) {
        for (Guess g : guesses) {
            FileOutputStream out = null;
            try {
                String filename = g.getKey();
                byte[] mensagem = g.getMessage();
                filename = filename + ".msg";

                out = new FileOutputStream(filename);
                out.write(mensagem);
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
