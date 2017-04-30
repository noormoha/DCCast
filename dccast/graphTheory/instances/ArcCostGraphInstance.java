package graphTheory.instances;

import graphTheory.algorithms.Algorithm;
import graphTheory.graph.Arc;
import graphTheory.graph.Graph;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * An instance is, in the algorithm and complexity theory, an input of a
 * problem, and more specifically in this project, a optimization problem.
 * 
 * This class contains more specific instances: ones defined over a graph where
 * the arcs are associated with a weight (or a cost).
 * 
 * Considering a specific instance of a specific problem, an {@link Algorithm}
 * is able to build the output of that problem, with this instance as input. A
 * optimization problem is a problem where various possible output exists
 * (called the feasible solutions), and we want to find one with minimum or
 * maximum cost.
 * 
 * As it is generally easy to determine if there is at least one feasible
 * solution or not, the instance contains a method {@link #hasSolution()} to do
 * so. If it answers false, the algorithm won't run over that instance.
 * 
 * @author Watel Dimitri
 * 
 */
public class ArcCostGraphInstance extends GraphInstance implements Cloneable {

	private static final Integer DEFAULT_COST = 1;

	public ArcCostGraphInstance(Graph g) {
		super(g);
	}

	protected HashMap<Arc, Integer> costs;
	
	/**
	 * @param a
	 * @return the cost associated with the arc a in this instance. If no cost
	 *         is associated, then, return the default cost: 1.
	 */
	public Integer getCost(Arc a) {
		return getCost(a, false);
	}

	/**
	 * @param a
	 * @param nullCosts
	 *            defines what the method returns when there is no cost
	 *            associated with a: the default cost 1 if false, and null if
	 *            true.
	 * @return the cost associated with the arc a in this instance. If no cost
	 *         is associated, then, return the default cost 1 if nullCosts is
	 *         false, and null if it is true.
	 */
	public Integer getCost(Arc a, boolean nullCosts) {
		if (costs == null)
			costs = new HashMap<Arc, Integer>();
		Integer cost = costs.get(a);

		if (nullCosts)
			return cost;

		if (cost != null)
			return cost;
		else
			return DEFAULT_COST;
	}
	
	/**
	 * @param n1
	 * @param n2
	 * @param nullCosts
	 *            defines what the method returns when there is no cost
	 *            associated with (n1,n2): the default cost 1 if false, and null if
	 *            true.
	 * @return the cost associated with the arc (n1,n2) in this instance. If no such arc exists in the graph
	 * 		   , returns null. If no cost is associated, then, return the default cost 1 if nullCosts is
	 *         false, and null if it is true.
	 */
	public Integer getCost(Integer n1, Integer n2, boolean nullCosts){
		Arc a = this.getGraph().getLink(n1, n2);
		if (a != null)
			return getCost(a, nullCosts);
		else
			return null;
	}

	/**
	 * Set the cost of the arc a to cost.
	 * 
	 * @param a
	 * @param cost
	 */
	public void setCost(Arc a, Integer cost) {
		if (costs == null)
			costs = new HashMap<Arc, Integer>();
		costs.put(a, cost);
	}

	/**
	 * Set the cost of the arc (n1,n2) to cost. If (n1,n2) does not belong to
	 * the graph associated with this instance, do nothing.
	 * 
	 * @param n1
	 * @param n2
	 * @param cost
	 */
	public void setCost(Integer n1, Integer n2, Integer cost) {
		Arc a = this.getGraph().getLink(n1, n2);
		if (a != null)
			setCost(a, cost);
	}

	/**
	 * @return all the costs associated with the arc of this instance. If some
	 *         arcs are not associated with a cost, return for them the default
	 *         cost 1.
	 */
	public HashMap<Arc, Integer> getCosts() {
		return getCosts(false);
	}

	/**
	 * 
	 * @param nullCosts
	 * @return all the costs associated with the arc of this instance. If some
	 *         arcs are not associated with a cost, return for them the default
	 *         cost 1 if nullCosts is false and null if it is true.
	 */
	public HashMap<Arc, Integer> getCosts(boolean nullCosts) {
		HashMap<Arc, Integer> costsCopy = new HashMap<Arc, Integer>();
		Iterator<Arc> it = graph.getEdgesIterator();
		while (it.hasNext()) {
			Arc a = it.next();
			costsCopy.put(a, this.getCost(a, nullCosts));
		}
		return costsCopy;
	}

	/**
	 * Reset the cost of all the arcs, and associate the cost of each arc to the
	 * one defined in the map costs.
	 * 
	 * @param costs
	 */
	public void setCosts(HashMap<Arc, Integer> costs) {
		this.costs = costs;
	}
}
