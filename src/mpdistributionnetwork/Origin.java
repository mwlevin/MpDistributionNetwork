/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

/**
 *
 * @author micha
 */
public class Origin {
    
    private OriginArc[] arcs; // index is fc
    
    private int[][] chi; // first index is product, 2nd index is destination
    
    private Demand dem[][];

    public Origin(FC[] fc, Demand dem[][]){
        int num_fc = fc.length;
        int num_zones = dem[0].length;
        
        
        chi = new int[Params.P][num_zones];
 
        this.dem = dem;
        arcs = new OriginArc[num_fc];
        
        this.dem = dem;
        
        for(int i = 0; i < arcs.length; i++){
            arcs[i] = new OriginArc(num_zones, fc[i]);
        }
    }
    
    
    public void step() throws IloException {
        if(Network.useMP){
            stepMP();
        }
        else{
            stepSimple();
        }
    }
    
    public void stepSimple() throws IloException {
        
        
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                    //System.out.println("***** "+p+" "+d);
                for(int i = 0; i < arcs.length; i++){
                    arcs[i].gamma[p][d] = 0;
                }
            }
        }
        
        // find nearest FC with product in stock and fulfill
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                
                
                int orders = chi[p][d];
                while(orders > 0){
                    
                    OriginArc best = null;
                    double cost = Integer.MAX_VALUE;
                    
                    double temp = 0;
                    int fulfilled = 0;
                    
                    
                    for(OriginArc a : arcs){
                        
 
                        int product_sent = 0;
                        int total_sent = 0;
                        
                        for(int d2 = 0; d2 < chi[p].length; d2++){
                            product_sent += a.gamma[p][d2];
                        }
                        
                        for(int p2 = 0; p2 < chi.length; p2++){
                            for(int d2 = 0; d2 < chi[p2].length; d2++){
                                total_sent += a.gamma[p2][d2];
                            }
                        }
                        
                        int avail = Math.min(a.fc.v[p] - product_sent, a.fc.getCapacity() - total_sent);
                        
                        
                        if(avail > 0 && (temp = a.fc.getCost(d)) < cost){
                            cost = temp;
                            best = a;
                            fulfilled = Math.min(avail, orders);
                        }
                    }
                    
                    if(best == null){
                        break;
                    }
                    

                    orders -= fulfilled;
                    best.gamma[p][d] += fulfilled;
                    
                }

            }
        }
    }
    
    public void stepMP() throws IloException {
        IloCplex cplex = new IloCplex();
        cplex.setOut(Params.out);
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                    //System.out.println("***** "+p+" "+d);
                for(int i = 0; i < arcs.length; i++){
                    if(chi[p][d] > 0){
                        int omega = chi[p][d] - arcs[i].fc.x[Params.SIZES[p]][d];

                        double obj_weight = omega - Params.beta * arcs[i].fc.getCost(d);
                        //double obj_weight = omega;
                        


                        if(obj_weight > 0){
                            arcs[i].mpvar_gamma[p][d] = cplex.intVar(0, chi[p][d]);
                            obj.addTerm(obj_weight, arcs[i].mpvar_gamma[p][d]);
                        }
                        else{
                            arcs[i].mpvar_gamma[p][d] = null;
                        }
                    }
                    else{
                        arcs[i].mpvar_gamma[p][d] = null;
                    }
                } 
            }
        }
        cplex.addMaximize(obj);

        // number of orders
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                IloLinearNumExpr lhs = cplex.linearNumExpr();
                
                for(int i = 0; i < arcs.length; i++){
                    if(arcs[i].mpvar_gamma[p][d] != null){
                        lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                    }
                }
                
                cplex.addLe(lhs, chi[p][d]);
            }
        }
        
        // inventory availability
        for(int p = 0; p < chi.length; p++){
            for(int i = 0; i < arcs.length; i++){
                IloLinearNumExpr lhs = cplex.linearNumExpr();
                for(int d = 0; d < chi[0].length; d++){
                    if(arcs[i].mpvar_gamma[p][d] != null){
                        lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                    }
                }
                cplex.addLe(lhs, arcs[i].fc.v[p]);
            }
        }
        
        // capacity
        for(int i = 0; i < arcs.length; i++){    
            IloLinearNumExpr lhs = cplex.linearNumExpr();
            
            for(int p = 0; p < chi.length; p++){
                for(int d = 0; d < chi[p].length; d++){
                    if(arcs[i].mpvar_gamma[p][d] != null){
                        lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                    }
                }  
            }
            cplex.addLe(lhs, arcs[i].fc.getCapacity());
        }

        
        
        cplex.solve();

        /*
        System.out.println("obj: "+cplex.getObjValue());
        */
        for(int i = 0; i < arcs.length; i++){
            arcs[i].setGamma(cplex);
        }
        
        cplex.end();
        
       
    }
    
    public void update(){
        /*
        for(int p = 0; p < chi.length; p++){
            
            
            for(int i = 0; i < arcs.length; i++){
                int total_move = 0;
                
                for(int d = 0; d < chi[p].length; d++){
                    total_move += arcs[i].gamma[p][d];
                }
                
                if(total_move > 0){
                    System.out.println(total_move+" "+arcs[i].fc.v[p]);
                }
            }
        }
        */
        
        
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){

                double orders = dem[p][d].nextDraw();
                chi[p][d] += orders;
                Network.new_orders += orders;
                
                int total = 0;
                
                for(int i = 0; i < arcs.length; i++){
                    total += arcs[i].gamma[p][d];
                    chi[p][d] -= arcs[i].gamma[p][d];
                }
                
                if(chi[p][d] < 0){
                    throw new RuntimeException("chi[p][d] < 0 "+ chi[p][d]);
                }
                
                Network.total_orders += chi[p][d];
            }
        }
    }
}
