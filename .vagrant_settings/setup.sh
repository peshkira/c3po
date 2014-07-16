#!/bin/bash

##
# NOTES:
##

# [1] the main tasks of this script:
#     --> it installs c3po and assembles the command-line app
#     --> it installs and runs a mongodb server
#     --> it installs and runs the playframework

# [2] in case you're behind a proxy:
#     --> have a look at http://tmatilai.github.io/vagrant-proxyconf/
#     --> for some authenticated proxies the only working solution I've
#         found is cntlm; put a proxy_settings.conf in the root dir,
#         it will be found and copied over to /etc/cntlm.conf; in this
#         case maven settings.xml has to point to localhost and the port
#         specified in cntlm.conf, without username and password!

##
# variables to be set
##

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
OPEN_JDK=openjdk-7-jdk

PLAY_VERSION=play-2.0.4
PLAY_URL=http://download.playframework.org/releases/$PLAY_VERSION.zip


##
# function definitions
##

function updateSource {
    echo "[setup.sh] retrieving ubuntu updates.."
    apt-get update
}

function installJavaDevEnv {
    echo "[setup.sh] installing java dev environment"
	echo "[setup.sh] ..open jdk.."
	apt-get install -y $OPEN_JDK
}

function checkProxy {
    # in case you're behind a (e.g. nmlt) proxy, this is a way to have maven working
    if [ -f /vagrant/.vagrant_settings/proxy_settings.conf ]
    then
        echo "[setup.sh] found proxy_settings, installing cntlm.."
        apt-get install -y cntlm
        cp /vagrant/.vagrant_settings/proxy_settings.conf /etc/cntlm.conf
        service cntlm restart
        PROXY_HOST=127.0.0.1
        # copy the 'Listen' port-number from cntlm.conf to use as proxy port
        PROXY_PORT=`grep Listen /vagrant/.vagrant_settings/proxy_settings.conf |grep -o -P '\d*'`
        export HTTP_PROXY=$PROXY_HOST:$PROXY_PORT
    fi
}

function setupMaven {
    echo "[setupt.sh] installing maven.."
    apt-get install -y maven

    # checks whether there's a maven settings file in /vagrant/.vagrant_settings and copies it
    # over if so (in case of proxy settings, etc)

    if [ -f /vagrant/.vagrant_settings/maven_settings.xml ]
    then
        if [ ! -d /home/vagrant/.m2 ]
        then
            mkdir /home/vagrant/.m2 && chown vagrant:vagrant /home/vagrant/.m2
        fi
        echo "[setup.sh] found maven_settings, linking to the settings file.."
        ln -s /vagrant/.vagrant_settings/maven_settings.xml /home/vagrant/.m2/settings.xml
    fi
}

function createCliJar {
    echo "installing c3po with maven.."
    cd /vagrant
    sudo -u vagrant mvn clean install -DskipTests=true
    echo "assembling jar-with-dependencies"
    cd c3po-cmd
    sudo -u vagrant mvn assembly:assembly
}

function setupMongoDB {
    echo "[setup.sh] installing mongodb.."
    setupMongoDB_2_0_5
}

function setupMongoDB_2_0_5 {
    cd /home/vagrant
    echo "[setup.sh] installing mongodb 2.0.5 (manual install).."
    wget http://downloads.mongodb.org/linux/mongodb-linux-x86_64-2.0.5.tgz
    tar xvfz mongodb-linux-x86_64-2.0.5.tgz 
    rm mongodb-linux-x86_64-2.0.5.tgz 
    export PATH=$PATH:/home/vagrant/mongodb-linux-x86_64-2.0.5/bin/
    mkdir -p /data/db
    chown -R vagrant /data/db
    echo "[setup.sh] starting the mongodb server.."
    mongod & >> /home/vagrant/mongod.log
}

function ensureCorrectJavaVersion {
    echo [setup.sh] setting $JAVA_HOME as default
    echo setting java to $JAVA_HOME
    sudo update-alternatives --set java $JAVA_HOME/jre/bin/java
}

function installPlay {
    cd /home/vagrant
    echo "[setup.sh] installing the playframework"
    wget $PLAY_URL
    apt-get install -y unzip
    unzip $PLAY_VERSION.zip
    rm $PLAY_VERSION.zip
    chown -R vagrant /home/vagrant/$PLAY_VERSION
    export PATH=$PATH:/home/vagrant/$PLAY_VERSION
    echo export PATH=$PATH:/home/vagrant/$PLAY_VERSION >> /home/vagrant/.bashrc
    play -h
    echo "[setup.sh] if the play logo is displayed above and it complains about there being no play application it should be installed now"
}

function runPlay {
    echo "[setup.sh] running C3PO on Netty (playframework)"
    echo "[setup.sh] (1) compiling.."
    cd /vagrant/c3po-webapi

    PROXY_ARGS=""
    if [ $HTTP_PROXY ]; then
        PROXY_ARGS="-Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT"
        echo "[setup.sh] set proxy args to $PROXY_ARGS"
    fi

    play $PROXY_ARGS clean compile stage
    echo "[setup.sh] (2) starting the server.."
    ./target/start &
    echo "[setup.sh] play started, should be available at localhost:9000/c3po"
}

function installGit {
    echo "[setup.sh] installing git"
    apt-get install -y git tig
}

function installC3poEnv {
    echo "[setup.sh] installing C3PO environment"
}


##
# calling the functions..
##

updateSource
installJavaDevEnv
checkProxy
setupMaven
createCliJar
setupMongoDB
ensureCorrectJavaVersion
installPlay
runPlay
