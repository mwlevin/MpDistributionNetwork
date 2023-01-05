/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JFrame;

/**
 *
 * @author micha
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IloException, Exception {
        // TODO code application logic here


        boolean mp = false;
        
        for(int x = 10; x <= 40; x += 5){
            Params params = new Params();
            params.epsilon_cap = params.epsilon_inv = x/100.0;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_beta"+params.beta+"_"+mp+".txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
        }
        
        /*
        MapViewer map = new MapViewer(test, 1000, 1600);
        
        JFrame frame = new JFrame();
        frame.add(map);
        frame.pack();
        frame.setVisible(true);
        */
        

        

    }
    
}
