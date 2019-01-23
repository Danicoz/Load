#!/bin/bash
LANG=zh_CN.GB18030
export LANG

PID=`ps -ef|grep -v grep|grep LoadUtil|awk '{print $2}'`
if [ -z $PID ];then
    java -DLoadUtil -Xms256m -Xmx2048m  -cp ./lib/LoadUtil.jar:./lib/commons-dbutils-1.6.jar:./lib/druid-1.0.26.jar:./lib/jdom.jar:./lib/logback-access-1.0.13.jar:./lib/logback-classic-1.0.13.jar:./lib/logback-core-1.0.13.jar:./lib/mysql-connector-java-5.1.13-bin.jar:./lib/ojdbc6.jar:./lib/slf4j-api-1.7.12.jar com.danicoz.Main  >/dev/null 2>err.log &
else 
    echo "程序正在运行,请使用停止脚本后再启动!"
fi
