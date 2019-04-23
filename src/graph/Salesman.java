package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Salesman {

  public Road currentRoad;
  public List<Road> path = new ArrayList<>();
  public City pre;
  public City post;
  public double weight;
  public List<City> needToVisit = new ArrayList<>(Top.positiveCities);
  public double score;
  public List<Double> roundScore = new ArrayList<>();
  private static ThreadLocalRandom random = ThreadLocalRandom.current();
  boolean started = false;
  double dist = 0;

  public Salesman(Road currentRoad, double weight) {
    this.currentRoad = currentRoad;
    pre = currentRoad.start;
    post = currentRoad.end;
    this.weight = weight;
  }

  public Salesman(Salesman parent1, Salesman parent2) {
    int smallSize = Math.min(parent1.path.size(), parent2.path.size());
    for (int i = 0; i < smallSize; i++) {
      if (parent1.path.get(i).equals(parent2.path.get(i))) {
        path.add(parent1.path.get(i));
        roundScore.add(parent1.roundScore.get(i));
      } else {
        break;
      }
    }
    if (path.size() == 0) {
      pre = SalesmanHandler.base;
      post = null;
      weight = 0;
      score = 0;
    } else {
      Road cR = path.remove(path.size() - 1);
      Double cS = roundScore.remove(roundScore.size() - 1);
      currentRoad = cR;
      score = cS;
      if (path.size() == 0) {
        if (currentRoad.start.equals(SalesmanHandler.base)) {
          pre = currentRoad.start;
          post = currentRoad.end;
        } else {
          post = currentRoad.start;
          pre = currentRoad.end;
        }
      } else {
        Road previous = path.get(path.size() - 1);
        if (currentRoad.start.equals(previous.start) || currentRoad.start.equals(previous.end)) {
          pre = currentRoad.start;
          post = currentRoad.end;
        } else {
          post = currentRoad.start;
          pre = currentRoad.end;
        }
      }
      fixSalesman();
    }
  }

  public void subtractWeight(double successWeight, int iteration) {
    if (started || path.size() < iteration) {
      weight -= successWeight;
      if (weight == 0) {
        path.add(currentRoad);
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
        post = null;
        roundScore.add(score);
      } else {
        score += 0.01d / weight;
      }
      started = true;
    }
    dist += successWeight;
  }

  public void fixSalesman() {
    List<City> visitedCities = new ArrayList<>();
    if (path.size() != 0 && roundScore.size() != 0) {
      score = roundScore.get(path.size() - 1);
    }
    needToVisit = new ArrayList<>(Top.positiveCities);
    for (Road r : path) {
      if (!visitedCities.contains(r.end)) {
        visitedCities.add(r.end);
        if (!r.end.equals(SalesmanHandler.base)) {
          needToVisit.remove(r.end);
        }
      } else if (!visitedCities.contains(r.start)) {
        visitedCities.add(r.start);
        if (!r.start.equals(SalesmanHandler.base)) {
          needToVisit.remove(r.start);
        }
      }
      dist += r.speed;
    }
    if (needToVisit.size() == 1 && visitedCities.contains(SalesmanHandler.base)) {
      needToVisit.remove(SalesmanHandler.base);
    }
  }

  public void boldPath() {
    for (Road r : path) {
      r.bold = true;
    }
  }

  public double getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return String
        .format("{score=%f, left=%d, pathsize=%d}", score, needToVisit.size(), path.size());
  }
}
