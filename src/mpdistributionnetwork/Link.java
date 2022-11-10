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
public class Link {
    private Node start, dest;
    private double cost;
    
    
    public Link(Node start, Node dest, double cost){
        this.start = start;
        this.dest = dest;
        this.cost = cost;
    }
    
    public Node getStart(){
        return start;
    }
    
    public Node getDest(){
        return dest;
    }
    
    public double getCost(){
        return cost;
    }
}
