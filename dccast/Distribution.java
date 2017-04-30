package dccast;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Mohammad Noormohammadpour
 */

public class Distribution {
    
    private final Random r = new Random();
    
    private class rrq {
        double start;
        double vol;
        
        public rrq(double s, double v) {
            start = s; 
            vol = v;
        } 
    }
    
    public Distribution()
    {
        r.setSeed(System.currentTimeMillis() % 1000000);
    }
    
    double uniformGen(double a, double b)
    {
        return (a + (b-a) * r.nextDouble());
    }
    
    double expGen(double mu)
    {
        double rnd = (r.nextDouble() + 0.0000001) / 1.0000001;
        return (-Math.log(rnd) / mu);
    }
    
    double[][] genPoisson(double lambda, 
            double mu,
            double t_end, 
            double demand_min,
            double demand_max,
            double kappa,
            boolean job_size_dist)
    {
        assert(demand_min >= 0 && demand_max <= 1.0);
        
        // buffer
        ArrayList<rrq> reqs = new ArrayList<>();
        
        // Generate Stuff here
        double t_fraction = 0;
        double D;

        while(true)
        {
            t_fraction += expGen(lambda);
            
            if(Math.floor(t_fraction) >= t_end)
                break;
            
            double t_s = Math.ceil(t_fraction * 1000000) / 1000000.0; 

            if(job_size_dist) {
                D = Math.round(uniformGen(demand_min, demand_max) * 300.0) / 10.0;
            } else {
                D = Math.ceil((1 + expGen(kappa)) * 100.0) / 10.0;
            }
            
            if(reqs.isEmpty())
            {            
                reqs.add(new rrq(t_s, D));
            }
            else
            {
                if(reqs.get(reqs.size()-1).start < t_s)
                {
                    reqs.add(new rrq(t_s, D));
                }
            }
        }

        // out source the stuff
        double[][] req = new double[reqs.size()][2];
        for(int i = 0; i < reqs.size(); i++)
        {
            rrq my_req = reqs.get(i);
            
            req[i][0] = my_req.start;
            req[i][1] = my_req.vol;
        }
        
        return req;
    }
}
