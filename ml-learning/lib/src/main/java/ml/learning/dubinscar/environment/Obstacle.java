package ml.learning.dubinscar.environment;

import java.awt.geom.Point2D;

import ml.learning.dubinscar.geometry.Polygon2D;

public class Obstacle {

   private Polygon2D shape;
   private double speedReduction;

   public Obstacle(Polygon2D shape, double speedReduction) {
      this.shape = shape;
      this.speedReduction = speedReduction;
   }

   public boolean intersects(Point2D start, Point2D end) {
      return shape.intersects(start, end);
   }

   public boolean contains(Point2D point) {
      return shape.contains(point);
   }

   public boolean contains(double x, double y) {
      return shape.contains(new Point2D.Double(x, y));
   }

   public double getSpeedReduction() {
      return speedReduction;
   }

   public Polygon2D getPolygon() {
      return shape;
   }
}
