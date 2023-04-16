package ml.learning.dubinscar.training;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import ml.learning.dubinscar.geometry.DubinsPath;
import ml.learning.dubinscar.geometry.Waypoint;
import ml.learning.dubinscar.pathplanning.NeuralNetworkPathPlanner;
import ml.learning.dubinscar.pathplanning.PathPlanner;
import ml.learning.dubinscar.training.DubinsCarTrainingDataGenerator.TrainingExample;

public class ModelEvaluator {
   private final int numMatches;

   public ModelEvaluator(int numMatches) {
      this.numMatches = numMatches;
   }

   public double evaluate(MultiLayerNetwork model1, MultiLayerNetwork model2) {
      int model1Wins = 0;
      int model2Wins = 0;
      for (int i = 0; i < numMatches; i++) {
         int result = playMatch(model1, model2);
         if (result > 0) {
            model1Wins++;
         } else if (result < 0) {
            model2Wins++;
         }
      }
      return (double) (model1Wins - model2Wins) / numMatches;
   }

   private int playMatch(MultiLayerNetwork model1, MultiLayerNetwork model2) {
      // Play a single match between two models and return the result
      // 1 if model1 wins, -1 if model2 wins, 0 if draw

      DubinsCarTrainingDataGenerator generator = DubinsCarTrainingDataGenerator.getDefault();
      TrainingExample trainingExample = generator.generateTrainingData(1).get(0);

      PathPlanner pathPlanner1 = new NeuralNetworkPathPlanner(model1);
      PathPlanner pathPlanner2 = new NeuralNetworkPathPlanner(model2);

      DubinsPath path1 = pathPlanner1.planPath(trainingExample.getCar(), trainingExample.getStart(),
            trainingExample.getEnd(), trainingExample.getObstacles(),
            trainingExample.getSpeedReductionRegions(), trainingExample.getSwPoint(),
            trainingExample.getNePoint());
      DubinsPath path2 = pathPlanner2.planPath(trainingExample.getCar(), trainingExample.getStart(),
            trainingExample.getEnd(), trainingExample.getObstacles(),
            trainingExample.getSpeedReductionRegions(), trainingExample.getSwPoint(),
            trainingExample.getNePoint());

      double path1Dur = determinePathDuration(path1);
      double path2Dur = determinePathDuration(path2);
      if (path1Dur > path2Dur) {
         return -1;
      } else if (path1Dur == path2Dur) {
         return 0;
      } else {
         return 1;
      }
   }

   private double determinePathDuration(DubinsPath path) {
      double duration = 0;

      for (int i = 0; i < path.getWaypoints().size() - 1; i++) {
         Waypoint wp = path.getWaypoints().get(i);
         Waypoint next = path.getWaypoints().get(i + 1);
         double deltaX = wp.getX() - next.getX();
         double deltaY = wp.getY() - next.getY();
         duration += deltaX * deltaX + deltaY * deltaY;
      }

      return duration;
   }
}
