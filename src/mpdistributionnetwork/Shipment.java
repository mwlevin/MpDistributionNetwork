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
public class Shipment implements Comparable<Shipment> {
    public int p;
    public int d;
    public int create_time;
    public int fulfill_time;
    
    
    public Shipment(int p, int d, int t){
        create_time = t;
        this.p = p;
        this.d = d;
    }
    
    public int compareTo(Shipment rhs){
        return create_time - rhs.create_time;
    }

    public String toString(){
        return ""+create_time;
    }
    
}
