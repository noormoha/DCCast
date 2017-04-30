package dccast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author Mohammad Noormohammadpour
 */

public class Dccast {
    
    // network topology
    public static int Nodes;
    public static Topology topo;
    
    public static final Distribution dist = new Distribution();
    public static final int latest_arrival = 1000;
    
    // traffic arrival
    public static double copies_per_request; // destinations per P2MP
    public static double network_lambda; // lambda * (n)
    public static double lambda; 
    
    // job size
    public static final double mu = 0.1;
    public static final double kappa = 0.5;
    public static double demand_min = 0.1; 
    public static double demand_max = 0.9; 
    
    // global statistics and data    
    private static double worst_finish = 0, avg_finish = 0, median_finish = 0;
    private static double total_bw = 0.0;
    private static long time = 0; // time marker (real-time)
    private static long req_count = 0;
    private static double total_demand = 0;
    
    public static TreeMap<Long, Double> req_finishes = new TreeMap<>();
    public static TreeMap<Long, Double> req_arrivals = new TreeMap<>();
    public static TreeMap<Long, Double> req_demands = new TreeMap<>();
    public static TreeMap<Long, Double> req_demands_org = new TreeMap<>();
    
    ///////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) 
    {
        // generate a topology or use a fixed topology
        // sets # of nodes
        getTopo(20); 
        
        // # of destinations per P2MP transfer
        copies_per_request = 5;

        // arrival rate at central controller
        network_lambda = 1;

        // used by traffic generator
        lambda = network_lambda / Nodes;

        Requests_P2MP reqs = genTraffic_P2MP(Nodes, lambda);
        
        do_simulation_dccast(reqs.clone());
    }
    
    public static Topology getTopo5FullMesh() {
        Nodes = 10;
        topo = new Topology(Nodes);
        
        // setup network topology
        topo.add(0, 5);
        topo.add(1, 6);
        topo.add(2, 7);
        topo.add(3, 8);
        topo.add(4, 9);
        
        topo.add(5, 0);
        topo.add(6, 1);
        topo.add(7, 2);
        topo.add(8, 3);
        topo.add(9, 4);
        
        topo.add(5, 6);
        topo.add(6, 5);
        topo.add(5, 7);
        topo.add(7, 5);
        topo.add(8, 5);
        topo.add(5, 8);
        topo.add(9, 5);
        topo.add(5, 9);
        topo.add(7, 6);
        topo.add(6, 7);
        topo.add(8, 6);
        topo.add(6, 8);
        topo.add(9, 6);
        topo.add(6, 9);
        topo.add(8, 7);
        topo.add(7, 8);
        topo.add(9, 7);
        topo.add(7, 9);
        topo.add(8, 9);
        topo.add(9, 8);
        
        return topo;
    }
    
    public static Topology getTopoGscale() {
        Nodes = 12;
        topo = new Topology(Nodes);
        
        // simulate the GScale Network from Google       
        // new topology & flows list
        // 12 nodes and 19 bidirectional links
        topo.add(0, 1);
        topo.add(1, 2);
        topo.add(0, 4);
        topo.add(2, 3);
        topo.add(2, 5);
        topo.add(3, 4);
        topo.add(4, 5);
        topo.add(5, 7);
        topo.add(3, 6);
        topo.add(3, 7);
        topo.add(5, 6);
        topo.add(6, 7);
        topo.add(8, 10);
        topo.add(8, 9);
        topo.add(9, 10);
        topo.add(7, 9);
        topo.add(6, 10);
        topo.add(9, 11);
        topo.add(10, 11);
        topo.add(1, 0);
        topo.add(2, 1);
        topo.add(4, 0);
        topo.add(3, 2);
        topo.add(5, 2);
        topo.add(4, 3);
        topo.add(5, 4);
        topo.add(7, 5);
        topo.add(6, 3);
        topo.add(7, 3);
        topo.add(6, 5);
        topo.add(7, 6);
        topo.add(10, 8);
        topo.add(9, 8);
        topo.add(10, 9);
        topo.add(9, 7);
        topo.add(10, 6);
        topo.add(11, 9);
        topo.add(11, 10);
        
        return topo;
    }
    
    public static Topology getTopo(int n) {
        Nodes = n;
        topo = new Topology(Nodes);
        Random rand = new Random();
        
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < 2; j++) {
                int k = Math.abs(rand.nextInt()) % (n-1);
                topo.add(i, (i + k + 1) % n);
                topo.add((i + k + 1) % n, i);
            }
            
