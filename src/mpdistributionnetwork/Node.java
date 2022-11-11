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
        this.capacity = capacity;
    }
 
    
    public boolean isValidDest(int d){
        return true;
    }
    
    
    public void setCost(ZIP3 z, double c){
        cost[z.getIdx()] = c;
    }
    
    public double getCost(int d){
        return cost[d];
    }
    
    
    

    
    public int getCapacity(){
        return capacity;
    }
    
    public void step() throws IloException {
        
        // solve MP problem
        
        IloCplex cplex = new IloCplex();
        cplex.setOut(Params.out);
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        
        for(Link ij : outgoing){
            for(int s = 0; s < x.length; s++){
                for(int d = 0; d < x[s].length; d++){
                    if(x[s][d] > 0){
                        double w = x[s][d];
                        
                        if(ij.getDest() instanceof Node){
                            w -= ((Node)ij.getDest()).x[s][d];
                        }
                        
                        double obj_weight = w + Params.beta * (getCost(d) - ij.getDest().getCost(d));
                        
                        if(obj_weight > 0){
                            ij.mpvar_y[s][d] = cplex.intVar(0, x[s][d]);
                            obj.addTerm(obj_weight, ij.mpvar_y[s][d]);
                        }
                        else{
                            ij.mpvar_y[s][d] = null;
                        }
                    }
                    else{
                        ij.mpvar_y[s][d] = null;
                    }
                }
            }
        }
        cplex.addMaximize(obj);
        
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
                    
                    /*
                    if(inc.y[inc.tt-1][s][d] > 0){
                        System.out.println("Received "+d+" at "+getName()+" "+getClass().getName());
                    }
*/
                }
                
                for(Link out : outgoing){
                    
                    x[s][d] -= out.y[0][s][d];
                    
                    /*
                    if(out.y[0][s][d] > 0){
                        System.out.println("Shipped "+d+" from "+getName()+" "+getClass().getName());
                    }
*/
                }
                
                if(x[s][d] < 0){
                    throw new RuntimeException("x[s][d]<0 "+x[s][d]);
                }
                
                Network.total_packages += x[s][d];
                
            }
        }
        
        for(Link l : incoming){
            l.update();
        }
    }

}
