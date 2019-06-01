#!/bin/bash

ps -aux | grep 'zookeeper' | grep -v grep | awk '{print $2}' | xargs -r kill -s SIGINT