            topo.add(i, (i + 1) % n);
            topo.add((i + 1) % n, i);
        }
        
        return topo;
    }
      
    public static long tik() {
        time = System.currentTimeMillis();
        return time;
    }
    
    public static long toc() {
        return (System.currentTimeMillis() - time);
    }
    
    public static Requests_P2MP genTraffic_P2MP(int Nodes, double lambda)
    {
        long req_id = 0;
        Requests_P2MP reqs = new Requests_P2MP();
        Random rand = new Random();
        rand.setSeed(System.nanoTime());
        
        for(int i = 0; i < Nodes; i++) 
        {
            // generate traffic as requested 
            double[][] ij_requests = dist.genPoisson(
                    lambda, 
                    mu, 
                    latest_arrival, 
                    demand_min, 
                    demand_max, 
                    kappa,
                    false); // exponential job size (true -> uniform)

            for(double[] req : ij_requests)
            {                
                HashSet<Integer> set = new HashSet<>();
                
                while(set.size() < copies_per_request) {
                    int rnd = ((i+1) + rand.nextInt(Nodes - 2))%(Nodes);
                    set.add(rnd);
                }
                              
                reqs.add(req_id, i, new ArrayList<>(set), req[1], req[0], 0);
                
                req_id++;
            } 
        }
        
        return reqs; 
    }
    
    public static void update_env(Requests_P2MP reqs) {
        Requests_P2MP cloned = reqs.clone();
        req_count = cloned.size();
        while(cloned.size() > 0) {
            Requests_P2MP.Req_P2MP p2mp = cloned.fetchReq();
            req_arrivals.put(p2mp.id, p2mp.arrival);
            req_demands.put(p2mp.id, p2mp.volume);
            req_demands_org.put(p2mp.id, p2mp.volume);
            total_demand += p2mp.volume;
        }
        worst_finish = 0;
        avg_finish = 0;
        median_finish = 0;
        req_finishes.clear();
    }
    
    public static void init()
    {
        // init
        total_bw = 0.0;
        req_count = 0;
        total_demand = 0;
        req_arrivals.clear();
        req_demands.clear();
        req_demands_org.clear();
    }
    
    //........................................................................
    
    public static void do_simulation_dccast(Requests_P2MP reqs)
    {        
        init();
        update_env(reqs);
                
        System.out.println("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println("Total number of requests: " + req_count);
        System.out.println("Total demand: " + total_demand);
        System.out.println("Total links: " + topo.size());
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                
        // the network
        NetworkP2MPWeighted net = new NetworkP2MPWeighted(); 
        net.compile(topo, Nodes); 

        tik();
        int loc_time = 0;
        while(reqs.size() > 0){           
            Requests_P2MP.Req_P2MP r = reqs.fetchReq();

            if(r == null)
                break;

            while(Math.ceil(r.arrival) > loc_time){
                loc_time++;
                net.walk(req_demands);
            }

            total_bw += net.allocate(r, req_finishes);
        }

        while(net.walk(req_demands)){
            loc_time++;
        }
        
        long runtime = toc();
        
        // verify correctness
        req_demands.keySet().stream().forEach((id) -> {
            if(req_demands.get(id) > 1E-10){
                System.out.println(id + " -> " + req_demands_org.get(id) + 
                        " -> " + 
                        req_demands.get(id) +
                        ", " +
                        req_arrivals.get(id));
            }
            
            assert((-1E-10 < req_demands.get(id)) && (req_demands.get(id) < 1E-10));
        });
        
        System.out.printf("\n --------- DCCAST OVERALL OUTPUT ----------- \n");
        System.out.printf("Total Bandwidth: %.2f units (unit = timeslot duration * link capacity)\n", total_bw);
        
        ArrayList<Double> median = new ArrayList<>();
        
        for(Entry<Long, Double> e : req_finishes.entrySet()){
            avg_finish += e.getValue();
            median.add(e.getValue());
        }
        
        avg_finish /= req_finishes.size();
        
        Collections.sort(median);
        median_finish = median.get(median.size() / 2); // 50th percentile
        worst_finish = median.get((int)(median.size() * 0.99)); // 99th percentile
        
        System.out.printf("Runtime: %d ms\n", runtime);
        System.out.printf("Median Finish: %.2f slots\n", median_finish);
        System.out.printf("Tail Finish: %.2f slots\n", worst_finish);
        System.out.printf("Avg Finish: %.2f slots\n", avg_finish);
                
        System.out.println("----------- Everything went well! ------------\n"); 
    }
}