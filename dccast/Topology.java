package dccast;

import java.util.LinkedList;

/**
 *
 * @author Mohammad Noormohammadpour
 */

public class Topology {
    private int nodes = 0;
    private final LinkedList<Integer> srcs = new LinkedList<>();
    private final LinkedList<Integer> dsts = new LinkedList<>();
    
    public Topology(int n)
    {
        nodes = n;
    }
    
    public void clean()
    {
        dsts.clear();
        srcs.clear();
    }
    
    public int size()
    {
        return srcs.size();
    }
    
    public void add(int src, int dst)
    {
        if(src >= nodes || dst >= nodes)
            return;
        
        dsts.add(dst);
        srcs.add(src);
    }
    
    public int[][] getLinks()
    {
        if(dsts.isEmpty())
            return null;
        
        int[][] links = new int[dsts.size()][2];
        
        for(int i = 0; i < dsts.size(); i++)
        {
            links[i][0] = srcs.get(i);
            links[i][1] = dsts.get(i);
        }
        
        return links;
    }
}
