/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Node extends Location{
    
    protected int[][] x; // first index is sizes, 2nd index is destination
    protected double[] cost; // cost to go for destination
    
    private int capacity;

    public Node(String name, double lat, double lng, int num_zones, int capacity){
        super(name, lat, lng);
        

        x = new int[Params.S][num_zones];
        cost = new double[num_zones];
    }
 
    
    public boolean isValidDest(int d){
        return true;
    }
    
    
    public void setCost(ZIP3 z, double c){
        cost[z.getIdx()] = c;
    }
    
    public double getCost(ZIP3 z){
        return cost[z.getIdx()];
    }
    
    
    

    
    public int getCapacity(){
        return capacity;
    }
    
    public void step() throws IloException {
        
        // solve MP problem
        
        IloCplex cplex = new IloCplex();
        
        for(Link ij : outgoing){
            ij.createVariables(cplex);
        }
        
        // inventory
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                IloLinearNumExpr lhs = cplex.linearNumExpr();
                
                for(Link ij : outgoing){
                    if(ij.mpvar_y[s][d] != null){
                        lhs.addTerm(1, ij.mpvar_y[s][d]);
                    }
                }
                
                cplex.addLe(lhs, x[s][d]);
            }
        }
        
        // capacity
        
        IloLinearNumExpr lhs = cplex.linearNumExpr();
        
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                for(Link ij : outgoing){
                    if(ij.mpvar_y[s][d] != null){
                        lhs.addTerm(1, ij.mpvar_y[s][d]);
                    }
                }
            }
        }
        
        cplex.addLe(lhs, getCapacity());
        
        
        
        
        cplex.solve();
        
        for(Link ij : outgoing){
            ij.setY(cplex);
        }
        
        cplex.end();
    }
    
    public void update(){
        
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                
                for(Link inc : incoming){
                    x[s][d] += inc.y[inc.tt-1][s][d];
                }
                
                for(Link out : outgoing){
                    x[s][d] -= out.y[0][s][d];
                }
            }
        }
        
        for(Link l : incoming){
            l.update();
        }
    }

}
