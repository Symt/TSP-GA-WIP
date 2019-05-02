package graph;

import java.util.Arrays;
import java.util.List;

class Top {

  private static String[] testCities = {"Denver", "Durango", "Grand Junction", "Alamosa",
      "Fort Collins", "Colorado Springs", "Boulder", "Pueblo"};
  static List<City> positiveCities;
  static Mapper self;

  static void planRoute() {
    City[] tcObj = new City[testCities.length];
    for (int i = 0; i < testCities.length; i++) {
      tcObj[i] = City.find(testCities[i]);
    }
    positiveCities = Arrays.asList(tcObj);
  }

  static void getShortest() {
    SalesmanHandler handler = new SalesmanHandler(100, positiveCities.get(0));
    self.updateDist(handler.generations());
    Top.self.repaint();
  }
}