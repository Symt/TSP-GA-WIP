package graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

class SalesmanHandler extends ArrayList<Salesman> {

  private static final long serialVersionUID = 1L;
  private static final boolean animate = false;
  private static final boolean delay = true;
  static City base;
  private static double mutationRate = 0;
  private int size;
  private ThreadLocalRandom random = ThreadLocalRandom.current();
  private HashMap<City, LinkedHashMap<Road, Double>> nodeWeights = new HashMap<>();
  private Comparator<Salesman> comparatorForWeight = Comparator.comparing(Salesman::getWeight);
  private Comparator<Salesman> comparatorForLeft = (Salesman a, Salesman b) -> {
    int aSize = a.needToVisit.size();
    int bSize = b.needToVisit.size();
    int result;
    if (bSize == aSize) {
      result = Double.compare(a.score, b.score);
      if (result == 0) {
        result = Double.compare(a.dist, b.dist);
        if (result == 0) {
          result = Double.compare(a.path.size(), b.path.size());
        }
      }
    } else {
      result = Integer.compare(aSize, bSize);
    }
    return result;
  };

  SalesmanHandler(int size, City startingNode) {
    base = startingNode;
    this.size = size;
    levelProbabilitiesALL();
    calculateNext(startingNode, null);
  }

  double generations() {
    long time = 0;
    long startTime = System.nanoTime();
    int generation = 0;
    int endGenSize = City.cityNum; // TODO: Fix up this number to be optimal for completion
    do {
      mutationRate += 0.000125;
      for (int i = 0; ; i++) {
        if (i == endGenSize || moveAndFitness(i)) {
          break;
        }
      }
      generation++;
      this.sort(comparatorForLeft);
      crossoverAndMutate();
      time += (System.nanoTime() - startTime);
      if (animate) {
        if (delay) {
          delay();
        }
        this.get(0).boldPath();
        Top.self.repaint();
      }
      startTime = System.nanoTime();
    } while (generation != 1000);
    System.out.println(generation + " generations: " + time / 1000000d + "ms");
    this.sort(comparatorForLeft);
    System.out.println(this);
    this.get(0).boldPath();
    mutationRate = 0;
    return this.get(0).dist;
  }

  private void crossoverAndMutate() {

    int resetSpot;
    Salesman s;

    for (int i = 0; i < this.size() / 4; i += 4) {
      this.set(this.size() / 4 + i, new Salesman(this.get(i), this.get(i + 1)));
      this.set(this.size() / 4 + i + 1, new Salesman(this.get(i + 1), this.get(i + 2)));
      this.set(this.size() / 4 + i + 2, new Salesman(this.get(i + 2), this.get(i + 3)));
      this.set(this.size() / 4 + i + 3, new Salesman(this.get(i + 3), this.get(i + 4)));
    }

    int tempSize = this.size();
    for (int i = 0; i < tempSize; i++) {
      s = this.get(i);
      if (s.pre.equals(base) && s.post == null) {
        calculateNext(s.pre, s.currentRoad);
      }
    }

    for (int i = 0; i < this.size(); i++) {
      s = this.get(i);
      if (s.path.size() != 0 && random.nextDouble() < mutationRate) {
        resetSpot = random.nextInt(s.path.size() / 2, s.path.size());
        s.currentRoad = s.path.get(resetSpot);
        s.path.subList(resetSpot, s.path.size()).clear();
      }
    }
  }

  private void delay() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  private Salesman weighted(LinkedHashMap<Road, Double> weights) {
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

  private void calculateNext(City node, Road roadToIgnore) {
    Road r;
    double random;
    double weight = 0;
    ArrayList<Salesman> updated = new ArrayList<>();
    ArrayList<Salesman> iteration = new ArrayList<>(this);
    LinkedHashMap<Road, Double> weights = new LinkedHashMap<>(nodeWeights.get(node));

    if (roadToIgnore != null && weights.containsKey(roadToIgnore)) {
      Double needToAdd = weights.remove(roadToIgnore) / weights.size();
      weights.forEach((k, v) -> weights.put(k, v + needToAdd));
    }

    if (this.size() == 0) {
      for (int i = iteration.size(); i < size; i++) {
        iteration.add(weighted(weights));
        this.add(weighted(weights));
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
    this.addAll(updated);
  }

  private void levelProbabilitiesALL() {
    for (City i : City.cities) {
      levelProbabilities(i);
    }
  }

  private void levelProbabilities(City node) {
    Set<Road> roads = new LinkedHashSet<>(node.roads);
    LinkedHashMap<Road, Double> weights = new LinkedHashMap<>();
    double totalWeight = 0d;
    double extraWeight;
    double tempWeight;
    for (Road r : roads) {
      weights.put(r, r.speed);
      totalWeight += r.speed;
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
    List<Map.Entry<Road, Double>> entries = new ArrayList<>(
        weights.entrySet());
    entries.sort(Comparator.comparing(Map.Entry::getValue));
    weights = new LinkedHashMap<>();
    for (Map.Entry<Road, Double> entry : entries) {
      weights.put(entry.getKey(), entry.getValue() / totalWeight);
    }
    if (weights.size() != 0) {
      nodeWeights.put(node, weights);
    }
  }

  private boolean moveAndFitness(int iteration) {
    this.sort(comparatorForWeight);
    double successWeight = this.get(0).weight;
    Map.Entry<City, Road> cityRoad = null;
    this.forEach((Salesman s) -> s.resetForGeneration(iteration));
    for (Salesman i : this) {
      i.subtractWeight(successWeight, iteration);
      if (i.needToVisit.size() == 0) {
        return true;
      }
      if (i.weight == 0) {
        cityRoad = new CustomEntry<>(i.pre, i.currentRoad);
      }
    }
    if (cityRoad != null) {
      this.calculateNext(cityRoad.getKey(), cityRoad.getValue());
    }
    return false;
  }
}
