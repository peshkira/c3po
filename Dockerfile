# syntax = docker/dockerfile:1.2

FROM ubuntu:16.04

RUN rm -f /etc/apt/apt.conf.d/docker-clean




RUN --mount=type=cache,target=/var/cache/apt --mount=type=cache,target=/var/lib/apt \
    apt-get update \
    && apt-get -y install apt-transport-https \
    && apt-get update \
    && apt-get install -yqq --no-install-recommends openjdk-8-jdk screen \
    && rm -rf /var/lib/apt/lists/*

COPY . /c3po
WORKDIR /c3po

RUN --mount=type=cache,target=/root/.ivy2 --mount=type=cache,target=/root/.sbt ["/bin/bash", "-c", "./sbt clean compile assembly dist"]





FROM ubuntu:16.04

RUN rm -f /etc/apt/apt.conf.d/docker-clean
RUN --mount=type=cache,target=/var/cache/apt --mount=type=cache,target=/var/lib/apt \
    apt-get update \
    && apt-get -y install apt-transport-https \
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6 \
    && echo "deb [ arch=amd64,arm64 ] http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.4 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.4.list \
    && apt-get update \
    && apt-get install -yqq --no-install-recommends mongodb-org openjdk-8-jdk screen unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /c3po
WORKDIR /c3po

COPY --from=0 /c3po/c3po-cmd/target/scala-2.11/c3po-cmd-assembly-0.1-SNAPSHOT.jar ./c3po-cmd.jar
COPY --from=0 /c3po/c3po-webapi/target/universal/c3po-webapi-0.1-SNAPSHOT.zip ./c3po-webapi.zip
RUN unzip c3po-webapi.zip


EXPOSE 9000

RUN echo "#!/bin/bash \n\
set -e \n\
env \n\
echo 'Now, C3PO will import metadata from FITS files..' \n\
mkdir -p /data/db \n\
nohup mongod --dbpath /data/db & \n\
java -jar /c3po/c3po-cmd.jar gather -c indocker -i /data/FITS -r \n\
ls \n\
pwd \n\
./c3po-webapi-0.1-SNAPSHOT/bin/c3po-webapi -Dplay.crypto.secret=abcdefghijk \n\
" >> /import.sh

ENTRYPOINT ["bash","/import.sh"]
