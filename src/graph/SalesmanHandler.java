package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SalesmanHandler extends ArrayList<Salesman> {
	private static final long serialVersionUID = 1L;
	static final boolean animate = false;
	static final boolean delay = true;
	int size;
	public static double mutationRate = 0;
	private LinkedHashMap<Road, Double> weights;
	
	private Comparator<Salesman> comparatorForWeight = (Salesman a, Salesman b) -> {
		return (a.weight > b.weight) ? 1 : (a.weight == b.weight) ? 0 : -1;
	};
	private Comparator<Salesman> comparatorForLeft = (Salesman a, Salesman b) -> {
		int asize = a.needToVisit.size();
		int bsize = b.needToVisit.size();
		if (bsize == asize) {
			if (a.path.size() > b.path.size()) {
				return 1;
			} else if (a.path.size() < b.path.size()) {
				return -1;
			} else {
				return 0;
			}
		} else {
			return (asize > bsize) ? 1 : -1;
		}
	};
	@SuppressWarnings("unused")
	private Comparator<Salesman> finalComparator = (Salesman a, Salesman b) -> {
		int asize = a.needToVisit.size();
		int bsize = b.needToVisit.size();
		if (asize == 0 && bsize == 0) {
			if (a.path.size() > b.path.size()) {
				return 1;
			} else if (b.path.size() > a.path.size()) {
				return -1;
			} else {
				if (a.score > b.score) {
					return -1;
				} else if (b.score > a.score) {
					return 1;
				} else {
					return 0;
				}
			}
		} else if (asize == 0) {
			return -1;
		} else if (bsize == 0) {
			return 1;
		} else {
			return 2;
		}
	};

	public static City base;

	public SalesmanHandler(int size, City startingNode) {
		base = startingNode;
		this.size = size;
		levelProbabilities(startingNode, null);
	}

	public void generations() {
		long time = 0;
		long startTime = System.nanoTime();
		int generation = 0;
		int endGenSize = City.cities.size()/2;
		for (;;) {
			mutationRate += 0.00025;
			for (int i = 0;; i++) {
				if (i == endGenSize || moveAndFitness()) {
					break;
				}
			}
			generation++;
			Collections.sort(this, comparatorForLeft);
			crossoverAndMutate();
			time += (System.nanoTime() - startTime);
			if (animate) {
				if (delay) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
				this.get(0).boldPath();
				Top.self.repaint();
			}
			startTime = System.nanoTime();
			
			if (generation == 1000) {
				break;
			}
		}
		System.out.println(generation + " generations: " + time / 1000000d + "ms");
		Collections.sort(this, comparatorForLeft);
		this.get(0).boldPath();
		Top.self.repaint();
	}

	private void crossoverAndMutate() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int resetSpot;
		Salesman s;
		
		for (int i = 0; i < this.size() / 4; i+=4) {
			this.set(this.size() / 4 + i + 0, new Salesman(this.get(i), this.get(i + 1)));
			this.set(this.size() / 4 + i + 1, new Salesman(this.get(i + 1), this.get(i + 2)));
			this.set(this.size() / 4 + i + 2, new Salesman(this.get(i + 2), this.get(i + 3)));
			this.set(this.size() / 4 + i + 3, new Salesman(this.get(i + 3), this.get(i + 4)));
		}

		int tempsize = this.size();
		for (int i = 0; i < tempsize; i++) {
			s = this.get(i);
			if (s.currentRoad == null || s.post == null) {
				levelProbabilities(s.pre, s.currentRoad);
			}
		}

		for (int i = 0; i < this.size(); i++) {
			s = this.get(i);
			if (s.path.size() != 0 && random.nextDouble() < mutationRate) {
				resetSpot = random.nextInt(0, s.path.size());
				s.path.subList(resetSpot, s.path.size()).clear();
				s.roundScore.subList(resetSpot, s.roundScore.size()).clear();
				s.fixSalesman();
			}
		}
	}

	private Salesman weighted() {
		double weight = 0;
		double random = Math.random();
		for (Map.Entry<Road, Double> entry : weights.entrySet()) {
			weight += entry.getValue();
			if (random < weight) {
				return new Salesman(entry.getKey(), entry.getKey().speed);
			}
		}
		return null;
	}

	private void calculateNext(City node) {
		Road r;
		double random;
		double weight = 0;
		ArrayList<Salesman> updated = new ArrayList<>();
		ArrayList<Salesman> iteration = new ArrayList<>(this);
		if (this.size() == 0) {
			for (int i = iteration.size(); i < size; i++) {
				iteration.add(weighted());
				this.add(weighted());
			}
		}
		for (Salesman s : iteration) {
			if (s.post == null) {
				this.remove(s);
				random = Math.random();
				for (Map.Entry<Road, Double> entry : weights.entrySet()) {
					weight += entry.getValue();
					if (random < weight) {
						s.pre = node;
						r = entry.getKey();
						s.post = (node.equals(r.end)) ? r.start : r.end;
						s.currentRoad = r;
						s.weight = r.speed;
						updated.add(s);
						break;
					}
				}
				weight = 0;
			}
		}
		weights = null;
		this.addAll(updated);
	}

	private void levelProbabilities(City node, Road roadToIgnore) {
		Set<Road> roads = new LinkedHashSet<>(node.roads);
		weights = new LinkedHashMap<>();
		double totalWeight = 0d;
		double extraWeight = 0d;
		double tempWeight = 0d;
		for (Road r : roads) {
			if (roadToIgnore == null || !roadToIgnore.equals(r)) {
				weights.put(r, r.speed);
				totalWeight += r.speed;
			}
		}
		if (weights.size() == 1) {
			weights.replace(weights.keySet().iterator().next(), 1d);
			calculateNext(node);
			return;
		}

		tempWeight = totalWeight;
		totalWeight = 0;
		for (Map.Entry<Road, Double> entry : weights.entrySet()) {
			Road r = entry.getKey();
			Double w = entry.getValue();
			extraWeight = 0;
			if (!node.equals(r.end)) {
				if (Top.positiveCities.contains(r.end)) {
					extraWeight = totalWeight / weights.size();
				}
			} else {
				if (Top.positiveCities.contains(r.start)) {
					extraWeight = totalWeight / weights.size();
				}
			}
			weights.replace(r, ((tempWeight - w) + extraWeight));
			totalWeight += ((tempWeight - w) + extraWeight);
		}
		List<Map.Entry<Road, Double>> entries = new ArrayList<Map.Entry<Road, Double>>(weights.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Road, Double>>() {
			public int compare(Map.Entry<Road, Double> a, Map.Entry<Road, Double> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		weights = new LinkedHashMap<Road, Double>();
		for (Map.Entry<Road, Double> entry : entries) {
			weights.put(entry.getKey(), entry.getValue() / totalWeight);
		}
		calculateNext(node);
	}

	private boolean moveAndFitness() {
		Collections.sort(this, comparatorForWeight);
		double successWeight = this.get(0).weight;
		Map.Entry<City, Road> cityRoad = null;
		for (Salesman i : this) {
			i.subtractWeight(successWeight);
			if (i.needToVisit.size() == 0) {
				return true;
			}
			if (i.weight == 0) {
				cityRoad = new CustomEntry<>(i.pre, i.currentRoad);
			}
		}
		if (cityRoad != null) {
			this.levelProbabilities(cityRoad.getKey(), cityRoad.getValue());
		}
		return false;
	}
}
