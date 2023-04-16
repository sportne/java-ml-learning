package ml.learning.dubinscar.pathplanning;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.environment.SpeedReductionRegion;
import ml.learning.dubinscar.geometry.DubinsCar;
import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Polygon2D;
import ml.learning.dubinscar.geometry.Waypoint;

public class NeuralNetworkPathPlanner implements PathPlanner {

   private final MultiLayerNetwork model;
   private INDArray mostRecentOutput;
   private INDArray mostRecentInput;

   public NeuralNetworkPathPlanner(MultiLayerNetwork model) {
      this.model = model;
   }

   private static INDArray createObstacleData(List<Obstacle> obstacles) {
      int numObstacles = obstacles.size();
      double[] obstacleData = new double[MAX_POLYGONS * POLYGON_SIZE];
      for (int i = 0; i < numObstacles; i++) {
         Polygon2D obstacle = obstacles.get(i).getPolygon();
         for (int j = 0; j < obstacle.getVertices().size(); j++) {
            Point2D p = obstacle.getVertices().get(j);
            obstacleData[i * POLYGON_SIZE + j * 2] = p.getX();
            obstacleData[i * POLYGON_SIZE + j * 2 + 1] = p.getY();
         }
      }
      return Nd4j.create(obstacleData);
   }

   private static INDArray createSpeedReductionData(
         List<SpeedReductionRegion> speedReductionRegions) {
      int numObstacles = speedReductionRegions.size();
      double[] obstacleData = new double[MAX_POLYGONS * POLYGON_SIZE];
      for (int i = 0; i < numObstacles; i++) {
         Polygon2D obstacle = speedReductionRegions.get(i).getPolygon();
         for (int j = 0; j < obstacle.getVertices().size(); j++) {
            Point2D p = obstacle.getVertices().get(j);
            obstacleData[i * POLYGON_SIZE + j * 2] = p.getX();
            obstacleData[i * POLYGON_SIZE + j * 2 + 1] = p.getY();
         }
      }

      return Nd4j.create(obstacleData);
   }

   public INDArray getLastOutput() {
      return mostRecentOutput;
   }

   public INDArray getLastInput() {
      return mostRecentInput;
   }

   private static final int OPAREA_SIZE = 4;
   private static final int CAR_SIZE = 2;
   private static final int WP_SIZE = 3;
   private static final int VERTEX_SIZE = 2;
   public static final int MAX_VERTICES_PER_POLY = 3;
   private static final int POLYGON_SIZE = VERTEX_SIZE * MAX_VERTICES_PER_POLY;
   public static final int MAX_POLYGONS = 20;
   public static final int MAX_WAYPOINTS = 10;

   public static int getInputSize() {
      return OPAREA_SIZE + CAR_SIZE + WP_SIZE + WP_SIZE + POLYGON_SIZE * MAX_POLYGONS * 2;
   }

   public static int getOutputSize() {
      return WP_SIZE * MAX_WAYPOINTS;
   }

   public static INDArray produceInputArray(DubinsCar car, Waypoint start, Waypoint end,
         List<Obstacle> obstacles, List<SpeedReductionRegion> speedReductionRegions,
         Point2D swPoint, Point2D nePoint) {
      // Convert input data into INDArrays
      INDArray opArea = Nd4j.create(
            new double[] { swPoint.getX(), swPoint.getY(), nePoint.getX(), nePoint.getY() });
      INDArray carState = Nd4j.create(new double[] { car.getRadius(), car.getSpeed() });
      INDArray startState = Nd4j
            .create(new double[] { start.getX(), start.getY(), start.getOrientation() });
      INDArray endState = Nd4j
            .create(new double[] { end.getX(), end.getY(), end.getOrientation() });
      INDArray obstacleData = createObstacleData(obstacles);
      INDArray speedReductionData = createSpeedReductionData(speedReductionRegions);

      INDArray input = Nd4j.hstack(opArea, carState, startState, endState, obstacleData,
            speedReductionData);
      return input.reshape(1, input.size(0));
   }

   @Override
   public DubinsPath planPath(DubinsCar car, Waypoint start, Waypoint end, List<Obstacle> obstacles,
         List<SpeedReductionRegion> speedReductionRegions, Point2D swPoint, Point2D nePoint) {

      // Stack inputs into a single INDArray
      INDArray input = produceInputArray(car, start, end, obstacles, speedReductionRegions, swPoint,
            nePoint);
      mostRecentInput = input;

      // Predict output using the neural network
      INDArray output = model.output(input);
      mostRecentOutput = output;

      // Convert output into a DubinsPath object
      List<Waypoint> waypoints = outputToWaypoints(output, nePoint, swPoint, start, end);
      return new DubinsPath(waypoints);
   }

   public static INDArray waypointsToOutput(List<Waypoint> waypoints, Point2D nePoint,
         Point2D swPoint, Waypoint start, Waypoint end) {
      double width = nePoint.getX() - swPoint.getX();
      double height = nePoint.getY() - swPoint.getY();

      double[][] output = new double[1][3 * MAX_WAYPOINTS];
      for (int i = 0; i < waypoints.size() - 2; i++) {
         output[0][i * 3] = (waypoints.get(i + 1).getX() - swPoint.getX()) / width;
         output[0][i * 3 + 1] = (waypoints.get(i + 1).getY() - swPoint.getY()) / height;
         output[0][i * 3 + 2] = waypoints.get(i + 1).getOrientation() / (2 * Math.PI);
      }
      for (int i = waypoints.size() - 2; i < MAX_WAYPOINTS; i++) {
         output[0][i * 3] = -1;
         output[0][i * 3 + 1] = -1;
         output[0][i * 3 + 2] = -1;
      }
      return Nd4j.create(output);
   }

   public static List<Waypoint> outputToWaypoints(INDArray output, Point2D nePoint, Point2D swPoint,
         Waypoint start, Waypoint end) {
      List<Waypoint> waypoints = new ArrayList<>();
      waypoints.add(start);
      double width = nePoint.getX() - swPoint.getX();
      double height = nePoint.getY() - swPoint.getY();
      for (int i = 1; 3 * i < output.size(1); i++) {
         double x = output.getDouble(3 * i) * width + swPoint.getX();
         double y = output.getDouble(3 * i + 1) * height + swPoint.getY();
         double orientation = output.getDouble(3 * i + 2) * Math.PI * 2;
         if (x < 0 && y < 0 && orientation < 0) {
            continue;
         }
         waypoints.add(new Waypoint(x, y, orientation));
      }
      waypoints.add(end);

      return waypoints;
   }

}
