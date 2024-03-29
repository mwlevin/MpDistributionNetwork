/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.gui.jmapviewer.Coordinate;

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
    
    public int getCapacity(){
        return 0;
    }
    
    public double getTotalIncCapacity(){
        double output = 0;
        
        for(Link l : incoming){
            output += l.getStart().getCapacity();
        }
        
        return output;
    }
    
    public Coordinate getCoordinate()
    {
        return new Coordinate(lat, lng);
    }

    
    public void addIncoming(Link l){
        incoming.add(l);
    }
    
    public void addOutgoing(Link l){
        outgoing.add(l);
    }
    
    public double getEstimatedQueueDelay(){
        return 0;
    }
    
    public int getTotalX(){
        return 0;
    }
    
    public abstract void step(Network network) throws IloException ;
    public abstract void update(Network network);
    public abstract void setCost(ZIP3 d, double cost);
    public abstract double getCost(int d);
    public abstract boolean isValidDest(int d);
    
    public String getName(){
        return name;
    }
    
    public double getLatitude(){
        return lat;
    }
    
    public double getY(){
        return lng;
    }
    
    public double getX(){
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
