/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.io.File;
import java.io.IOException;
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

        Params.init();
        
        Network test = new Network(true);
        test.simulate(System.out);
        
        /*
        MapViewer map = new MapViewer(test, 1000, 1600);
        
        JFrame frame = new JFrame();
        frame.add(map);
        frame.pack();
        frame.setVisible(true);
        */
        

        
        test = new Network(false);
        test.simulate(System.out);    
    }
    
}
