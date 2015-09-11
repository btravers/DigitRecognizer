package com.zenika.digit_recognizer;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class RecognizerComputation {
    private static final Logger logger = LoggerFactory.getLogger(RecognizerComputation.class);

    private static final int MAX_ITER = 100;
    private static final int BLOC_SIZE = 128;
    private static final long SEED = 1234L;

    public static void main(String... args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Expecting two arguments: train file path and model path.");
        }

        String file = args[0];
        String modelPath = args[1];

        logger.info("Compute model using data sets from" + file + " and save resulting model in " + modelPath + ".");

        SparkConf sparkConf = new SparkConf().setAppName("digit recognizer computation");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(jsc);

        logger.info("Loading and formatting data");
        int[] layers = new int[]{Utils.NB_PIXELS, Utils.NB_PIXELS, Utils.NB_DIGITS};
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(BLOC_SIZE)
                .setSeed(SEED)
                .setMaxIter(MAX_ITER);

        JavaRDD<String> rawData = jsc.textFile(file);
        JavaRDD<LabeledPoint> labeledPoints = rawData.mapPartitionsWithIndex((i, iterator) -> {
            if (i == 0 && iterator.hasNext()) {
                iterator.next();
            }
            return iterator;
        }, false).map(s -> {
            String[] splits = s.split(",");

            double val = Double.parseDouble(splits[0]);
            double[] features = new double[splits.length - 1];

            for (int i = 0; i < splits.length - 1; i++) {
                features[i] = Double.parseDouble(splits[i]);
            }

            return new LabeledPoint(val, new DenseVector(features));
        });

        DataFrame dataFrames = sqlContext.createDataFrame(labeledPoints, LabeledPoint.class);

        logger.info("Generating train and test sets randomly.");
        DataFrame[] splits = dataFrames.randomSplit(new double[]{0.6, 0.4}, SEED);

        DataFrame train = splits[0];
        DataFrame test = splits[1];

        logger.info("Training model.");
        MultilayerPerceptronClassificationModel model = trainer.fit(train);

        logger.info("Evaluating final model.");
        DataFrame result = model.transform(test);
        DataFrame predictionAndLabels = result.select("prediction", "label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setMetricName("precision");

        logger.info("Precision = " + evaluator.evaluate(predictionAndLabels));

        try (FileOutputStream fos = new FileOutputStream(modelPath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            logger.info("Serializing model.");
            oos.writeObject(model);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        jsc.stop();
    }
}
