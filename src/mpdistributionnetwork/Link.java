/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Link {
    private Location start, dest;
    private double dist;
    protected int[][][] y; // 1st index is time
    protected ArrayList<Shipment>[][][] y_track;
    protected IloIntVar[][] mpvar_y; // this is for 1st time step
    
    protected int tt;

    
    public Link(Location start, Location dest, int num_zones, Network network){
        this.start = start;
        this.dest = dest;
        this.dist = network.params.haversine(start, dest);
        
        
        this.tt = (int)Math.max(1, Math.ceil(this.dist / network.params.SPEED));
                
        if(dest instanceof ZIP3){
            tt = 1;
        }
        
        y = new int[tt][network.params.S][num_zones];
        
        if(network.params.TRACK_PACKAGES){
            y_track = new ArrayList[tt][network.params.S][num_zones];

            for(int t = 0; t < tt; t++){
                for(int s = 0; s < y_track[t].length; s++){
                    for(int d = 0; d < y_track[t][s].length; d++){
                        y_track[t][s][d] = new ArrayList<>();
                    }
                }
            }
        }
        
        
        mpvar_y = new IloIntVar[network.params.S][num_zones];
        
        start.addOutgoing(this);
        dest.addIncoming(this);

    }
    
    public Location[] getCoordinates(){
        return new Location[]{start, dest};
    }
    
    public void setY(IloCplex cplex, Network network) throws IloException{
        
        
        for(int s = 0; s < y[0].length; s++){
            for(int d = 0; d < y[0][s].length; d++){
                if(mpvar_y[s][d] != null){
                    y[0][s][d] = (int)Math.round(cplex.getValue(mpvar_y[s][d]));
                }
                else{
                    y[0][s][d] = 0;
                }
                
                if(network.params.TRACK_PACKAGES){
                    y_track[0][s][d] = new ArrayList<>();
                }
            }
        }
        
        
    }
    

    
    public void update(Network network){
        
        
        
        
        for(int tau = y.length-1; tau > 0; tau--){
            y[tau] = y[tau-1];
            
            if(network.params.TRACK_PACKAGES){
                y_track[tau] = y_track[tau-1];
            }
        }
        
        

    }
    
    public Location getStart(){
        return start;
    }
    
    public Location getDest(){
        return dest;
    }
    
    public double getCost(Network network){
        return dist / network.params.SPEED + dest.getEstimatedQueueDelay();
    }
    
}
