#!/bin/bash

echo "--"
echo $RAILS_ENV
echo $RAILS_ENV              >> /etc/cron.d/versioneye
echo $DB_PORT_27017_TCP_ADDR
echo $DB_PORT_27017_TCP_ADDR >> /etc/cron.d/versioneye
echo "--"
