/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author micha
 */
public class DS extends Node {
    
    private Set<Integer> valid_zip;
    
    
    public DS(String name, double lat, double lng, int num_zones, int capacity, Network network){
        super(name, lat, lng, num_zones, capacity, network);
        
        valid_zip = new HashSet<>();
    }
    
    public boolean isValidDest(int d){
        return valid_zip.contains(d);
    }
    
    public void addOutgoing(Link l){
        super.addOutgoing(l);
        
        if(l.getDest() instanceof ZIP3){
            valid_zip.add( ((ZIP3)l.getDest()).getIdx() );
        }
    }
}
