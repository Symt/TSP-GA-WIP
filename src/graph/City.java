package graph;

import java.util.ArrayList;
import java.util.Iterator;

public class City {
	public String name;
	public Point p;
	public ArrayList<Road> roads = new ArrayList<Road>();
	public static ArrayList<City> cities = new ArrayList<City>();
	public int n;
	public static int cityNum = 0;

	public City(String name, int x, int y) {
		this.name = name;
		this.p = new Point(x, y);
		cities.add(this);
		this.n = cityNum;
		cityNum++;
	}

	@Override
	public String toString() {
		return name;
	}

	public void addRoad(Road r) {
		roads.add(r);
	}

	@Override
	public boolean equals(Object o) {
		return ((City) o).name.equals(name);
	}

	public static City find(String name) {
		for (City c : cities)
			if (c.name.equals(name))
				return c;
		System.out.print("City " + name + " missing!");
		return cities.get(0);
	}

}
