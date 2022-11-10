/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public abstract class Location implements Comparable<Location> {
   
    private String name;
    private double lat, lng;
    
    protected List<Link> incoming, outgoing;
    
    protected double label;
    protected boolean inQ;
    
    public Location(String name, double lat, double lng){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        
        incoming = new ArrayList<>();
        outgoing = new ArrayList<>();
    }
    
    public void addIncoming(Link l){
        incoming.add(l);
    }
    
    public void addOutgoing(Link l){
        outgoing.add(l);
    }
    
    public abstract void step() throws IloException ;
    public abstract void update();
    public abstract void setCost(ZIP3 d, double cost);
    public abstract double getCost(ZIP3 d);
    public abstract boolean isValidDest(int d);
    
    public String getName(){
        return name;
    }
    
    public double getLatitude(){
        return lat;
    }
    
    public double getLongitude(){
        return lng;
    }
    
    public String toString(){
        return name;
    }
    
    public int compareTo(Location rhs){
        if(label < rhs.label){
            return -1;
        }
        else if(label > rhs.label){
            return 1;
        }
        else{
            return 0;
        }
    }
}
