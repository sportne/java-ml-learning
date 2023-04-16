package ml.learning.dubinscar.visualize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.geometry.Waypoint;

public class DubinsCarPathVisualizer extends JPanel {

   private static final long serialVersionUID = 1L;

   private static final int OFFSET = 100;

   private JFrame frame;

   private Waypoint start;
   private Waypoint end;
   private List<Waypoint> waypoints;
   private List<Obstacle> obstacles;
   private Point2D swPoint;
   private Point2D nePoint;

   public DubinsCarPathVisualizer() {
      setPreferredSize(new Dimension(500, 500));

      frame = new JFrame("Dubins Path Visualizer");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(this);
      frame.pack();
      frame.setVisible(true);
   }

   public void setWaypoints(List<Waypoint> waypoints) {
      this.waypoints = waypoints;
   }

   public void setStart(Waypoint start) {
      this.start = start;
   }

   public void setEnd(Waypoint end) {
      this.end = end;
   }

   public void setObstacles(List<Obstacle> obstacles) {
      this.obstacles = obstacles;
   }

   public void setOpArea(Point2D swPoint, Point2D nePoint) {
      this.swPoint = swPoint;
      this.nePoint = nePoint;
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (start == null || obstacles == null || end == null || waypoints == null) {
         return;
      }

      // draw start and end points as circles
      g.setColor(Color.BLUE);
      g.fillOval((int) start.getX() - 5 + OFFSET, (int) start.getY() - 5 + OFFSET, 10, 10);
      g.fillOval((int) end.getX() - 5 + OFFSET, (int) end.getY() - 5 + OFFSET, 10, 10);

      // draw Dubins path as a series of line segments
      g.setColor(Color.BLACK);
      for (int i = 0; i < waypoints.size() - 1; i++) {
         Waypoint curr = waypoints.get(i);
         Waypoint next = waypoints.get(i + 1);
         g.drawLine((int) curr.getX() + OFFSET, (int) curr.getY() + OFFSET,
               (int) next.getX() + OFFSET, (int) next.getY() + OFFSET);
      }
      g.setColor(Color.BLUE);
      for (int i = 0; i < waypoints.size(); i++) {
         g.fillOval((int) waypoints.get(i).getX() - 2 + OFFSET,
               (int) waypoints.get(i).getY() - 2 + OFFSET, 4, 4);
      }

      // draw each of the obstacles as a series of line segments
      g.setColor(Color.RED);
      for (Obstacle obstacle : obstacles) {
         int[] xs = new int[obstacle.getPolygon().getVertices().size()];
         int[] ys = new int[obstacle.getPolygon().getVertices().size()];
         for (int i = 0; i < obstacle.getPolygon().getVertices().size(); i++) {
            xs[i] = (int) obstacle.getPolygon().getVertices().get(i).getX() + OFFSET;
            ys[i] = (int) obstacle.getPolygon().getVertices().get(i).getY() + OFFSET;
         }
         g.drawPolygon(xs, ys, xs.length);

         g.setColor(new Color(255, 0, 0, 100));
         g.fillPolygon(xs, ys, xs.length);
      }

      // draw the area being worked in
      g.setColor(Color.RED);
      g.drawRect((int) swPoint.getX() + OFFSET, (int) swPoint.getY() + OFFSET,
            (int) (nePoint.getX() - swPoint.getX()), (int) (nePoint.getY() - swPoint.getY()));

   }

}
