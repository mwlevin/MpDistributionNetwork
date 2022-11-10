/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

/**
 *
 * @author micha
 */
public class FC extends Node {
    
    private int[] v; // index is product
    
    public FC(int num_zones){
        super(num_zones);
        
        v = new int[Params.P];
    }
}
