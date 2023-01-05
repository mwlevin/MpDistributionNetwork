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
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Node extends Location{
    
    protected int[][] x; // first index is sizes, 2nd index is destination
    protected PriorityQueue<Shipment>[][] x_track;
    protected double[] cost; // cost to go for destination
    
    protected int total_processed;
    
    private int capacity;

    public Node(String name, double lat, double lng, int num_zones, int capacity){
        super(name, lat, lng);
        
        x = new int[Params.S][num_zones];
        
        if(Params.TRACK_PACKAGES){
            x_track = new PriorityQueue[Params.S][num_zones];

            for(int s = 0; s < x_track.length; s++){
                for(int d = 0; d < x_track[s].length; d++){
                    x_track[s][d] = new PriorityQueue<>();
                }
            }
        }
        
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
        if(Network.useMP){
            stepMP();
        }
        else {
            stepSimple();
        }
        
        
    }
    
    public void stepSimple(){
        
        int remainingCap = capacity;
        
        // set to 0 to start with
        for(Link ij : outgoing){
            for(int s = 0; s < x.length; s++){
                for(int d = 0; d < x[s].length; d++){
                    ij.y[0][s][d] = 0;
                }
            }
        }
        
        outer: for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                if(x[s][d] > 0 && remainingCap > 0){
                    // find best outgoing link
                    Link best = null;
                    double cost = Integer.MAX_VALUE;
                    double temp = 0;
                    
                    for(Link ij : outgoing){
                        if(ij.getDest().isValidDest(d) && (temp = ij.getDest().getCost(d)) < cost){
                            best = ij;
                            cost = temp;
                        }
                    }
                    
                    int fulfilled = Math.min(x[s][d], remainingCap);
                    
                    best.y[0][s][d] += fulfilled;
                    remainingCap -= fulfilled;
                    
                    if(remainingCap == 0){
                        break outer;
                    }
                }

                
            }
        }
    }
    
    
    public void stepMP() throws IloException {
        
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
                        
                        double obj_weight = w + Params.node_beta * (getCost(d) - ij.getDest().getCost(d));
                        
                        if(ij.getDest().isValidDest(d) && obj_weight > 0){
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
        
        if(Params.TRACK_PACKAGES){
            for(Link ij : outgoing){
            
                for(int s = 0; s < x.length; s++){
                    for(int d = 0; d < x[s].length; d++){
                        for(int a = 0; a < ij.y[0][s][d]; a++){
                            ij.y_track[0][s][d].add(x_track[s][d].remove());
                        }
                    }
                }
            }
        }
        
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                
                for(Link inc : incoming){
                    
                    int added = inc.y[inc.tt-1][s][d];
                    
                    x[s][d] += added;
                    total_processed += added;
                    
                    if(Params.TRACK_PACKAGES){
                        for(int a = 0; a < added; a++){
                            x_track[s][d].add(inc.y_track[inc.tt-1][s][d].get(a));
                        }
                    }
                    
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

                    
                    if(out.getDest() instanceof ZIP3 && out.y[0][s][d] > 0){
                        //System.out.println("Delivered "+d);
                        

                        int deliver = out.y[0][s][d];
                        Network.total_delivered += deliver;

                        if(Params.TRACK_PACKAGES){
                            for(int a = 0; a < deliver; a++){
                                Shipment ship = out.y_track[0][s][d].get(a);

                                int transport_time = Network.t - ship.fulfill_time+1;

                                
                                Network.transportTime.add(transport_time);
                            }
                        }

                    }
                    
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
        
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                if(Params.TRACK_PACKAGES && x[s][d] != x_track[s][d].size()){
                    throw new RuntimeException("Size mismatch "+x[s][d]+" "+x_track[s][d].size()+" "+getClass().getName());
                }
            }
        }
    }

}
