package com.zenika.digit_recognizer;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class RecognizerPrediction {
    private static final Logger logger = LoggerFactory.getLogger(RecognizerPrediction.class);

    public static void main(String... args) throws Exception {

        if (args.length != 3) {
            throw new Exception("Expecting three arguments.");
        }

        String modelFile = args[0];
        String digitsFile = args[1];
        String imagesPath = args[2];

        logger.info("Making prediction with model " + modelFile + " on " + digitsFile + ", build images and save its in " + imagesPath + ".");

        try (FileInputStream fis = new FileInputStream(modelFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            final MultilayerPerceptronClassificationModel model = (MultilayerPerceptronClassificationModel) ois.readObject();
            ois.close();
            fis.close();
            logger.info("Model loaded correctly.");

            SparkConf sparkConf = new SparkConf().setAppName("digit recognizer prediction");
            JavaSparkContext jsc = new JavaSparkContext(sparkConf);

            JavaRDD<String> digits = jsc.textFile(digitsFile);
            digits.mapPartitionsWithIndex((i, iterator) -> {
                if (i == 0 && iterator.hasNext()) {
                    iterator.next();
                }
                return iterator;
            }, false).foreach(s -> {
                String[] splits = s.split(",");
                double[] pixels = new double[Utils.NB_PIXELS];

                for (int i = 0; i < Utils.NB_PIXELS; i++) {
                    pixels[i] = Double.parseDouble(splits[i]);
                }

                Vector vector = new DenseVector(pixels);

                int prediction = (int) model.predict(vector);

                BufferedImage img = new BufferedImage(Utils.IMAGE_SIZE, Utils.IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < Utils.IMAGE_SIZE; i++) {
                    for (int j = 0; j < Utils.IMAGE_SIZE; j++) {
                        img.setRGB(i, j, (int) pixels[i + j * Utils.IMAGE_SIZE]);
                    }
                }

                String imgName = "img" + Utils.getImgNb() + "_" + prediction + ".png";

                File f = new File(imagesPath + imgName);
                ImageIO.write(img, "PNG", f);
            });

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

}
