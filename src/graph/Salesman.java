package graph;

import java.util.ArrayList;
import java.util.List;

public class Salesman {

  double dist = 0;
  double weight;
  double score;
  City pre;
  City post;
  List<City> needToVisit = new ArrayList<>(Top.positiveCities);
  List<Road> path = new ArrayList<>();
  Road currentRoad;
  int iteration = 0;
  int startSize = 0;
  static int largestPathSize = 0;


  Salesman(Road currentRoad, double weight) {
    this.currentRoad = currentRoad;
    pre = currentRoad.start;
    post = currentRoad.end;
    this.weight = weight;
  }

  Salesman(Salesman s) {
    dist = s.dist;
    weight = s.weight;
    score = s.score;
    pre = s.pre;
    post = s.post;
    needToVisit = new ArrayList<>(s.needToVisit);
    path = new ArrayList<>(s.path);
    currentRoad = s.currentRoad;
    iteration = s.iteration;
    startSize = s.startSize;
  }

  Salesman(Salesman parent1, Salesman parent2) {
    score = 0;
    int smallSize = Math.min(parent1.path.size(), parent2.path.size());
    for (int i = 0; i < smallSize; i++) {
      if (parent1.path.get(i).equals(parent2.path.get(i))) {
        path.add(parent1.path.get(i));
      } else {
        currentRoad = (Math.random() < .5) ? parent1.path.get(i) : parent2.path.get(i);
        break;
      }
    }
    if (path.size() == 0) {
      pre = SalesmanHandler.base;
    } else if (path.size() == 1) {
      pre = path.get(0).convert(SalesmanHandler.base);
    } else {
      pre = path.get(path.size() - 1).start.equals(path.get(path.size() - 2).end) || path
          .get(path.size() - 1).start.equals(path.get(path.size() - 2).start) ? path
          .get(path.size() - 1).end : path.get(path.size() - 1).start;
    }
  }

  void resetForGeneration() {
    needToVisit = new ArrayList<>(Top.positiveCities);
    if (path.size() == 0 && currentRoad != null) {
      pre = SalesmanHandler.base;
      post = currentRoad.convert(SalesmanHandler.base);
      weight = currentRoad.speed;
      startSize = 0;
    } else {
      currentRoad = path.get(0);
      dist = 0;
      score = 0;
      weight = path.get(0).speed;
      pre = SalesmanHandler.base;
      post = (path.get(0).start.equals(SalesmanHandler.base)) ? path.get(0).end : path.get(0).start;
      startSize = path.size();
    }
  }

  void subtractWeight(double successWeight) {
    weight -= successWeight;
    dist += successWeight;
    if (weight == 0) {
      if (iteration >= startSize) {
        path.add(currentRoad);
        if (path.size() > largestPathSize) {
          largestPathSize = path.size();
        }
      }
      if (needToVisit.contains(post)) {
        if (needToVisit.size() != 1 && post.equals(SalesmanHandler.base)) {
          score -= 5;
        } else {
          needToVisit.remove(post);
          score += 25;
        }
      } else {
        score -= 5;
      }
      pre = post;
      if (iteration < startSize) {
        post = (path.get(iteration).convert(pre));
        currentRoad = path.get(iteration);
        weight = currentRoad.speed;
      } else {
        post = null;
      }
      iteration++;
    } else {
      score += 0.01d / weight;
    }
  }

  void boldPath() {
    for (Road r : path) {
      r.bold = true;
    }
  }

  double getWeight() {
    return weight;
  }

  double totalSpeed() {
    double total = 0;
    for (Road r : path) {
      total += r.speed;
    }
    return total;
  }
  double totalDist() {
    double total = 0;
    for (Road r : path) {
      total += r.length;
    }
    return total;
  }

  @Override
  public String toString() {
    return String
        .format("{dist=%f, left=%d, path_size=%d}", dist, needToVisit.size(), path.size());
  }
}
