/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public class Demand implements RandomVar {
    
    private double lambda;
    
    private double[] cdf;
    
    public Demand(double lambda){
        this.lambda = lambda;
        
        List<Double> temp = new ArrayList<>();
        
        int fact = 1;
        
        for(int k = 0; ; k++){
            if(k > 1){
                fact *= k;
            }
            
            double p = Math.pow(lambda, k) * Math.pow(Math.E, -lambda) / fact;
            
            if(p < 0.01){
                break;
            }
            
            temp.add(p);
        }
        
        cdf = new double[temp.size()];
        
        double cumulative = 0;
        
        for(int i = 0; i < cdf.length; i++){
            cumulative += temp.get(i);
            cdf[i] = cumulative;
        }
    }
    
    public int nextDraw() {
        double p = Params.rand.nextDouble();
        
        for(int i = 0; i < cdf.length-1; i++){
            if(cdf[i] > p){
                return i;
            }
        }
        
        return cdf.length;
    }
    
    
    public double getAvg(){
        return lambda;
    }
}
