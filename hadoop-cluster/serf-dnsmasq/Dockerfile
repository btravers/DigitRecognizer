# Creates a base ubuntu image with serf and dnsmasq
FROM ubuntu:15.04

MAINTAINER Benoit TRAVERS <benoit.travers.fr@gmail.com>

RUN apt-get update
RUN apt-get install -y unzip curl dnsmasq

# dnsmasq configuration
ADD dnsmasq/* /etc/

# install serf
RUN curl -Lso serf.zip https://dl.bintray.com/mitchellh/serf/0.5.0_linux_amd64.zip
RUN unzip serf.zip -d /bin  
RUN rm serf.zip

# configure serf
ENV SERF_CONFIG_DIR /etc/serf
ADD serf/* $SERF_CONFIG_DIR/
ADD handlers $SERF_CONFIG_DIR/handlers
RUN chmod +x  $SERF_CONFIG_DIR/event-router.sh $SERF_CONFIG_DIR/start-serf-agent.sh
