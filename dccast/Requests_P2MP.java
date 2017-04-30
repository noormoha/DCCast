package dccast;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author Mohammad Noormohammadpour
 */

public class Requests_P2MP {
    
    public static class Req_P2MP {
        public int is_periodic;
        public long id;
        public int src;
        public List<Integer> dst = new ArrayList<>(); // multiple destinations for a request 
        public double volume;
        public double arrival;
        public int is_processed;
        
        public Req_P2MP(Requests_P2MP.Req_P2MP r)
        {
            this.id = r.id;
            this.src = r.src;
            this.dst.addAll(r.dst);
            this.volume = r.volume;
            this.arrival = r.arrival;
            this.is_periodic = r.is_periodic;
            this.is_processed = r.is_processed;
        }
        
        public Req_P2MP(long id, int src, List<Integer> dst, double volume, 
            double arrival, int is_periodic, int is_processed)
        {
            this.id = id;
            this.src = src;
            this.dst.addAll(dst);
            this.volume = volume;
            this.arrival = arrival;
            this.is_periodic = is_periodic;
            this.is_processed = is_processed;
        }
    }
    
    private final PriorityQueue<Req_P2MP> queue = new PriorityQueue<>((r1,r2) -> {
        return Double.valueOf(r1.arrival).compareTo(r2.arrival);
    });
    
    public Requests_P2MP() {}
    
    public Requests_P2MP(PriorityQueue<Req_P2MP> q)
    {
        queue.addAll(q);
    }
    
    @Override
    public Requests_P2MP clone()
    {
        return new Requests_P2MP(queue);
    }
    
    public int size()
    {
        return queue.size();
    }
    
    public void add(long id, int src, List<Integer> dst, double volume, 
            double arrival, int is_periodic)
    {
        assert(volume >= 0);

        queue.add(new Req_P2MP(id, src, dst, volume, 
            arrival, is_periodic, 0));
    }
    
    public Requests_P2MP.Req_P2MP fetchReq()
    {
        if(queue.isEmpty())
            return null;
        
        return queue.remove();
    }
    
    public Requests_P2MP.Req_P2MP peekReq()
    {
        if(queue.isEmpty())
            return null;
        
        return queue.peek();
    }
}
