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
        
        // solve outgoing MP problem
        for(int i = 0; i < arcs.length; i++){
            arcs[i].createVariables(cplex);
        }
        
        // number of orders
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                IloLinearNumExpr lhs = cplex.linearNumExpr();
                
                for(int i = 0; i < arcs.length; i++){
                    lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                }
                
                cplex.addLe(lhs, chi[p][d]);
            }
        }
        
        // inventory availability
        for(int p = 0; p < chi.length; p++){
            for(int i = 0; i < arcs.length; i++){
                IloLinearNumExpr lhs = cplex.linearNumExpr();
                for(int d = 0; d < chi[0].length; d++){
                    lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                }
                cplex.addLe(lhs, arcs[i].fc.v[p]);
            }
        }
        
        // capacity
        for(int i = 0; i < arcs.length; i++){    
            IloLinearNumExpr lhs = cplex.linearNumExpr();
            
            for(int p = 0; p < chi.length; p++){
                for(int d = 0; d < chi[p].length; d++){
                    lhs.addTerm(1, arcs[i].mpvar_gamma[p][d]);
                }  
            }
            cplex.addLe(lhs, arcs[i].fc.getCapacity());
        }
        
        
        cplex.solve();
        
        for(int i = 0; i < arcs.length; i++){
            arcs[i].setGamma(cplex);
        }
        
        cplex.end();
        
       
    }
    
    public void update(){
        for(int p = 0; p < chi.length; p++){
            for(int d = 0; d < chi[p].length; d++){
                chi[p][d] += dem[p][d].nextDraw();
                
                for(int i = 0; i < arcs.length; i++){
                    chi[p][d] -= arcs[i].gamma[p][d];
                }
            }
        }
    }
}
