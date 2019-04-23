package graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MapReadder {

  public static String error;

  public static int getint(String f, String l) {
    try {
      return Integer.parseInt(f);
    } catch (Exception e) {
      error = "Bad Integer: " + f + " in " + l;
      return 0;
    }
  }

  public static String readMapFile(File inputf) {
    try {
      error = null;
      BufferedReader reader = new BufferedReader(new FileReader(inputf));
      String s;
      boolean inCity = true;
      while ((s = reader.readLine()) != null) {
        String[] fields = s.split(",");
        if (inCity) {
          if (s.charAt(0) == ',') {
            inCity = false;
          } else {
            if (fields.length < 3) {
              error = "Bad road: " + s;
            } else {
              new City(fields[0], getint(fields[1], s), getint(fields[2], s));
            }
          }
        } else {
          if (fields.length < 5) {
            error = "Bad road: " + s;
          } else {
            City start = City.find(fields[1]);
            City end = City.find(fields[2]);
            Road r1 = new Road(fields[0], start, end, getint(fields[3], s), getint(fields[4], s));
            start.addRoad(r1);
            end.addRoad(r1);
          }
        }
      }
      reader.close();

    } catch (IOException e) {
      System.out.println("Error: " + e);
    }

    return error;
  }
}
