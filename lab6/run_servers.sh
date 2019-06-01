#!/bin/bash

# usuniecie starych danych i stworzenie folderow na dane na nowo
rm -rf zk1 zk2 zk3
mkdir zk1 zk2 zk3
printf "1" > zk1/myid
printf "2" > zk2/myid
printf "3" > zk3/myid

# odpalenie w osobnych terminalach serwerow korzystajacych z konkretnych konfigow
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkServer.sh start-foreground conf1.cfg
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkServer.sh start-foreground conf2.cfg
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkServer.sh start-foreground conf3.cfg
