/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Link {
    private Location start, dest;
    private double cost;
    protected int[][][] y; // 1st index is time
    protected IloIntVar[][] mpvar_y; // this is for 1st time step
    
    protected int tt;
    
    
    public Link(Location start, Location dest, int num_zones){
        this.start = start;
        this.dest = dest;
        this.cost = Params.haversine(start, dest);
        
        
        this.tt = (int)Math.max(1, Math.ceil(this.cost / Params.SPEED));
        
        if(dest instanceof ZIP3){
            tt = 1;
        }
        
        y = new int[tt][Params.S][num_zones];
        
        start.addOutgoing(this);
        dest.addIncoming(this);
    }
    
    public void setY(IloCplex cplex) throws IloException{
        for(int s = 0; s < y.length; s++){
            for(int d = 0; d < y.length; d++){
                if(mpvar_y[s][d] != null){
                    y[0][s][d] = (int)Math.round(cplex.getValue(mpvar_y[s][d]));
                }
            }
        }
    }
    
    public void createVariables(IloCplex cplex) throws IloException {
        for(int s = 0; s < y.length; s++){
            for(int d = 0; d < y.length; d++){
                if(dest.isValidDest(d)){
                    continue;
                }
                mpvar_y[s][d] = cplex.intVar(0, 1000);
                y[0][s][d] = 0;
            }
        }
    }
    
    public void update(){
        for(int tau = y.length-1; tau > 0; tau--){
            y[tau] = y[tau-1];
        }
    }
    
    public Location getStart(){
        return start;
    }
    
    public Location getDest(){
        return dest;
    }
    
    public double getCost(){
        return cost;
    }
}
