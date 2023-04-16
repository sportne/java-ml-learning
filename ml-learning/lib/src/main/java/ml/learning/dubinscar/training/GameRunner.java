package ml.learning.dubinscar.training;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import ml.learning.dubinscar.environment.Obstacle;
import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Waypoint;
import ml.learning.dubinscar.pathplanning.NeuralNetworkPathPlanner;
import ml.learning.dubinscar.training.DubinsCarTrainingDataGenerator.TrainingExample;

public class GameRunner {

   private static final Random random = new Random();

   private static final double DESIRED_VALUE = 0;

   public static class GameResult {
      int result;
      INDArray features;
      INDArray labels;
   }

   private INDArray computeOutput(DubinsPath path, TrainingExample trainingExample) {
      return NeuralNetworkPathPlanner.waypointsToOutput(path.getWaypoints(),
            trainingExample.getNePoint(), trainingExample.getSwPoint(), trainingExample.getStart(),
            trainingExample.getEnd());
   }

   private DubinsPath straightPath(TrainingExample data) {
      List<Waypoint> waypoints = new ArrayList<>();

      waypoints.add(data.getStart());

      int numWps = 3; // random.nextInt(NeuralNetworkPathPlanner.MAX_WAYPOINTS);
      for (int i = 0; i < numWps; i++) {
         double fraction = (i + 1.0) / (numWps + 1.0);
         double x = fraction * (data.getEnd().getX() - data.getStart().getX())
               + data.getStart().getX();
         double y = fraction * (data.getEnd().getY() - data.getStart().getY())
               + data.getStart().getY();
         double o = 0;
         waypoints.add(new Waypoint(x, y, o));
      }

      waypoints.add(data.getEnd());

      return new DubinsPath(waypoints);
   }

   private static class Node {
      Waypoint wp;

      List<Node> neighbors = new ArrayList<>();

      double gScore = -1;
      double fScore = -1;
      Node previous;

      Node(Waypoint wp) {
         this.wp = wp;
      }

      List<Node> neighbors() {
         return neighbors;
      }
   }

   private Node search(Node start, Node goal, TrainingExample trainingExample) {
      PriorityQueue<Node> openSet = new PriorityQueue<>((n1, n2) -> {
         return n1.fScore > n2.fScore ? 1 : n1.fScore == n2.fScore ? 0 : -1;
      });
      openSet.add(start);

      Set<Node> closedSet = new HashSet<>();

      while (!openSet.isEmpty()) {
         Node current = openSet.poll();

         if (current.equals(goal)) {
            return current;
         }

         closedSet.add(current);

         for (Node neighbor : current.neighbors()) {
            if (closedSet.contains(neighbor)) {
               continue;
            }

            double tentativeGScore = current.gScore
                  + segmentDuration(current.wp, neighbor.wp, trainingExample);

            if (neighbor.gScore < 0 || tentativeGScore < neighbor.gScore) {
               neighbor.previous = current;
               neighbor.gScore = tentativeGScore;
               neighbor.fScore = neighbor.gScore
                     + segmentDuration(neighbor.wp, goal.wp, trainingExample);

               if (!openSet.contains(neighbor)) {
                  openSet.add(neighbor);
               }
            }
         }
      }

      return null;
   }

