package graph;

public class Point {

  public int x;
  public int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return "(" + x + "," + y + ")";
  }

  public int sx() {
    return x * 2;
  }

  public int sy() {
    return (int) (y * 1.3) - 30;
  }

  public static Point ToPoint(int x, int y) {
    return new Point(x / 2, (int) ((y - 30) / 1.3));
  }

}
