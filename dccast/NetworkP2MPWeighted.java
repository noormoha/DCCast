package dccast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.StringJoiner;

/**
 *
 * @author Mohammad Noormohammadpour
 */

// the following three imports are provided by Dimitri Watel: https://github.com/mouton5000/DSTAlgoEvaluation
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.GFLACAlgorithm;
import graphTheory.graph.DirectedGraph;
import graphTheory.instances.steiner.classic.SteinerDirectedInstance;
import java.util.ArrayList;
import java.util.LinkedList;
//////////////////////////////////////////////////////////////////////////

public class NetworkP2MPWeighted {
    
    // subclasses below Network
    private class timeslot {
        private final LinkedHashMap<Long, Double> content 
                = new LinkedHashMap<>();
        private double sum = 0.0;
        
        public double getSpace() {
            assert(sum >= 0 && sum <= 1);
            return Math.max(0, (1.0 - sum));
        }
        
        public double getSum() {
            assert(sum >= 0 && sum <= 1);
            return Math.min(1.0, sum); 
        }
        
        public boolean addTraffic(double vol, long reqid) {
            assert(sum >= 0);
            
            if(vol <= getSpace()) 
            {
                sum = Math.min(1.0, sum + vol);
                
                if(content.containsKey(reqid)) 
                {
                    content.put(reqid, vol + content.get(reqid));
                } 
                else 
                {
                    content.put(reqid, vol);
                }

                assert(sum >= 0 && sum <= 1);
                return true;
            }
            
            assert(sum >= 0);
            return false;
        }
        
        public double getTraffic(long reqid, boolean pull) {
            assert(sum >= 0);
            
            if(content.containsKey(reqid))
            {
                double vol = content.get(reqid);
                
                if(pull)
                {
                    sum = Math.max(0, sum - vol);
                    content.remove(reqid);
                }
                
                assert(sum >= 0);
                return vol;
            }
            else 
            {
                assert(sum >= 0);
                return 0;
            }
        }
        
        public double getTrafficVol(long reqid, double reqvol) {
            assert(sum >= 0);
            
            if(content.containsKey(reqid))
            {
                double vol = content.get(reqid);
                
                if(reqvol >= vol) 
                {
                    sum = Math.max(0, sum - vol);
                    content.remove(reqid);
                    
                    assert(sum >= 0);
                    return vol;
                }
                else 
                {
                    sum = Math.max(0, sum - reqvol);
                    content.put(reqid, vol - reqvol);
                    
                    assert(sum >= 0);
                    return reqvol;
                }
            }
            else
            {
                assert(sum >= 0);
                return 0;
            }
        }
        
        public Long[] getRequests() {
            assert(sum >= 0);
            return content.keySet().toArray(new Long[content.size()]);
        }
    }
    
    private class jNode {
        LinkedHashMap<jNode, jEdge> next = new LinkedHashMap<>();
        int index = 0;
    }
    
    private class jEdge {
        int id;
        int src, dst;
        ArrayList<timeslot> t_slot;
        double load; // Le on the paper
    }
    
    private class jSTEINER_tree {
        LinkedList<jEdge> edges = new LinkedList<>();
    }
    
    // our networkwide variables 
    private final LinkedHashMap<Long, LinkedList<Integer>> reqedges = 
            new LinkedHashMap<>();
    
    private final HashMap<Integer, HashMap<Integer, jEdge>> node_to_edge = 
            new HashMap<>();
    
    private final HashMap<Integer, HashMap<Long, Double>> schedule = 
            new HashMap<>();
    
    private final HashMap<Long, Requests_P2MP.Req_P2MP> requests = 
            new HashMap<>();
    
    private int NODES;
    private int[][] LINKS; 
    private int t_time; // now
    private int hor_end = 0;
    private jNode[] nodes;
    private jEdge[] edges;
    private double[] total_link_traffic;
            
    // compile network topology
    public void compile(Topology links, int Nodes)
    {
        NODES = Nodes; 
        LINKS = links.getLinks();
        t_time = 0; // now 
        total_link_traffic = new double[LINKS.length];
        
        // build the whole network based off the links 
        nodes = new jNode[NODES];
        for(int i = 0; i < NODES; i++) {
            nodes[i] = new jNode();
            nodes[i].index = i;
        }
        
        edges = new jEdge[LINKS.length];
        for(int i = 0; i < LINKS.length; i++) {
            edges[i] = new jEdge();
            edges[i].id = i;
            edges[i].src = LINKS[i][0];
            edges[i].dst = LINKS[i][1];
            edges[i].load = 0;
            edges[i].t_slot = new ArrayList<>();
            
            nodes[LINKS[i][0]].next.put(nodes[LINKS[i][1]], edges[i]);
            
            if(!node_to_edge.containsKey(edges[i].src)) {
                node_to_edge.put(edges[i].src, new HashMap<>());
            }
            node_to_edge.get(edges[i].src).put(edges[i].dst, edges[i]);
        }
    }
    
    public double allocate(Requests_P2MP.Req_P2MP r, TreeMap<Long, Double> req_finishes)
    {
        return routeTraffic(r, req_finishes);
    }
    
