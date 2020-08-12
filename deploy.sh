#!/usr/bin/env bash

set -e

APP=video_reply_telegram_bot2
USER=apps1
SERV=prod.cmlteam.com

#JAVA_HOME=/home/xonix/soft/graalvm-ce-java11-20.1.0
JAVA_HOME=/home/xonix/.sdkman/candidates/java/current

echo
echo "BUILD..."
echo

./mvnw clean package -DskipTests

echo
echo "DEPLOY..."
echo

scp $APP.conf target/$APP.jar $USER@$SERV:~/

echo
echo "RESTART..."
echo

ssh $USER@$SERV "
if [[ ! -f /etc/init.d/$APP ]]
then
    sudo ln -s /home/$USER/$APP.jar /etc/init.d/$APP
    sudo update-rc.d $APP defaults 99
fi
sudo /etc/init.d/$APP restart
sleep 20
tail -n 200 /var/log/$APP.log
"
