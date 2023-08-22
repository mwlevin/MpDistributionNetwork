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
import java.util.PriorityQueue;
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
        
        for(int x = 5; x >= 5; x -= 5){
            Params params = new Params();
            params.greedy2 = true;
            params.epsilon_cap = params.epsilon_inv = x/100.0;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_beta"+params.beta+"_"+mp+"_"+(params.greedy2?"rev":"")+".txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
            
            System.gc();
        }
        
        for(int x = 5; x >= 5; x -= 5){
            Params params = new Params();
            
            params.epsilon_cap = params.epsilon_inv = x/100.0;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_beta"+params.beta+"_"+mp+"_"+(params.greedy2?"rev":"")+".txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
            
            System.gc();
        }
        
        mp = true;
        
        for(int x = 5; x >= 5; x -= 5){
            Params params = new Params();
            params.epsilon_cap = params.epsilon_inv = x/100.0;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_beta"+params.beta+"_"+mp+"_rev.txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
            
            System.gc();
        }
        

        /*
        for(int x = 1; x <= 10; x += 2){
            if(x == 5){
                continue;
            }
            Params params = new Params();
            params.node_beta = x / 100.0;
            params.epsilon_cap = params.epsilon_inv = 0.1;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_betanode"+params.node_beta+"_"+mp+".txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
            
            System.gc();
        }
        */
        
        // 0.005, 0.01, 0.05, 0.1, 
        /*
        for(int x = 3; x <= 10; x += 2){
            if(x == 5){
                continue;
            }
            Params params = new Params();
            params.beta = x / 100.0;
            params.epsilon_cap = params.epsilon_inv = 0.1;
            
            PrintStream out = new PrintStream(new FileOutputStream("log_"+params.epsilon_cap+"_beta"+params.beta+"_"+mp+"_rev.txt"), true);
            Network test = new Network(mp, params);
            test.simulate(out);

            test = null;
            
            System.gc();
        }
        
        */
        
        
        
        /*
        MapViewer map = new MapViewer(test, 1000, 1600);
        
        JFrame frame = new JFrame();
        frame.add(map);
        frame.pack();
        frame.setVisible(true);
        */
        

        

    }
    
}
