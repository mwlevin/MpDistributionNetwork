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
            
            if(count == Params.NUM_ZONES){
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
      
            fc.add(new FC(name, lat, lng, dests.length, Params.FC_CAPACITY));
        }
        filein.close();
        
        fcs = new FC[fc.size()];
        
        for(FC f : fc){
            fcs[f.getIdx()] = f;
            all_nodes.add(f);
        }
        
        double total_capacity = 0;
        
        for(FC f : fcs){
            total_capacity += f.getCapacity();
        }
        
        total_capacity /= (1+Params.epsilon_cap);
        
        double[] total = new double[Params.P];
        
        double total_prob = 0;
        for(int p = 0; p < total.length; p++){
            total[p] = Params.rand.nextDouble();
            total_prob += total[p];
        }


        for(int p = 0; p < total.length; p++){
            total[p] = total_capacity * total[p] / total_prob;
            
        }
        
        
        
        double[][] prob = new double[fcs.length][Params.P];
        
        double[] total_p = new double[Params.P];
        
        for(int f = 0; f < fcs.length; f++){
            for(int p = 0; p < Params.P; p++){
                prob[f][p] = Params.rand.nextDouble();
                total_p[p] += prob[f][p];
            }
        }
        
        double total_inv = 0;
        
        for(int f = 0; f < fcs.length; f++){
            Restock[] r = new Restock[Params.P];
            
            for(int p = 0; p < Params.P; p++){
                r[p] = new Restock(prob[f][p] / total_p[p] * (1+Params.epsilon_inv)*total[p]);
                
                total_inv += r[p].getAvg();
            }
            
            fcs[f].setRestock(r);
            
        }
        
        prob = new double[Params.P][dests.length];
        total_p = new double[Params.P];
        
        for(int d = 0; d < dests.length; d++){
            for(int p = 0; p < Params.P; p++){
                prob[p][d] = Params.rand.nextDouble();
                total_p[p] += prob[p][d];
            }
        }
        
        Demand[][] dem = new Demand[Params.P][dests.length];
        
        double total_lambda = 0;
        for(int d = 0; d < dests.length; d++){
            for(int p = 0; p < Params.P; p++){
                dem[p][d] = new Demand(prob[p][d] / total_p[p] * total[p]);
                total_lambda += dem[p][d].getAvg();
            }
        }
        
        System.out.println("Total demand: "+total_lambda+" Total supply: "+total_inv);
        
        
        
        
        
        
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
            SC sc = new SC(name, lat, lng, dests.length, Params.SC_CAPACITY);
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
            
            DS ds = new DS(name, lat, lng, dests.length, Params.DS_CAPACITY);
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
        

        
        origin = new Origin(fcs, dem);
        calcCosts();
    }
    
    public static int t;
    public static int total_delivered, total_packages, total_orders, new_orders, total_inventory, new_inventory;
    
    public void simulate() throws IloException {
        
        System.out.println("Time\tOrders\tDemand\tInventory\tRestock\tPackages\tDelivered");
        
        for(t = 0; t < Params.T; t++){
            
            step();
            update();
            System.out.println(t+"\t"+total_orders+"\t"+new_orders+"\t"+total_inventory+"\t"+new_inventory+"\t"+total_packages+"\t"+total_delivered);
            
            total_orders = 0;
            total_packages = 0;
            total_delivered = 0;
            total_inventory = 0;
            new_inventory = 0;
            new_orders = 0;
        }
    }
    
    
    
    public void step() throws IloException {
        
        origin.step();
        
        
        for(Location n : all_nodes){
            n.step();
        }
        
        
        
    }
    
    public void update(){
        
        origin.update();
        
        for(Location n : all_nodes){
            n.update();
        }
        
        
        
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
