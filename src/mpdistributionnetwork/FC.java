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
    
    
    
    public FC(String name, double lat, double lng, int num_zones, int capacity){
        super(name, lat, lng, num_zones, capacity);
        
        idx = Network.fc_next_idx++;
        v = new int[Params.P];
        
    }
    
    public void setRestock(Restock[] restock){
        this.restock = restock;
    }
    
    
    
    public void setOriginArc(OriginArc a){
        arc = a;
    }
    
    public int getIdx(){
        return idx;
    }
    

    
    public void update(){
        
        for(int p = 0; p < arc.gamma.length; p++){
            for(int d = 0; d < arc.gamma[p].length; d++){
                int new_packages = arc.gamma[p][d]; 
                x[Params.SIZES[p]][d] += new_packages;
                Network.new_packages += new_packages;
                v[p] -= new_packages;
                
                if(Params.TRACK_PACKAGES){
                    for(int a = 0; a < new_packages; a++){
                        Shipment ship = arc.gamma_track[p][d].get(a);
                        ship.fulfill_time = Network.t;
                        x_track[Params.SIZES[p]][d].add(ship);
                    }
                }
                
                /*
                if(arc.gamma[p][d] > 0){
                    System.out.println("Fulfilled "+d+" at "+idx);
                }
                */
            }
        }
        
        for(int p = 0; p < v.length; p++){
            
            double res = Math.min(Params.inventory_max - v[p], restock[p].nextDraw());
            v[p] += res;
            Network.new_inventory += res;
            
            Network.total_inventory += v[p];
        }
        
        super.update();
    }
}
