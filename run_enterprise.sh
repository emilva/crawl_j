#!/bin/bash

echo "RAILS_ENV=$RAILS_ENV
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise


echo "DB_PORT_27017_TCP_ADDR=$DB_PORT_27017_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise

echo "DB_PORT_27017_TCP_PORT=$DB_PORT_27017_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise


echo "RM_PORT_5672_TCP_ADDR=$RM_PORT_5672_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise

echo "RM_PORT_5672_TCP_PORT=$RM_PORT_5672_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise


echo "MC_PORT_11211_TCP_ADDR=$MC_PORT_11211_TCP_ADDR
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise

echo "MC_PORT_11211_TCP_PORT=$MC_PORT_11211_TCP_PORT
$(/bin/cat /mnt/crawl_j/crontab_enterprise)" > /mnt/crawl_j/crontab_enterprise



/usr/bin/crontab /mnt/crawl_j/crontab_enterprise
cron
tail -f /var/log/cron.log
