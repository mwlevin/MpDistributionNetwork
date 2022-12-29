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
import java.util.List;

/**
 *
 * @author micha
 */
public class OriginArc {
    
    protected FC fc;
    
    protected int gamma[][]; // 1st index is product, 2nd index is dest
    protected List<Shipment> gamma_track[][];
    
    protected IloIntVar mpvar_gamma[][];
    
    
    public OriginArc(int num_zones, FC fc){
        gamma = new int[Params.P][num_zones];
        gamma_track = new List[Params.P][num_zones];
        
        for(int p = 0; p < gamma_track.length; p++){
            for(int d = 0; d < gamma_track[p].length; d++){
                gamma_track[p][d] = new ArrayList<>();
            }
        }
        
        
        mpvar_gamma = new IloIntVar[Params.P][num_zones];
        this.fc = fc;
        fc.setOriginArc(this);

    }
    
    public void setGamma(IloCplex cplex) throws IloException{
        for(int p = 0; p < gamma.length; p++){
            for(int d = 0; d < gamma[p].length; d++){
                if(mpvar_gamma[p][d] != null){
                    gamma[p][d] = (int)Math.round(cplex.getValue(mpvar_gamma[p][d]));
                }
                else{
                    gamma[p][d] = 0;
                }
            }
        }
    }
    

    
    
}
