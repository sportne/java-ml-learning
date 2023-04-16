package ml.learning.dubinscar.geometry;

import java.awt.geom.Point2D;
import java.util.Objects;

public class Waypoint {

   private double x;
   private double y;
   private double orientation;

   public Waypoint(double x, double y, double orientation) {
      this.x = x;
      this.y = y;
      this.orientation = orientation;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if ((obj == null) || (getClass() != obj.getClass()))
         return false;
      Waypoint other = (Waypoint) obj;
      return Double.doubleToLongBits(orientation) == Double.doubleToLongBits(other.orientation)
            && Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
            && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
   }

   public double getOrientation() {
      return orientation;
   }

   public Point2D getPoint2D() {
      return new Point2D.Double(x, y);
   }

   public double getX() {
      return x;
   }

   public double getY() {
      return y;
   }

   @Override
   public int hashCode() {
      return Objects.hash(orientation, x, y);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Waypoint [x=");
      builder.append(x);
      builder.append(", y=");
      builder.append(y);
      builder.append(", orientation=");
      builder.append(orientation);
      builder.append("]");
      return builder.toString();
   }

}
