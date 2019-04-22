package graph;

import java.util.ArrayList;

public class Route {
	public int length = 0;
	public ArrayList<City> path = new ArrayList<City>();
	
	public Route() {
		
	}
	
	public Route add(City c) {
		Route r = new Route();
		r.path.add(c);
		if (path.size() == 0) {
			return r;
		}
		
		for (City x : path) {
			r.path.add(x);
		}
		
		r.length = length + Top.paths[c.n][path.get(0).n].length;
		return r;
	}

}
