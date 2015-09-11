FROM btravers/serf-dnsmasq:0.0.0

MAINTAINER Benoit TRAVERS <benoit.travers.fr@gmail.com>

# install openssh-server, vim and openjdk
RUN apt-get install -y openssh-server vim
RUN apt-get install -y openjdk-8-jdk

# move all configuration files into container
ADD files/* /usr/local/

# set jave environment variable
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $PATH:$JAVA_HOME/bin

#configure ssh free key access
RUN mkdir /var/run/sshd
RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P ''
RUN cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
RUN mv /usr/local/ssh_config ~/.ssh/config
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

#install hadoop
RUN curl -s http://apache.websitebeheerjd.nl/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz | tar -xz -C /usr/local/
RUN ln -s /usr/local/hadoop-2.6.0 /usr/local/hadoop
RUN mv /usr/local/bashrc ~/.bashrc
RUN mv /usr/local/hadoop-env.sh /usr/local/hadoop/etc/hadoop/hadoop-env.sh
