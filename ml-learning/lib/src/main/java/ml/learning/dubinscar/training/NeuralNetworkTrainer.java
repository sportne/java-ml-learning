package ml.learning.dubinscar.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import ml.learning.dubinscar.pathplanning.NeuralNetworkPathPlanner;
import ml.learning.dubinscar.training.GameRunner.GameResult;
import ml.learning.dubinscar.visualize.DubinsCarPathVisualizer;

public class NeuralNetworkTrainer {

   public static DubinsCarPathVisualizer visualizer;

   private static final boolean TRAIN = true;

   public static final File model1File = new File("model_1_file.nn");
   public static final File model2File = new File("model_2_file.nn");

   public static void main(String[] args) throws IOException {
      visualizer = new DubinsCarPathVisualizer();

      MultiLayerNetwork model1;
      MultiLayerNetwork model2;
      if (model1File.exists()) {
         model1 = MultiLayerNetwork.load(model1File, true);
      } else {
         model1 = createNewModel();
      }
      if (model2File.exists()) {
         model2 = MultiLayerNetwork.load(model2File, true);
      } else {
         model2 = createNewModel();
      }
      System.out.println(String.format("The total number of parameters in the model is: %d",
            model1.numParams()));

      NeuralNetworkTrainer trainer = new NeuralNetworkTrainer(model1, model2);

      trainer.train(100000);
   }

   public static MultiLayerNetwork createNewModel() {
      long seed = new Random().nextLong();
      int numInputs = NeuralNetworkPathPlanner.getInputSize();
      int numOutputs = NeuralNetworkPathPlanner.getOutputSize();
      int numHidden = (numInputs * 2 + numOutputs) / 2;

      int numHiddenLayers = 10;

      NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder().seed(seed)
            .weightInit(WeightInit.XAVIER).updater(new Adam.Builder().learningRate(1e-3).build())
            .list();

      // input layer to first hidden
      builder.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
            .activation(Activation.RELU).build());
      // hidden layers to hidden layers
      for (int i = 0; i < numHiddenLayers; i++) {
         builder.layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
               .activation(Activation.RELU).build());
      }

      // final hidden layer to output layer
      builder.layer(new OutputLayer.Builder().nIn(numHidden).nOut(numOutputs)
            .activation(Activation.IDENTITY).lossFunction(LossFunctions.LossFunction.MSE).build());
      MultiLayerConfiguration conf = builder.build();

      // Initialize the model
      MultiLayerNetwork model = new MultiLayerNetwork(conf);
      model.init();

      return model;
   }

   private MultiLayerNetwork model1;
   private MultiLayerNetwork model2;

   public NeuralNetworkTrainer(MultiLayerNetwork model1, MultiLayerNetwork model2) {
      this.model1 = model1;
      this.model2 = model2;
   }

   public void train(int numIterations) {

      ExecutorService service = Executors.newFixedThreadPool(1);

      int batchSize = 1000;
      int i = 0;
      // Train for the specified number of iterations
      while (i < numIterations) {
         List<Future<?>> futures = new ArrayList<>();
         for (; i < numIterations && (i % batchSize != batchSize - 1 || futures.isEmpty()); i++) {
            int val = i;
            futures.add(service.submit(() -> {
               // Play a certain number of games between the current network and the opponent
               // network
               GameResult gameResults = new GameRunner().playMatch(model1, model2);

               if (TRAIN) {
                  if (val % 2 == 0) {
                     model1.fit(gameResults.features, gameResults.labels);
                  } else {
                     model2.fit(gameResults.features, gameResults.labels);
                  }
               }

               if (val % 1000 == 0) {
                  System.out.println(String.format("%d: model1 score=%f; model2 score=%f", val,
                        model1.score(), model2.score()));
               }
            }));

         }

         for (var f : futures) {
            try {
               f.get(100000l, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
               e.printStackTrace();
            }
         }

         if (TRAIN) {
            // save off the models
            try {
               model1.save(model1File);
               model2.save(model2File);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }
}
