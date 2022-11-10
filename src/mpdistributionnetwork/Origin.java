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
public class Origin {
    private int[][] chi; // first index is product, 2nd index is destination
    
    public Origin(int num_zones){
        chi = new int[Params.P][num_zones];
    }
}
