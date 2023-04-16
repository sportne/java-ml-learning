package ml.learning.dubinscar.pathplanning;

import java.awt.geom.Point2D;
import java.util.List;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.environment.SpeedReductionRegion;
import ml.learning.dubinscar.geometry.DubinsCar;
import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Waypoint;

public interface PathPlanner {
   DubinsPath planPath(DubinsCar car, Waypoint start, Waypoint end, List<Obstacle> obstacles,
         List<SpeedReductionRegion> speedRegions, Point2D swPoint, Point2D nePoint);
}
