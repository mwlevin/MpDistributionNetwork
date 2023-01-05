/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

/**
 *
 * @author micha
 */
public class Periodic implements RandomVar {
    
    private double lambda;
    private double extra;
    
    public Periodic(double lambda){
        this.lambda = lambda;
    }
    
    public int nextDraw(Network network){
        if(network.t % network.params.DAY == 0){
            double temp = lambda*network.params.DAY;
            int output = (int)Math.floor(temp);
            
            extra = temp - output;
            return output;
        }
        return 0;
    }
    
    public double getAvg(){
        return lambda;
    }
}
