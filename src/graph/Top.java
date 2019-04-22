package graph;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

public class Top {

	public static Route bestRoute;
	public static JFrame gui = new JFrame("Top");
	public static String[] testCities = { "Denver", "Durango", "Grand Junction", "Alamosa", "Fort Collins",
			"Colorado Springs", "Boulder", "Pueblo" };
	public static List<City> positiveCities;
	public static Journey paths[][];
	public static Mapper self;

	public static void planRoute() {
		City[] tcObj = new City[testCities.length];
		for (int i = 0; i < testCities.length; i++) {
			tcObj[i] = City.find(testCities[i]);
		}
		positiveCities = Arrays.asList(tcObj);
	}

	public static void getShortest() {
		SalesmanHandler handler = new SalesmanHandler(100, positiveCities.get(0));
		handler.generations();
	}

	public static void main(String[] args) {
		String err = MapReadder.readMapFile(new File("coloradomap.csv"));
		if (err != null) {
			System.out.println("Error: " + err);
			System.exit(0);
		}

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Mapper().setVisible(true);
			}
		});
	}
}
