package ml.learning.dubinscar.training;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.environment.SpeedReductionRegion;
import ml.learning.dubinscar.geometry.DubinsCar;
import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Polygon2D;
import ml.learning.dubinscar.geometry.Waypoint;
import ml.learning.dubinscar.pathplanning.NeuralNetworkPathPlanner;

public class DubinsCarTrainingDataGenerator {

   public static class TrainingExample {
      private DubinsCar car;
      private Waypoint start;
      private Waypoint end;
      private List<Obstacle> obstacles;
      private List<SpeedReductionRegion> speedReductionRegions;
      private DubinsPath dubinsPath;
      private Point2D swPoint;
      private Point2D nePoint;

      TrainingExample(DubinsCar car, Waypoint start, Waypoint end, List<Obstacle> obstacles,
            List<SpeedReductionRegion> speedReductionRegions, Point2D swPoint, Point2D nePoint,
            DubinsPath dubinsPath) {
         this.car = car;
         this.start = start;
         this.end = end;
         this.obstacles = obstacles;
         this.speedReductionRegions = speedReductionRegions;
         this.swPoint = swPoint;
         this.nePoint = nePoint;
         this.dubinsPath = dubinsPath;
      }

      public DubinsCar getCar() {
         return car;
      }

      public Waypoint getStart() {
         return start;
      }

      public Waypoint getEnd() {
         return end;
      }

      public Point2D getSwPoint() {
         return swPoint;
      }

      public Point2D getNePoint() {
         return nePoint;
      }

      public List<Obstacle> getObstacles() {
         return obstacles;
      }

      public List<SpeedReductionRegion> getSpeedReductionRegions() {
         return speedReductionRegions;
      }

      public DubinsPath getDubinsPath() {
         return dubinsPath;
      }

   }

   public static DubinsCarTrainingDataGenerator getDefault() {
      return new DubinsCarTrainingDataGenerator(new Random().nextLong(), 100, 100, 10, 5, 10, 10, 25,
            NeuralNetworkPathPlanner.MAX_VERTICES_PER_POLY, NeuralNetworkPathPlanner.MAX_POLYGONS);
   }

   private final Random random;

   private final double environmentWidth;
   private final double environmentHeight;

   private final int maxSpeed;
   private final double turnRadius;

   private final int maxNumWaypoints;

   private final double minObstacleWidth;
   private final double maxObstacleWidth;
   private final int maxVertices;
   private final int maxNumPolygons;

   public DubinsCarTrainingDataGenerator(long seed, double environmentWidth,
         double environmentHeight, int maxSpeed, double turnRadius, int maxNumWaypoints,
         double minObstacleWidth, double maxObstacleWidth, int maxVertices, int maxNumPolygons) {
      this.random = new Random(seed);

      this.environmentWidth = environmentWidth;
      this.environmentHeight = environmentHeight;

      this.maxSpeed = maxSpeed;
      this.turnRadius = turnRadius;

      this.maxNumWaypoints = maxNumWaypoints;

      this.minObstacleWidth = minObstacleWidth;
      this.maxObstacleWidth = maxObstacleWidth;
      this.maxVertices = maxVertices;
      this.maxNumPolygons = maxNumPolygons;
   }

   public List<TrainingExample> generateTrainingData(int numSamples) {
      List<TrainingExample> trainingData = new ArrayList<>(numSamples);
      for (int i = 0; i < numSamples; i++) {
         Waypoint start = generateRandomWaypoint();
         Waypoint end = generateRandomWaypoint();
         List<Obstacle> obstacles = generateRandomObstacles();
         List<SpeedReductionRegion> speedReductionRegions = new ArrayList<>();// generateRandomSpeedReductionRegions();
         DubinsCar dubinsCar = new DubinsCar(start.getX(), start.getY(), start.getOrientation(),
               turnRadius, maxSpeed);
         DubinsPath dubinsPath = generateRandomDubinsPath(start, end);
         trainingData.add(new TrainingExample(dubinsCar, start, end, obstacles,
               speedReductionRegions, new Point2D.Double(0, 0),
               new Point2D.Double(environmentWidth, environmentHeight), dubinsPath));
      }
      return trainingData;
   }

   public Waypoint generateRandomWaypoint() {
      double x = random.nextDouble() * environmentWidth;
      double y = random.nextDouble() * environmentHeight;
      double orientation = random.nextDouble() * Math.PI * 2;
      return new Waypoint(x, y, orientation);
   }

   /**
    * Generates a list of random obstacles in the environment.
    *
    * @return a list of random obstacles
    */
   private List<Obstacle> generateRandomObstacles() {
      List<Obstacle> obstacles = new ArrayList<>();

      int numRegions = random.nextInt(maxNumPolygons);

      for (int i = 0; i < numRegions; i++) {
         Polygon2D poly = generateRandomPolygon2D();
         obstacles.add(new Obstacle(poly, 0.0));
      }

      return obstacles;
   }

   private Polygon2D generateRandomPolygon2D() {
      // Generate a random polygon with a random number of vertices
      int numVertices = (int) (Math.random() * (maxVertices - 3)) + 3;

      double centerX = random.nextDouble() * environmentWidth;
      double centerY = random.nextDouble() * environmentHeight;
      double radius = random.nextDouble() * (maxObstacleWidth - minObstacleWidth);

      List<Point2D> vertices = new ArrayList<>();
      double angle = 0;
      for (int i = 0; i < numVertices; i++) {
         angle += (angle - Math.PI * 2) * 0.5 * random.nextDouble();
         double x = centerX + radius * Math.sin(angle);
         double y = centerY + radius * Math.cos(angle);
         vertices.add(new Point2D.Double(x, y));
      }
      return new Polygon2D(vertices);
   }

   /**
    * Generates a list of random speed reduction regions in the environment.
    *
    * @return a list of random speed reduction regions
    */
   private List<SpeedReductionRegion> generateRandomSpeedReductionRegions() {
      List<SpeedReductionRegion> regions = new ArrayList<>();

      int numRegions = random.nextInt(maxNumPolygons);

      for (int i = 0; i < numRegions; i++) {
         Polygon2D poly = generateRandomPolygon2D();
         regions.add(new SpeedReductionRegion(poly, 0.5));
      }

      return regions;
   }

   /**
    * Generates a random Dubins path using the DubinsCar class and the randomly
    * generated start and end waypoints.
    *
    * @return a random Dubins path
    */
   private DubinsPath generateRandomDubinsPath(Waypoint start, Waypoint end) {
      List<Waypoint> waypoints = new ArrayList<>();

      waypoints.add(start);

      int numWaypoints = random.nextInt(maxNumWaypoints - 2);
      for (int i = 0; i < numWaypoints; i++) {
         waypoints.add(generateRandomWaypoint());
      }

      waypoints.add(end);

      return new DubinsPath(waypoints);
   }

}