   private DubinsPath graphPath(TrainingExample data) {

      Node start = new Node(data.getStart());
      start.gScore = 0;
      start.fScore = 0;

      Node end = new Node(data.getEnd());

      Point2D sw = data.getSwPoint();
      Point2D ne = data.getNePoint();

      int discretizationLevel = 50;
      Node[][] graph = new Node[discretizationLevel][discretizationLevel];
      for (int i = 0; i < discretizationLevel; i++) {
         for (int j = 0; j < discretizationLevel; j++) {
            graph[i][j] = new Node(
                  new Waypoint((double) i / discretizationLevel * (ne.getX() - sw.getX()),
                        (double) j / discretizationLevel * (ne.getY() - sw.getY()), 0));
         }
      }

      int neighborDist = discretizationLevel / 4;
      for (int i = 0; i < discretizationLevel; i++) {
         for (int j = 0; j < discretizationLevel; j++) {

            if (distance(graph[i][j].wp, end.wp) < sw.distance(ne) / discretizationLevel
                  * neighborDist) {
               graph[i][j].neighbors().add(end);
            }

            if (distance(graph[i][j].wp, start.wp) < sw.distance(ne) / discretizationLevel
                  * neighborDist) {
               start.neighbors().add(graph[i][j]);
            }

            for (int k = -neighborDist; k < neighborDist; k++) {
               if (i + k >= 0 && i + k < discretizationLevel) {
                  for (int l = -neighborDist; l < neighborDist; l++) {
                     if ((k != 0 || l != 0) && j + l >= 0 && j + l < discretizationLevel)
                        graph[i][j].neighbors.add(graph[i + k][j + l]);
                  }
               }
            }
         }
      }

      Node result = search(start, end, data);

      if (result == null) {
         return new DubinsPath(Arrays.asList(data.getStart(), data.getEnd()));
      }

      // collect the final path
      List<Waypoint> reverseList = new ArrayList<>();
      Node current = end;
      while (current.previous != null) {
         reverseList.add(current.wp);
         current = current.previous;
      }
      reverseList.add(current.wp);

      List<Waypoint> waypoints = new ArrayList<>(reverseList.size());
      for (int i = reverseList.size() - 1; i >= 0; i--) {
         waypoints.add(reverseList.get(i));
      }

      if (waypoints.size() > 12) {
         return new DubinsPath(Arrays.asList(data.getStart(), data.getEnd()));
      }

      return new DubinsPath(waypoints);
   }

   private DubinsPath randomPath(TrainingExample data) {

      List<Waypoint> waypoints = new ArrayList<>();

      waypoints.add(data.getStart());

      int numWps = random.nextInt(NeuralNetworkPathPlanner.MAX_WAYPOINTS);
      for (int i = 0; i < numWps; i++) {
         double x = random.nextDouble(data.getSwPoint().getX(), data.getNePoint().getX());
         double y = random.nextDouble(data.getSwPoint().getY(), data.getNePoint().getY());
         double o = random.nextDouble(Math.PI * 2);
         waypoints.add(new Waypoint(x, y, o));
      }

      waypoints.add(data.getEnd());

      return new DubinsPath(waypoints);
   }

   private DubinsPath useModel(MultiLayerNetwork model, TrainingExample trainingExample) {
      NeuralNetworkPathPlanner pathPlanner = new NeuralNetworkPathPlanner(model);
      return pathPlanner.planPath(trainingExample.getCar(), trainingExample.getStart(),
            trainingExample.getEnd(), trainingExample.getObstacles(),
            trainingExample.getSpeedReductionRegions(), trainingExample.getSwPoint(),
            trainingExample.getNePoint());
   }

   int solutionCount = 0;

   private DubinsPath getASolution(MultiLayerNetwork model, TrainingExample trainingExample) {
      solutionCount++;

      double controller = random.nextDouble();

      double horizon = 10;

      if (controller > 0)
         if (solutionCount % 2 == 0) {
            return straightPath(trainingExample);
         } else {
            return graphPath(trainingExample);
         }

//      if(controller > 0)
//      {
//         return straightPath(trainingExample);
//      }
//      if (controller > 0) {
//         return graphPath(trainingExample);
//      }

      if (controller < 0.33 * (horizon - count) / horizon) {
         return randomPath(trainingExample);
      } else if (controller < 0.66 * (horizon - count) / horizon) {
         return straightPath(trainingExample);
      }
      return useModel(model, trainingExample);
   }

