package graph;

public class Journey {
	public int length;
	public City from, to;
	public Road road;
	public Journey next;
	public Journey(Road road, City from, City to) {
		this.road = road;
		this.length = road.length;
		this.from = from;
		this.to = to;
		this.next = null;
	}
	
	public Journey(Road road, City from, City to, Journey next) {
		this.road = road;
		this.length = road.length + next.length;
		this.from = from;
		this.to = to;
		this.next = next;
	}
}