    private double routeTraffic(Requests_P2MP.Req_P2MP r, TreeMap<Long, Double> req_finishes)
    {
        requests.put(r.id, r);
        
        DirectedGraph dg = new DirectedGraph();

        for(int i = 0; i < NODES; i++)
                dg.addVertice(i);

        // Add arcs
        for(int[] link : LINKS){
            dg.addDirectedEdges(link[0], link[1]);
        }               
        
        SteinerDirectedInstance sdi = new SteinerDirectedInstance(dg);
        sdi.setRoot(r.src); // Set the node 1 as the root of the instance
        r.dst.stream().forEach(dst -> {sdi.setRequired(dst);});
        
        HashMap<jEdge, Integer> hm_costs = new HashMap<>();
        for(jEdge e : edges){
            hm_costs.put(e, (int) (e.load + r.volume)); // We = Le + Vr
            sdi.setCost(e.src, e.dst, hm_costs.get(e));
        }
        
        // Create an algorithm to solve or approximate the instance
        GFLACAlgorithm alg = new GFLACAlgorithm(); // This algorithm was developed by Dimitri Watel and Marc-Antoine Weisser: http://dl.acm.org/citation.cfm?id=3013458
        alg.setInstance(sdi);
        alg.compute();

        // System.out.println("Src: " + r.src + ", Dst: " + r.dst.toString());
        // System.out.println("Returned solution : " + alg.getArborescence());
        // System.out.println("Cost " + r.id + " : " + alg.getCost());
        // System.out.println("Runtime: " + alg.getTime() + " ms");
        
        // validate the tree
        jSTEINER_tree selectedTree = new jSTEINER_tree();
        selectedTree.edges.clear();
        HashSet<Integer> set = new HashSet<>();
        HashSet<Integer> set2 = new HashSet<>();
        set.add(r.src);
        HashSet<Integer> dsts = new HashSet<>(r.dst);
        while(!set.isEmpty()){
            set2.clear();
            alg.getArborescence().stream().forEach(arc -> {
                if(set.contains(arc.getInput())){
                    selectedTree.edges.add(node_to_edge.
                            get(arc.getInput()).
                            get(arc.getOutput()));
                    set2.add(arc.getOutput());
                    if(dsts.contains(arc.getOutput())){
                        dsts.remove(arc.getOutput());
                    }
                }
            });
            set.clear();
            set.addAll(set2);
        }
        
        assert(dsts.isEmpty()); // all destinations are reachable from source
               
        int what = PathAllocate(selectedTree, r.volume, r.id);
        req_finishes.put(r.id, what - r.arrival + 1);
        double total_bw = selectedTree.edges.size() * r.volume;
                
        final LinkedList<Integer> lst = new LinkedList<>();
        selectedTree.edges.stream().forEach(e -> {
            lst.add(e.id);
        });

        reqedges.put(r.id, lst);

        StringJoiner sj = new StringJoiner(",");
        selectedTree.edges.stream().forEach((e) -> {
            sj.add(String.format("%d->%d", e.src, e.dst));
        });

        System.out.printf("Job id: [" + r.id + "] -> TREE: [%s], \n\tsrc: %d, dst: %s, demand: %.2f, t_time: %d\n",
                    sj.toString(), r.src, r.dst.toString(), r.volume, t_time);

        return total_bw;
    }
    
    // do as late as possible on edges
    private int PathAllocate(jSTEINER_tree p, double vol, long id)
    {
        int t = 0;
        
        while(vol > 0){
            double space = vol;
            
            if(t >= hor_end - t_time){
                if(hor_end < t_time){
                    hor_end = t_time;
                }
                for(jEdge e : edges){
                    e.t_slot.add(new timeslot());
                }
                hor_end++;
            }
            
            for(jEdge ex : p.edges){
                space = Math.min(ex.t_slot.get(t).getSpace(), space);
            }
            
            if(space > 0) {
                vol -= space;
                
                if(!schedule.containsKey(t + t_time)){
                    schedule.put(t + t_time, new HashMap<>());
                }
                schedule.get(t + t_time).put(id, space);
                
                for(jEdge ex : p.edges){
                    assert(ex.t_slot.get(t).addTraffic(space, id));
                    ex.load += space;
                }
            }
            
            t++;
        }
        
        return t + t_time;
    }
    
    // move one timeslot ahead
    public boolean walk(TreeMap<Long, Double> req_demands)
    {
        print_schedule();
        
        if(hor_end <= t_time){
            t_time++;
            return false;
        }
        
        // update the load field for all edges
        for (jEdge edge : edges)
        {
            double now = edge.t_slot.get(0).getSum();
            
            assert(now <= 1.0 + 1E-9);
            edge.load = Math.max(0.0, edge.load - now);
            total_link_traffic[edge.id] += now; 
            
            // test the allocation for validity & integrity
            for(long id:edge.t_slot.get(0).getRequests())
            {
                assert(req_demands.containsKey(id));
                
                req_demands.put(id, (req_demands.get(id) - 
                        edge.t_slot.get(0).getTraffic(id, false) / 
                                reqedges.get(id).size()));
            }
        }
        
        t_time++;
        for(jEdge e : edges){
            e.t_slot.remove(0);
        }
        return t_time < hor_end;
    }
    
    // print the transmission schedule for next slot
    public void print_schedule(){
        System.out.println("Schedule for time " + t_time + ":");
        if(schedule.containsKey(t_time)){
            for(Long id : schedule.get(t_time).keySet()){
                System.out.printf("- SOURCE: %d, JobID: %d, RATE: %.3f\n", 
                        requests.get(id).src,
                        id,
                        schedule.get(t_time).get(id));
            }
        }
        System.out.println();
    }
}
