#!/bin/bash

# odpalenie w osobnych terminalach klientow laczacych sie do roznych serwerow 
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkCli.sh -server 127.0.0.1:2181
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkCli.sh -server 127.0.0.1:2182
gnome-terminal -- ./apache-zookeeper-3.5.5-bin/bin/zkCli.sh -server 127.0.0.1:2183
