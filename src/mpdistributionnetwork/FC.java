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
    private static int next_idx = 0;
    private int idx;
    
    private OriginArc arc;
    
    protected int[] v; // index is product
    private Restock[] restock;
    
    
    
    public FC(String name, double lat, double lng, int num_zones, int capacity, Restock[] restock){
        super(name, lat, lng, num_zones, capacity);
        
        idx = next_idx++;
        v = new int[Params.P];
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
                x[Params.SIZES[p]][d] += arc.gamma[p][d];
                v[p] -= arc.gamma[p][d];
            }
        }
        
        for(int p = 0; p < v.length; p++){
            v[p] += restock[p].nextDraw();
        }
        
        super.update();
    }
}
