/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author micha
 */
public class Poisson implements RandomVar {
    
    private double lambda;
    
    private double[] cdf;
    
    public Poisson(double lambda){
        this.lambda = lambda;
        
        List<Double> temp = new ArrayList<>();
        
        double fact = 1;
        boolean found = false;
        
        double pow_lambda = Math.pow(Math.E, -lambda);
        
        for(int k = 0; ; k++){
            if(k > 1){
                fact *= k;
            }
            double p = pow_lambda / fact;
            pow_lambda *= lambda;
            
            if(found && p < 0.01){
                break;
            }
            
            if(p > 0.01){
                found = true;
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
    
    public String test(){
        int total = 0;
        
        for(int i = 0; i < 10000; i++){
            total += nextDraw();
        }
        
        return ""+(total/10000.0)+" "+lambda;
    }
    
    public int nextDraw() {
        double p = Params.rand.nextDouble();
        
        for(int i = 0; i < cdf.length; i++){
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
