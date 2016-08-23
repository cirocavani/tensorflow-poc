#!/bin/bash
set -e

service sshd start
chkconfig sshd on

adduser -m -d /home/hadoop hadoop
passwd -d hadoop

mkdir -p /data/hadoop
chown hadoop:hadoop /data/hadoop

mkdir -p /opt/hadoop-2.5.2/logs
chown hadoop:hadoop /opt/hadoop-2.5.2/logs

su - hadoop -c "ssh-keygen -C hadoop -P '' -f ~/.ssh/id_rsa"
su - hadoop -c "cp ~/.ssh/{id_rsa.pub,authorized_keys}"
su - hadoop -c "ssh-keyscan `hostname` >> ~/.ssh/known_hosts"
su - hadoop -c "ssh-keyscan localhost >> ~/.ssh/known_hosts"
su - hadoop -c "ssh-keyscan 127.0.0.1 >> ~/.ssh/known_hosts"
su - hadoop -c "ssh-keyscan 0.0.0.0 >> ~/.ssh/known_hosts"

echo "<configuration>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/data/hadoop</value>
    </property>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://$(hostname)</value>
    </property>
</configuration>" > /opt/hadoop-2.5.2/etc/hadoop/core-site.xml

echo "<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <property>
        <name>dfs.blocksize</name>
        <value>8M</value>
    </property>
</configuration>" > /opt/hadoop-2.5.2/etc/hadoop/hdfs-site.xml

su - hadoop -c 'hdfs namenode -format'

su - hadoop -c 'start-dfs.sh'

su - hadoop -c 'hdfs dfs -mkdir -p /user/hadoop'

su - hadoop -c 'stop-dfs.sh'

service sshd stop
