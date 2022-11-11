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
        IloCplex cplex = new IloCplex();
        cplex.setOut(Params.out);
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                    //System.out.println("***** "+p+" "+d);
                for(int i = 0; i < arcs.length; i++){
                    if(chi[p][d] > 0){
                        int omega = chi[p][d] - arcs[i].fc.x[Params.SIZES[p]][d];

                        //double obj_weight = omega - Params.beta * arcs[i].fc.getCost(d);
                        double obj_weight = omega;
                        


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
            //cplex.addLe(lhs, arcs[i].fc.getCapacity());
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
                
                for(int i = 0; i < arcs.length; i++){

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
