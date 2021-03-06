package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class Mapper extends javax.swing.JFrame {

  private static boolean ran = false;
  private javax.swing.JLabel status;

  private Mapper() {
    initComponents();
    String err = MapReadder.readMapFile(new File("coloradomap.csv"));
    if (err != null) {
      System.out.println("Error: " + err);
      System.exit(0);
    }
  }

  public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(() -> new Mapper().setVisible(true));
  }

  private void initComponents() {
    javax.swing.JPanel mapPanel;
    javax.swing.JButton startButton;
    mapPanel = new MyPanel();
    status = new javax.swing.JLabel();
    startButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    mapPanel.setPreferredSize(new java.awt.Dimension(1000, 650));
    status.setPreferredSize(new java.awt.Dimension(100, 100));
    status.setText("Distance = 0");

    startButton.setPreferredSize(new java.awt.Dimension(100, 100));
    startButton.setText("Start Problem");
    startButton.addActionListener((java.awt.event.ActionEvent evt) -> startButtonActionPerformed());

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(mapPanel)
            .addComponent(startButton)
            .addGap(50)
            .addComponent(status)
            .addGap(100)
    );
    layout.setVerticalGroup(
        layout.createSequentialGroup()
            .addComponent(mapPanel)
            .addComponent(startButton)
            .addComponent(status)
    );
    pack();
  }

  private void startButtonActionPerformed() {
    if (!ran) {
      ran = true;
      Thread t = new Thread(() -> {
        Top.self = this;
        Top.planRoute();
        Top.getShortest();
        Mapper.ran = false;
      });
      t.start();
    }

  }

  public class MyPanel extends JPanel {

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g.setFont(new java.awt.Font("Tahoma", Font.BOLD, 11));
      for (City c : City.cities) {
        for (Road r : c.roads) {
          if (r.bold) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.BLUE);
            g2d.drawLine(c.p.sx(), c.p.sy(), r.end.p.sx(), r.end.p.sy());
            g2d.setStroke(new BasicStroke(1));
          } else {
            g.setColor(Color.RED);
            g.drawLine(c.p.sx(), c.p.sy(), r.end.p.sx(), r.end.p.sy());
          }
        }
      }

      for (City c : City.cities) {
        for (Road r : c.roads) {
          r.bold = false;
        }
      }

      for (City c : City.cities) {
        int px = c.p.sx();
        int py = c.p.sy();
        g.setColor(Color.BLACK);
        g.fillOval(px - 2, py - 2, 5, 5);
        g.drawString(c.name, px + 2, py - 2);
      }
    }
  }

  void updateDist(double dist) {
    status.setText("Distance = " + dist);
  }
}
