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
public class FC extends Node {
    
    // FCs have separate indices
    
    private int idx;
    
    private OriginArc arc;
    
    protected int[] v; // index is product
    private Restock[] restock;
    
    
    
    public FC(String name, double lat, double lng, int num_zones, int capacity, Network network){
        super(name, lat, lng, num_zones, capacity, network);
        
        idx = network.fc_next_idx++;
        v = new int[network.params.P];
        
    }
    
    public void setRestock(Restock[] restock){
        this.restock = restock;
    }
    public Restock[] getRestock(){
        return restock;
    }
    
    public int getInventory(){
        int output = 0;
        
        for(int i : v){
            output += i;
        }
        
        return output;
    }
    
    
    
    public void setOriginArc(OriginArc a){
        arc = a;
    }
    
    public int getIdx(){
        return idx;
    }
    

    
    public void update(Network network){
        
        for(int p = 0; p < arc.gamma.length; p++){
            for(int d = 0; d < arc.gamma[p].length; d++){
                int new_packages = arc.gamma[p][d]; 
                x[network.params.SIZES[p]][d] += new_packages;
                network.new_packages += new_packages;
                v[p] -= new_packages;
                
                if(network.params.TRACK_PACKAGES){
                    for(int a = 0; a < new_packages; a++){
                        Shipment ship = arc.gamma_track[p][d].get(a);
                        ship.fulfill_time = network.t;
                        
                        
                        Network.fulfillTime.add(ship.fulfill_time - ship.create_time);
                        x_track[network.params.SIZES[p]][d].add(ship);
                    }
                }
                
                /*
                if(arc.gamma[p][d] > 0){
                    System.out.println("Fulfilled "+d+" at "+idx);
                }
                */
            }
        }
        
        restock(network);
        
        
        super.update(network);
    }
    
    public void restock(Network network){
        for(int p = 0; p < v.length; p++){
            
            double res = Math.min(network.params.inventory_max - v[p], restock[p].nextDraw(network));
            v[p] += res;
            network.new_inventory += res;
            
            network.total_inventory += v[p];
        }
    }
}
