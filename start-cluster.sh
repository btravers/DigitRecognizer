#!/usr/bin/env bash

N=3

docker rm -f hadoop-master
echo "start master container..."
docker run -d -t --dns 127.0.0.1 -P --name hadoop-master -h master.btravers -v $PWD/data:/data -v $PWD/target:/recognizer -v $PWD/images:/images -w /root btravers/hadoop-master:0.0.0

FIRST_IP=$(docker inspect --format="{{.NetworkSettings.IPAddress}}" hadoop-master)

i=1
while [ $i -lt $N ]
do
	docker rm -f hadoop-slave$i
	echo "start slave$i container..."
	docker run -d -t --dns 127.0.0.1 -P --name hadoop-slave$i -h slave$i.btravers -e JOIN_IP=$FIRST_IP btravers/hadoop-slave:0.0.0
	((i++))
done

#docker exec master /root/start-hadoop.sh
docker exec -it hadoop-master bash