   public GameResult playMatch(MultiLayerNetwork model1, MultiLayerNetwork model2) {
      // Play a single match between two models and return the result
      // 1 if model1 wins, -1 if model2 wins, 0 if draw

      DubinsCarTrainingDataGenerator generator = DubinsCarTrainingDataGenerator.getDefault();
      TrainingExample trainingExample = generator.generateTrainingData(1).get(0);

      // get output paths
      DubinsPath path1 = getASolution(model1, trainingExample);
      DubinsPath path2 = getASolution(model2, trainingExample);
      INDArray path1Output = computeOutput(path1, trainingExample);
      INDArray path2Output = computeOutput(path2, trainingExample);

      double path1Dur = determinePathDuration(path1, trainingExample);
      double path2Dur = determinePathDuration(path2, trainingExample);
      double optimalDur = segmentDuration(trainingExample.getStart(), trainingExample.getEnd(),
            trainingExample);
      optimalDur = optimalDur > DESIRED_VALUE ? optimalDur - DESIRED_VALUE : 0;

      GameResult result = new GameResult();
      result.features = NeuralNetworkPathPlanner.produceInputArray(trainingExample.getCar(),
            trainingExample.getStart(), trainingExample.getEnd(), trainingExample.getObstacles(),
            trainingExample.getSpeedReductionRegions(), trainingExample.getSwPoint(),
            trainingExample.getNePoint());
      if (path1Dur > path2Dur) {
         result.result = -1;
         result.labels = path2Output;
         printResults(
               String.format("Model 2 won! %f vs %f [optimal %f]. %d %s", path1Dur, path2Dur,
                     optimalDur, path2.getWaypoints().size(), path2.getWaypoints()),
               trainingExample, path2.getWaypoints());
      } else if (path1Dur == path2Dur) {
         result.result = 0;
         result.labels = path2Output;
         printResults(
               String.format("It was a tie! %f vs %f [optimal %f]. %d %s", path1Dur, path2Dur,
                     optimalDur, path1.getWaypoints().size(), path1.getWaypoints()),
               trainingExample, path1.getWaypoints());
      } else {
         result.result = 1;
         result.labels = path1Output;
         printResults(
               String.format("Model 1 won! %f vs %f [optimal %f]. %d %s", path1Dur, path2Dur,
                     optimalDur, path1.getWaypoints().size(), path1.getWaypoints()),
               trainingExample, path1.getWaypoints());
      }

      return result;
   }

   private void printIteration(String msg) {
      if (count++ % 100 == 0) {
         System.out.println("" + count + ": " + msg);
      }
   }

   private void printResults(String msg, TrainingExample trainingExample,
         List<Waypoint> waypoints) {
      printIteration(msg);
      if (count % 100 == 0) {
         NeuralNetworkTrainer.visualizer.setStart(trainingExample.getStart());
         NeuralNetworkTrainer.visualizer.setEnd(trainingExample.getEnd());
         NeuralNetworkTrainer.visualizer.setWaypoints(waypoints);
         NeuralNetworkTrainer.visualizer.setObstacles(trainingExample.getObstacles());
         NeuralNetworkTrainer.visualizer.setOpArea(trainingExample.getSwPoint(),
               trainingExample.getNePoint());
         NeuralNetworkTrainer.visualizer.repaint();
      }
   }

   private static int count = 0;

   private double determinePathDuration(DubinsPath path, TrainingExample trainingExample) {
      double duration = 0;

      for (int i = 0; i < path.getWaypoints().size() - 1; i++) {
         duration += segmentDuration(path.getWaypoints().get(i), path.getWaypoints().get(i + 1),
               trainingExample);
      }

      // change the metric to be targeting a specific length, 50
      double error = Math.abs(DESIRED_VALUE - duration);

      return error;
   }

   private double segmentDuration(Waypoint wp1, Waypoint wp2, TrainingExample trainingExample) {
      double segmentLength = distance(wp1, wp2);
      if (intersectsObstacle(wp1, wp2, trainingExample)) {
         segmentLength *= 100;
      }
      if (isNotInOpArea(wp1, trainingExample.getSwPoint(), trainingExample.getNePoint())
            || isNotInOpArea(wp2, trainingExample.getSwPoint(), trainingExample.getNePoint())) {
         segmentLength *= 100;
      }
      return segmentLength;
   }

   private boolean isNotInOpArea(Waypoint wp1, Point2D sw, Point2D ne) {
      return wp1.getX() < sw.getX() || wp1.getX() > ne.getX() || wp1.getY() < sw.getY()
            || wp1.getY() > ne.getY();
   }

   private double distance(Waypoint wp1, Waypoint wp2) {
      double deltaX = wp1.getX() - wp2.getX();
      double deltaY = wp1.getY() - wp2.getY();
      return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
   }

   private boolean intersectsObstacle(Waypoint wp1, Waypoint wp2, TrainingExample trainingExample) {
      for (Obstacle obstacle : trainingExample.getObstacles()) {
         if (obstacle.intersects(wp1.getPoint2D(), wp2.getPoint2D())) {
            return true;
         }
      }
      return false;
   }

}
