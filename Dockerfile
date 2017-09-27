FROM ubuntu:latest
RUN apt-get update \
    && apt-get -y install apt-transport-https \
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6 \
    && echo "deb [ arch=amd64,arm64 ] http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.4 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.4.list \
    && echo "deb https://dl.bintray.com/sbt/debian /" | tee /etc/apt/sources.list.d/sbt.list \
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
    && apt-get update \
    && apt-get -y install mongodb-org git sbt openjdk-8-jdk screen

RUN echo "#!/bin/bash \n\

set -e \n\
mkdir -p /c3po \n\
git clone -b dev https://github.com/datascience/c3po /c3po \n\
cd /c3po \n\
sbt clean compile assembly \n\
" >> /locknload.sh
RUN bash /locknload.sh


EXPOSE 9000

RUN echo "#!/bin/bash \n\
set -e \n\
echo 'Now, C3PO will import metadata from FITS files..' \n\
mkdir -p /data/db \n\
nohup mongod --dbpath /data/db & \n\
java -jar /c3po/c3po-cmd/target/scala-2.11/c3po-cmd-assembly-0.1-SNAPSHOT.jar gather -c dockerised -i /data/FITS -r \n\
cd /c3po \n\
sbt \"project c3po-webapi\" run \n\
" >> /import.sh

ENTRYPOINT ["bash","/import.sh"]
