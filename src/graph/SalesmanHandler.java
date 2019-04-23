package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
  public static boolean newGen = false;
  private ThreadLocalRandom random = ThreadLocalRandom.current();
  public HashMap<City, LinkedHashMap<Road, Double>> nodeWeights = new HashMap<>();

  private Comparator<Salesman> comparatorForWeight = Comparator.comparing(Salesman::getWeight);

  private Comparator<Salesman> comparatorForLeft = (Salesman a, Salesman b) -> {
    int asize = a.needToVisit.size();
    int bsize = b.needToVisit.size();
    if (bsize == asize) {
      if (a.dist > b.dist) {
        return 1;
      } else if (a.dist < b.dist) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return (asize > bsize) ? 1 : -1;
    }
  };

  public static City base;

  public SalesmanHandler(int size, City startingNode) {
    base = startingNode;
    this.size = size;
    levelProbabilitiesALL();
    calculateNext(startingNode, null);
  }

  public void generations() {
    long time = 0;
    long startTime = System.nanoTime();
    int generation = 0;
    int endGenSize = City.cities.size() / 2;
    for (; ; ) {
      mutationRate += 0.00025;
      newGen = true;
      for (int i = 0; ; i++) {
        if (i == endGenSize || moveAndFitness(i)) {
          break;
        }
      }
      generation++;
      newGen = false;
      Collections.sort(this, comparatorForLeft);
      crossoverAndMutate();
      time += (System.nanoTime() - startTime);
      if (animate) {
        if (delay) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
          }
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
    mutationRate = 0;
  }

  private void crossoverAndMutate() {

    int resetSpot;
    Salesman s;
    for (int i = 0; i < this.size() / 4; i += 4) {
      this.set(this.size() / 4 + i + 0, new Salesman(this.get(i), this.get(i + 1)));
      this.set(this.size() / 4 + i + 1, new Salesman(this.get(i + 1), this.get(i + 2)));
      this.set(this.size() / 4 + i + 2, new Salesman(this.get(i + 2), this.get(i + 3)));
      this.set(this.size() / 4 + i + 3, new Salesman(this.get(i + 3), this.get(i + 4)));
    }

    int tempsize = this.size();
    for (int i = 0; i < tempsize; i++) {
      s = this.get(i);
      if (s.pre.equals(base) && s.post == null) {
        calculateNext(s.pre, s.currentRoad);
      }
    }

    for (int i = 0; i < this.size(); i++) {
      s = this.get(i);
      if (s.path.size() != 0 && random.nextDouble() < mutationRate) {
        boolean good = isContinuous(s.path, base);
        resetSpot = random.nextInt(0, s.path.size());
        s.currentRoad = s.path.get(resetSpot);
        System.out.println("Before: " + s.path);
        s.path.subList(resetSpot, s.path.size()).clear();
        System.out.println("After: " + s.path);
        s.roundScore.subList(resetSpot, s.roundScore.size()).clear();
        s.fixSalesman();
        System.out.println(good == isContinuous(s.path, base));
        delay(5000);
      }
    }
  }

  public void delay(int t) {
    try {
      Thread.sleep(t);
    } catch (InterruptedException e) {
    }
  }

  public boolean isContinuous(List<Road> s, City start) {
    City next = start;
    for (Road i : s) {
      if (i.start.equals(next)) {
        next = i.end;
      } else if (i.end.equals(next)) {
        next = i.start;
      } else {
        return false;
      }
    }
    return true;
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
    LinkedHashMap<Road, Double> weights = new LinkedHashMap<Road, Double>(nodeWeights.get(node));

    if (roadToIgnore != null && weights.containsKey(roadToIgnore)) {
      Double needToAdd = weights.remove(roadToIgnore) / weights.size();
      weights.forEach((k, v) -> {
        weights.put(k, v + needToAdd);
      });
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
    double extraWeight = 0d;
    double tempWeight = 0d;
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
    List<Map.Entry<Road, Double>> entries = new ArrayList<Map.Entry<Road, Double>>(
        weights.entrySet());
    Collections.sort(entries, Comparator.comparing(Map.Entry::getValue));
    weights = new LinkedHashMap<>();
    for (Map.Entry<Road, Double> entry : entries) {
      weights.put(entry.getKey(), entry.getValue() / totalWeight);
    }
    if (weights.size() != 0) {
      nodeWeights.put(node, weights);
    }
  }

  private boolean moveAndFitness(int iteration) {
    Collections.sort(this, comparatorForWeight);
    double successWeight = this.get(0).weight;
    Map.Entry<City, Road> cityRoad = null;
    this.forEach((Salesman s) -> s.started = false);
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
