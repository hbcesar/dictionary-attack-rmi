/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.pp2016_01;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hbcesar
 */
public class GuessPrinter {
    public static void print(Guess[] guesses){
        for(Guess g : guesses){
            FileOutputStream out = null;
            try {
                String filename = g.getKey();
                byte[] mensagem = g.getMessage();
                filename += ".msg";

                out = new FileOutputStream(filename);
                out.write(mensagem);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(GuessPrinter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
