/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import ilog.concert.IloException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class Network {
    
    public int zip3_next_idx = 0;
    public int fc_next_idx = 0;
    
    private FC[] fcs;
    private ZIP3[] dests;
    
    private List<Node> all_nodes;
    
    private Origin origin;
    
    public Params params;
    
    public Random rand;

    
    public Network(boolean mp, Params params) throws IOException {
        this.params = params;
        zip3_next_idx = 0;
        fc_next_idx = 0;
        
        rand = new Random(1234);
        
        useMP = mp;
        
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
            

            zips.add(new ZIP3(zip, lat, lng, pop, this));
            
            count++;
            
            if(count == params.NUM_ZONES){
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
      
            fc.add(new FC(name, lat, lng, dests.length, params.FC_CAPACITY, this));
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
        
        total_capacity /= (1+params.epsilon_cap);
        
        double[] total = new double[params.P];
        
        double total_prob = 0;
        for(int p = 0; p < total.length; p++){
            total[p] = rand.nextDouble();
            total_prob += total[p];
        }


        for(int p = 0; p < total.length; p++){
            total[p] = total_capacity * total[p] / total_prob;
            
        }
        
        
        
        double[][] prob = new double[fcs.length][params.P];
        
        double[] total_p = new double[params.P];
        
        for(int f = 0; f < fcs.length; f++){
            for(int p = 0; p < params.P; p++){
                prob[f][p] = rand.nextDouble();
                total_p[p] += prob[f][p];
            }
        }
        
        double total_inv = 0;
        
        for(int f = 0; f < fcs.length; f++){
            Restock[] r = new Restock[params.P];
            
            for(int p = 0; p < params.P; p++){
                r[p] = new Restock(prob[f][p] / total_p[p] * (1+params.epsilon_inv)*total[p]);
                
                total_inv += r[p].getAvg();
            }
            
            fcs[f].setRestock(r);
            
        }
        
        prob = new double[params.P][dests.length];
        total_p = new double[params.P];
        
        for(int d = 0; d < dests.length; d++){
            for(int p = 0; p < params.P; p++){
                prob[p][d] = (rand.nextDouble()) * dests[d].getPopulation();
                total_p[p] += prob[p][d];
            }
        }
        
        Demand[][] dem = new Demand[params.P][dests.length];
        
        double total_lambda = 0;
        for(int d = 0; d < dests.length; d++){
            for(int p = 0; p < params.P; p++){
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
            SC sc = new SC(name, lat, lng, dests.length, params.SC_CAPACITY, this);
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
            
            DS ds = new DS(name, lat, lng, dests.length, params.DS_CAPACITY, this);
            all_nodes.add(ds);
            dss.add(ds);
        }
        filein.close();
        
        
        System.out.println("FCs: "+fc.size());
        System.out.println("SCs: "+scs.size());
        System.out.println("DSs: "+dss.size());
        
        
        
        for(FC f : fcs){
            for(SC s : scs){
                new Link(f, s, dests.length, this);
            }
        }
        
        for(SC s : scs){
            for(Node d : dss){
                new Link(s, d, dests.length, this);
            }
        }
        
        for(FC f : fcs){
            for(Node d : dss){
                if(params.haversine(f, d) <= params.SPEED){
                    new Link(f, d, dests.length, this);
                }
            }
        }
        
        for(DS d : dss){
            for(ZIP3 z : dests){
                if(params.haversine(d, z) <= params.SPEED){
                    new Link(d, z, dests.length, this);
                }
            }
        }
        
        
        for(ZIP3 z : dests){
            if(z.incoming.size() == 0){
                
                DS best = null;
                double min = Integer.MAX_VALUE;
                
                for(DS d : dss){
                    double temp = params.haversine(d, z);
                    if(temp < min){
                        min = temp;
                        best = d;
                    }
                }
                
                
                new Link(best, z, dests.length, this);
            }
        }
        

        
        origin = new Origin(fcs, dem, this);
        calcCosts();
    }
    
    public int t;
    public int total_delivered, total_packages, total_orders, new_orders, total_inventory, new_inventory, new_packages;
    public boolean useMP;
    
    private RunningAvg originTime, nodeTime, simTime;
    public RunningAvg fulfill_time, transport_time, fulfill_test;
    private int sim_delivered;
    private int sim_packages;
    private int sim_orders;
    
    public void simulate(PrintStream out) throws IloException {
        
        double total_lambda = 0;
        for(int d = 0; d < dests.length; d++){
            for(int p = 0; p < params.P; p++){
                total_lambda += origin.dem[p][d].getAvg();
            }
        }

        double total_inv = 0;
        for(int f = 0; f < fcs.length; f++){
            for(int p = 0; p < params.P; p++){
                total_inv += fcs[f].getRestock()[p].getAvg();
            }
            
        }
        
        out.println("Total demand\t"+total_lambda);
        out.println("Total restock\t"+total_inv);
        out.println("MP\t"+useMP);
        out.println("FC capacity\t"+params.FC_CAPACITY);
        out.println("SC capacity\t"+params.SC_CAPACITY);
        out.println("DS capacity\t"+params.DS_CAPACITY);

        originTime = new RunningAvg();
        nodeTime = new RunningAvg();
        simTime = new RunningAvg();
        fulfill_time = new RunningAvg();
        transport_time = new RunningAvg();
        
        
        out.println("Time\tOrders\tDemand\tInventory\tRestock\tTotal packages\tNew packages\tDelivered\tAvg fulfillment time\tAvg. transport time");
        
        
        for(int t = 0; t < 1; t++){
            for(FC f : fcs){
                f.restock(this);
            }
        }
        

        
        
        for(t = 0; t < params.T; t++){
            total_orders = 0;
            total_packages = 0;
            total_delivered = 0;
            total_inventory = 0;
            new_inventory = 0;
            new_orders = 0;
            new_packages = 0;
            
            fulfill_test = new RunningAvg();
            
            step();
            update();
            out.println(t+"\t"+origin.getNumOrders()+"\t"+new_orders+"\t"+total_inventory+"\t"+new_inventory+"\t"+total_packages+"\t"+new_packages+"\t"+total_delivered+"\t"+
                    fulfill_time.getAverage()+"\t"+transport_time.getAverage());
            
            sim_orders += new_orders;
            sim_delivered += total_delivered;
            sim_packages += new_packages;
            
            
        }
        
        out.println("Origin CPU time\t"+originTime.getAverage());
        out.println("Node CPU time\t"+nodeTime.getAverage());
        out.println("Simulation time\t"+simTime.getAverage());
        out.println("Total packages\t"+sim_packages);
        out.println("Total orders\t"+sim_orders);
        out.println("Total delivered\t"+sim_delivered);
        out.println("Fulfillment time\t"+fulfill_time.getAverage());
        out.println("Transport time\t"+transport_time.getAverage());
        
        
        out.println("\t");
        
        /*
        for(Node n : all_nodes){
            if(n instanceof SC){
                out.println("SC "+n.getName()+"\t"+((double)((SC) n).total_processed / params.T));
            }
        }
        */
    }
    
    public List<Node> getNodes(){
        return all_nodes;
    }
    
    public ZIP3[] getDests(){
        return dests;
    }
    
    
    
    public void step() throws IloException {
        
        long time2 = System.nanoTime();
        long time = time2;
        origin.step(this);
        
        originTime.add( (System.nanoTime() - time)/1.0e9);
        
        
        for(Location n : all_nodes){
            time = System.nanoTime();
            n.step(this);
            
            nodeTime.add( (System.nanoTime() - time)/1.0e9);
        }
        
        simTime.add((System.nanoTime()-time2)/1.0e9);
        
    }
    
    public void update(){
        
        origin.update(this);
        
        for(Location n : all_nodes){
            n.update(this);
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
                double temp = u.label + uv.getCost(this);
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
