#!/bin/bash

echo "RAILS_ENV=$RAILS_ENV
$(/bin/cat crontab_enterprise)" > crontab_enterprise"


echo "DB_PORT_27017_TCP_ADDR=$DB_PORT_27017_TCP_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "DB_PORT_27017_TCP_PORT=$DB_PORT_27017_TCP_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "MONGO_RS_2_ADDR=$MONGO_RS_2_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "MONGO_RS_2_PORT=$MONGO_RS_2_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "MONGO_RS_3_ADDR=$MONGO_RS_3_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "MONGO_RS_3_PORT=$MONGO_RS_3_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"


echo "RM_PORT_5672_TCP_ADDR=$RM_PORT_5672_TCP_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "RM_PORT_5672_TCP_PORT=$RM_PORT_5672_TCP_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"


echo "MC_PORT_11211_TCP_ADDR=$MC_PORT_11211_TCP_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "MC_PORT_11211_TCP_PORT=$MC_PORT_11211_TCP_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"


echo "ES_PORT_9200_TCP_ADDR=$ES_PORT_9200_TCP_ADDR
$(/bin/cat crontab_enterprise)" > crontab_enterprise"

echo "ES_PORT_9200_TCP_PORT=$ES_PORT_9200_TCP_PORT
$(/bin/cat crontab_enterprise)" > crontab_enterprise"


/usr/bin/crontab /mnt/crawl_j/crontab_enterprise
cron
tail -f /var/log/cron.log
