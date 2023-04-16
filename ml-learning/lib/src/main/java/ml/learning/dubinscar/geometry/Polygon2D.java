package ml.learning.dubinscar.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

public class Polygon2D {
   private List<Point2D> vertices;

   public Polygon2D(List<Point2D> vertices) {
      this.vertices = vertices;
   }

   public List<Point2D> getVertices() {
      return vertices;
   }

   public boolean contains(Point2D point) {
      int crossings = 0;

      // Check for crossings by drawing a line from the point to infinity
      for (int i = 0; i < vertices.size(); i++) {
         Point2D a = vertices.get(i);
         Point2D b = vertices.get((i + 1) % vertices.size());

         if (a.getY() != b.getY() && point.getY() >= Math.min(a.getY(), b.getY())
               && point.getY() < Math.max(a.getY(), b.getY())) {
            double x = (point.getY() - a.getY()) * (b.getX() - a.getX()) / (b.getY() - a.getY())
                  + a.getX();
            if (x < point.getX()) {
               crossings++;
            }
         }
      }

      return crossings % 2 != 0;
   }

   public boolean intersects(Point2D start, Point2D end) {
      if (contains(start) || contains(end)) {
         return true;
      }

      Line2D line1 = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());

      for (int i = 0; i < vertices.size(); i++) {
         Point2D next = vertices.get((i + 1) % vertices.size());
         Line2D line2 = new Line2D.Double(vertices.get(i).getX(), vertices.get(i).getY(),
               next.getX(), next.getY());

         if (line1.intersectsLine(line2)) {
            return true;
         }
      }

      return false;
   }
}
