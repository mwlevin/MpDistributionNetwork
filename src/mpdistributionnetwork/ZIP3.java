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
public class ZIP3 extends Location {
    private int idx;
    
    
    private int pop;
    private int zip;
    
    public ZIP3(int zip, double lat, double lng, int pop, Network network){
        super(""+zip, lat, lng);
        this.pop = pop;
        
        idx = network.zip3_next_idx++;
    }
    
    public int getIdx(){
        return idx;
    }
    
    public int getPopulation(){
        return pop;
    }
    
    public double getCost(int d){
        if(d == idx){
            return 0;
        }
        else{
            return Integer.MAX_VALUE;
        }
    }
    
    public boolean isValidDest(int d){
        return d == idx;
    }
    
    public int hashCode(){
        return zip;
    }
    
    public void setCost(ZIP3 d, double cost){
        // do nothing  
    }
    
    public int getZipCode(){
        return zip;
    }
    
    public void step(Network network){
        // do nothing 
    }
    
    public void update(Network network){
        // do nothing
    }
}
