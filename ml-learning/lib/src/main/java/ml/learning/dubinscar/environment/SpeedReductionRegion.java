package ml.learning.dubinscar.environment;

import ml.learning.dubinscar.geometry.Polygon2D;

public class SpeedReductionRegion {
   private Polygon2D polygon;
   private double reductionFactor;

   public SpeedReductionRegion(Polygon2D polygon, double reductionFactor) {
      this.polygon = polygon;
      this.reductionFactor = reductionFactor;
   }

   public Polygon2D getPolygon() {
      return polygon;
   }

   public double getReductionFactor() {
      return reductionFactor;
   }
}
