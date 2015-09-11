#!/usr/bin/env bash

docker build -t btravers/serf-dnsmasq:0.0.0 hadoop-cluster/serf-dnsmasq
docker build -t btravers/hadoop-base:0.0.0 hadoop-cluster/hadoop-base
docker build -t btravers/hadoop-slave:0.0.0 hadoop-cluster/hadoop-slave
docker build -t btravers/hadoop-master:0.0.0 hadoop-cluster/hadoop-master