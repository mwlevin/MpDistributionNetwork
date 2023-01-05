/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
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

    public Node(String name, double lat, double lng, int num_zones, int capacity, Network network){
        super(name, lat, lng);
        
        x = new int[network.params.S][num_zones];
        
        if(network.params.TRACK_PACKAGES){
            x_track = new PriorityQueue[network.params.S][num_zones];

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
    
    public void step(Network network) throws IloException {
        if(network.useMP){
            stepMP(network);
        }
        else {
            stepSimple(network);
        }
        
        
    }
    
    public void stepSimple(Network network){
        
        int remainingCap = capacity;
        
        // set to 0 to start with
        for(Link ij : outgoing){
            for(int s = 0; s < x.length; s++){
                for(int d = 0; d < x[s].length; d++){
                    ij.y[0][s][d] = 0;
                    
                    if(network.params.TRACK_PACKAGES){
                        ij.y_track[0][s][d] = new ArrayList<Shipment>();
                    }
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
    
    
    public void stepMP(Network network) throws IloException {
        
        // solve MP problem
        
        IloCplex cplex = new IloCplex();
        cplex.setOut(network.params.out);
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        
        for(Link ij : outgoing){
            for(int s = 0; s < x.length; s++){
                for(int d = 0; d < x[s].length; d++){
                    if(x[s][d] > 0){
                        double w = x[s][d];
                        
                        if(ij.getDest() instanceof Node){
                            w -= ((Node)ij.getDest()).x[s][d];
                        }
                        
                        double obj_weight = w + network.params.node_beta * (getCost(d) - ij.getDest().getCost(d));
                        
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
            ij.setY(cplex, network);
            
        }
        
        cplex.end();
    }
    
    public void update(Network network){
        
        if(network.params.TRACK_PACKAGES){
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
                    
                    if(network.params.TRACK_PACKAGES){
                        
                        ArrayList<Shipment> orig = inc.y_track[inc.tt-1][s][d];
                        for(int a = 0; a < added; a++){
                            // this should be a removal. Removing it at end
                            x_track[s][d].add(orig.get(a));
                        }
                        
                        
                        ArrayList<Shipment> temp = new ArrayList<>();
                        
                        for(int a = added; a < orig.size(); a++){
                            temp.add(orig.get(a));
                        }
                            
                            
                        inc.y_track[inc.tt-1][s][d] = temp;
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
                        network.total_delivered += deliver;

                        if(network.params.TRACK_PACKAGES){
                            for(int a = 0; a < deliver; a++){
                                Shipment ship = out.y_track[0][s][d].get(a);

                                int transport_time = network.t - ship.fulfill_time + out.tt;

                                network.transport_time.add(transport_time);
                            }
                            
                        }

                    }
                    
                }
                
                if(x[s][d] < 0){
                    throw new RuntimeException("x[s][d]<0 "+x[s][d]);
                }
                
                network.total_packages += x[s][d];
                
            }
        }
        
        for(Link l : incoming){
            l.update(network);
        }
        
        for(int s = 0; s < x.length; s++){
            for(int d = 0; d < x[s].length; d++){
                if(network.params.TRACK_PACKAGES && x[s][d] != x_track[s][d].size()){
                    throw new RuntimeException("Size mismatch "+x[s][d]+" "+x_track[s][d].size()+" "+getClass().getName());
                }
            }
        }
    }

}
