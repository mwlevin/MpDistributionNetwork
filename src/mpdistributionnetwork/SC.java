/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.util.Set;

/**
 *
 * @author micha
 */
public class SC extends Node {
    public SC(String name, double lat, double lng, int num_zones, int capacity, Network network){
        super(name, lat, lng, num_zones, capacity, network);
    }
    
    public boolean isValidDest(int d){
        return true;
    }
    
}
