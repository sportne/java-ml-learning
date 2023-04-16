package ml.learning.dubinscar;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.environment.SpeedReductionRegion;
import ml.learning.dubinscar.geometry.DubinsCar;
import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Polygon2D;
import ml.learning.dubinscar.geometry.Waypoint;
import ml.learning.dubinscar.pathplanning.NeuralNetworkPathPlanner;
import ml.learning.dubinscar.pathplanning.PathPlanner;
import ml.learning.dubinscar.visualize.DubinsCarPathVisualizer;

public class Driver {
   public static void main(String[] args) throws IOException {
      // create DubinsCar
      double x = 0;
      double y = 0;
      double theta = 0;
      double r = 1;
      int speed = 2;
      DubinsCar car = new DubinsCar(x, y, theta, r, speed);

      // create start and end points
      Waypoint start = new Waypoint(0, 0, 0);
      Waypoint end = new Waypoint(5, 5, Math.PI / 2);

      // create obstacles
      List<Obstacle> obstacles = new ArrayList<>();
      obstacles.add(new Obstacle(new Polygon2D(Arrays.asList(new Point2D.Double(2, 2),
            new Point2D.Double(3, 2), new Point2D.Double(3, 3), new Point2D.Double(2, 3))), 0.0));

      // create speed reduction regions
      List<SpeedReductionRegion> speedReductionRegions = new ArrayList<>();
      speedReductionRegions.add(new SpeedReductionRegion(
            new Polygon2D(Arrays.asList(new Point2D.Double(1, 1), new Point2D.Double(2, 1),
                  new Point2D.Double(2, 2), new Point2D.Double(1, 2))),
            0.5));

      // create path planner
      MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork("path/to/trained/model");
      PathPlanner pathPlanner = new NeuralNetworkPathPlanner(model);

      // find optimal path
      DubinsPath path = pathPlanner.planPath(car, start, end, obstacles, speedReductionRegions,
            new Point2D.Double(), new Point2D.Double(10, 10));

      // create visualization of start, end, and path
      DubinsCarPathVisualizer visualizer = new DubinsCarPathVisualizer();
   }
}
