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
public class Node {
    private int idx;
    private static int next_idx = 0;
    
    private int[][] x; // first index is sizez, 2nd index is destination
    private double[] cost; // cost to go for destination
    
    public Node(int num_zones){
        this.idx = next_idx++;
        x = new int[Params.SIZES][num_zones];
        cost = new double[num_zones];
    }

}
