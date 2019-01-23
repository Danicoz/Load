#!/bin/bash
LANG=zh_CN.GB18030
export LANG
PID=`ps -ef|grep -v grep|grep LoadUtil |awk '{print $2}'`
if [ ! -z $PID ]; then
    kill -9 $PID
else
    echo "³ÌÐòÎ´Æô¶¯!"
fi
