/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.util.Random;

/**
 *
 * @author micha
 */
public class Params {
    public static final int P = 10;
    public static final int[] SIZE = new int[]{}; // size cutoff
    public static final int S = SIZE.length+1;
    
    public static final double SPEED = 60;
    
    public static final int T = 365*24;
    
    
    
    
    
    public static Random rand = new Random(1234);
    
    
    
    public static int[] SIZES = new int[P];
    
    public static void init(){
        int s = 0;
        for(int p = 0; p < P; p++){
            if(s < SIZE.length && p >= SIZE[s]){
                s++;
            }
            
            SIZES[p] = s;
        }
    }
    
    
    public static double haversine(Location n1, Location n2){
        return haversine(n1.getLatitude(), n1.getLongitude(), n2.getLatitude(), n2.getLongitude());
    }
    public static double haversine(double lat1, double lon1,
                            double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 3958.8;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }
}
