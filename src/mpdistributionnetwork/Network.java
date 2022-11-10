/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class Network {
    private FC[] fcs;
    private ZIP3[] dests;
    
    private List<Location> all_nodes;
    
    private Origin origin;
    
    private Demand[][] dem;
    private Restock[][] restock;
    
    
    public Network() throws IOException {
        
        
        all_nodes = new ArrayList<>();
        
        
        
        
        Scanner filein = new Scanner(new File("data/uszips.txt"));
        filein.nextLine();
        
        int count = 0;
        
        List<ZIP3> zips = new ArrayList<>();
        while(filein.hasNext()){
            int zip = filein.nextInt();
            double lat = filein.nextDouble();
            double lng = filein.nextDouble();
            
            int pop = filein.nextInt();
            

            zips.add(new ZIP3(zip, lat, lng, pop));
            
            count++;
            
            if(count == 500){
                break;
            }
        }
        filein.close();
        
        dests = new ZIP3[zips.size()];
        for(ZIP3 z : zips){
            dests[z.getIdx()] = z;
        }
        
        
        
        
        List<FC> fc = new ArrayList<>();
        
        filein = new Scanner(new File("data/FC.txt"));
        filein.nextLine();
        
        while(filein.hasNext()){
            String name = filein.next();
            double lat = filein.nextDouble();
            double lng = filein.nextDouble();
            
            if(name.indexOf('/') >= 0){
                name = name.substring(0, name.indexOf('/'));
            }
            name = name.replaceAll("#", "");
            
            fc.add(new FC(name, lat, lng, dests.length, 0, null));
        }
        filein.close();
        
        fcs = new FC[fc.size()];
        
        for(FC f : fc){
            fcs[f.getIdx()] = f;
            all_nodes.add(f);
        }
        
        filein = new Scanner(new File("data/SC.txt"));
        filein.nextLine();
        
        List<SC> scs = new ArrayList<>();
        while(filein.hasNext()){
            String name = filein.next();
            double lat = filein.nextDouble();
            double lng = filein.nextDouble();
            
            if(name.indexOf('/') >= 0){
                name = name.substring(0, name.indexOf('/'));
            }
            name = name.replaceAll("#", "");
            SC sc = new SC(name, lat, lng, dests.length, 0);
            all_nodes.add(sc);
            scs.add(sc);
        }
        filein.close();
        
        filein = new Scanner(new File("data/DS.txt"));
        filein.nextLine();
        
        List<DS> dss = new ArrayList<>();
        
        while(filein.hasNext()){
            String name = filein.next();
            double lat = filein.nextDouble();
            double lng = filein.nextDouble();
            
            if(name.indexOf('/') >= 0){
                name = name.substring(0, name.indexOf('/'));
            }
            name = name.replaceAll("#", "");
            
            DS ds = new DS(name, lat, lng, dests.length, 0);
            all_nodes.add(ds);
            dss.add(ds);
        }
        filein.close();
        
        
        
        
        
        
        
        for(FC f : fcs){
            for(SC s : scs){
                new Link(f, s, dests.length);
            }
        }
        
        for(SC s : scs){
            for(Node d : dss){
                new Link(s, d, dests.length);
            }
        }
        
        for(FC f : fcs){
            for(Node d : dss){
                if(Params.haversine(f, d) <= Params.SPEED){
                    new Link(f, d, dests.length);
                }
            }
        }
        
        for(DS d : dss){
            for(ZIP3 z : dests){
                if(Params.haversine(d, z) <= Params.SPEED){
                    new Link(d, z, dests.length);
                }
            }
        }
        
        
        for(ZIP3 z : dests){
            if(z.incoming.size() == 0){
                
                DS best = null;
                double min = Integer.MAX_VALUE;
                
                for(DS d : dss){
                    double temp = Params.haversine(d, z);
                    if(temp < min){
                        min = temp;
                        best = d;
                    }
                }
                
                
                new Link(best, z, dests.length);
            }
        }
        
        
        dem = new Demand[Params.P][dests.length];
        
        
        
        origin = new Origin(fcs, dem);
        calcCosts();
    }
    
    public static int t;
    
    public void simulate() throws IloException {
        for(t = 0; t < Params.T; t++){
            step();
            update();
        }
    }
    
    
    
    public void step() throws IloException {
        for(Location n : all_nodes){
            n.step();
        }
        
        
        origin.step();
    }
    
    public void update(){
        for(Location n : all_nodes){
            n.update();
        }
        
        
        origin.update();
    }
    
    
    
    
    
    public void calcCosts(){
        
        for(ZIP3 d : dests){
            dijkstras(d);
            
            for(Location n : all_nodes){
                n.setCost(d, n.label);
            }
        } 
    }
    
    private void dijkstras(Location d){
        
        for(Location n : all_nodes){
            n.label = Integer.MAX_VALUE;
            n.inQ = false;
        }
        
        d.label = 0;
        d.inQ = true;
        
        PriorityQueue<Location> Q = new PriorityQueue<>();
        
        Q.add(d);
        
        while(!Q.isEmpty()){
            Location u = Q.remove();
            u.inQ = false;
            
            for(Link uv : u.incoming){
                double temp = u.label + uv.getCost();
                Location v = uv.getStart();
                
                if(temp < v.label){
                    v.label = temp;
                    
                    if(v.inQ){
                        Q.remove(v);
                    }
                    
                    v.inQ = true;
                    Q.add(v);
                }
            }
        }
    }
    
}
