# Digit Recognizer

## Prerequisite

- Maven
- Docker

## Introduction

This application is a digit recognizer running on Spark 1.5.0 or more. 
Input data sets come from [Kaggle](https://www.kaggle.com/) and represent pixels of digit images.
In order to correctly predict digits, I use a [Multilayer perceptron classifier](https://en.wikipedia.org/wiki/Perceptron).  

## Build recognizer

First, you need to build Spark recognizer using Maven:

    mvn clean package

## Build Hadoop/Spark cluster

If you run the application for the first time, you need to build cluster's Docker images:

    ./build-images.sh
    
Then, start the Hadoop cluster with:

    ./start-cluster.sh
  
Once the cluster running, you will run bash shell in master container. The next steps will run in master container.

### Run Spark job

Compute the model:

    $SPARK_HOME/bin/spark-submit --class com.zenika.digit_recognizer.RecognizerComputation /recognizer/digit_recognizer-0.0.0-jar-with-dependencies.jar file:/data/train.csv /data/model
  
Recognizer digit from test file using:
  
    $SPARK_HOME/bin/spark-submit --class com.zenika.digit_recognizer.RecognizerPrediction /recognizer/digit_recognizer-0.0.0-jar-with-dependencies.jar /data/model file:/data/test.csv /images/


### Run Spark job on YARN
 
Start Hadoop:

    ./start-hadoop.sh
  
Put datasets into HDFS:

    hadoop dfs -mkdir /data
    hadoop dfs -put /data/* /data

Finally, run Spark job on YARN for model computation and digit prediction:

    $SPARK_HOME/bin/spark-submit --master yarn-cluster --class com.zenika.digit_recognizer.RecognizerComputation /recognizer/digit_recognizer-0.0.0-jar-with-dependencies.jar /data/train.csv /data/model
    $SPARK_HOME/bin/spark-submit --master yarn-cluster --class com.zenika.digit_recognizer.RecognizerPrediction /recognizer/digit_recognizer-0.0.0-jar-with-dependencies.jar /data/model /data/test.csv /images/
  
