#!/bin/bash

echo "RAILS_ENV=$RAILS_ENV
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production


echo "DB_PORT_27017_TCP_ADDR=$DB_PORT_27017_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "DB_PORT_27017_TCP_PORT=$DB_PORT_27017_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "MONGO_RS_2_ADDR=$MONGO_RS_2_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "MONGO_RS_2_PORT=$MONGO_RS_2_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "MONGO_RS_3_ADDR=$MONGO_RS_3_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "MONGO_RS_3_PORT=$MONGO_RS_3_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production


echo "RM_PORT_5672_TCP_ADDR=$RM_PORT_5672_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "RM_PORT_5672_TCP_PORT=$RM_PORT_5672_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production


echo "MC_PORT_11211_TCP_ADDR=$MC_PORT_11211_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "MC_PORT_11211_TCP_PORT=$MC_PORT_11211_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production


echo "ES_PORT_9200_TCP_ADDR=$ES_PORT_9200_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production

echo "ES_PORT_9200_TCP_PORT=$ES_PORT_9200_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_production)" > /mnt/crawl_j/crontab_production


/usr/bin/crontab /mnt/crawl_j/crontab_production
cron
tail -f /var/log/cron.log
