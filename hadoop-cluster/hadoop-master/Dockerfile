FROM btravers/hadoop-base:0.0.0

MAINTAINER Benoit TRAVERS <benoit.travers.fr@gmail.com>

# move all confugration files into container
ADD files/* /tmp/

ENV HADOOP_INSTALL /usr/local/hadoop

RUN mkdir -p ~/hdfs/namenode
RUN mkdir -p ~/hdfs/datanode
RUN mkdir $HADOOP_INSTALL/logs

RUN mv /tmp/hdfs-site.xml $HADOOP_INSTALL/etc/hadoop/hdfs-site.xml
RUN mv /tmp/core-site.xml $HADOOP_INSTALL/etc/hadoop/core-site.xml
RUN mv /tmp/mapred-site.xml $HADOOP_INSTALL/etc/hadoop/mapred-site.xml
RUN mv /tmp/yarn-site.xml $HADOOP_INSTALL/etc/hadoop/yarn-site.xml
RUN mv /tmp/slaves $HADOOP_INSTALL/etc/hadoop/slaves
RUN mv /tmp/start-hadoop.sh ~/start-hadoop.sh
RUN mv /tmp/run-wordcount.sh ~/run-wordcount.sh
RUN mv /tmp/start-ssh-serf.sh ~/start-ssh-serf.sh

RUN chmod +x ~/start-hadoop.sh
RUN chmod +x ~/run-wordcount.sh
RUN chmod +x ~/start-ssh-serf.sh
RUN chmod 1777 tmp

# format namenode
RUN /usr/local/hadoop/bin/hdfs namenode -format

EXPOSE 22 7373 7946 9000 50010 50020 50070 50075 50090 50475 8030 8031 8032 8033 8040 8042 8060 8088 50060

# Downloading and installing Spark
RUN curl -s http://d3kbcqa49mib13.cloudfront.net/spark-1.5.0-bin-hadoop2.6.tgz | tar -xz -C /usr/local/
RUN ln -s /usr/local/spark-1.5.0-bin-hadoop2.6 /usr/local/spark
ENV SPARK_HOME /usr/local/spark

RUN echo "#!/bin/bash" > $SPARK_HOME/conf/spark-env.sh
RUN echo 'export SPARK_DIST_CLASSPATH=$(hadoop classpath)' >> $SPARK_HOME/conf/spark-env.sh
RUN chmod +x $SPARK_HOME/conf/spark-env.sh

CMD '/root/start-ssh-serf.sh'; 'bash'
