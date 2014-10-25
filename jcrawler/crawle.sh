#!/bin/sh

case "$1" in
        daily)
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -s crawlerRubyGems       >> gem.txt &
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -s crawlerPythonPip      >> pip.txt &
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -s crawlerR              >> r.txt &
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -s crawlerGitHub         >> github.txt &
                ;;
        gems)
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -d crawlerRubyGems 2 >> gem.txt &
                ;;
        pips)
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -d crawlerPythonPip 2 >> pip.txt &
                ;;
        github)
                java -server -jar target/crawler-1.0-SNAPSHOT-all.jar -d crawlerGitHub 2 >> github.txt &
                ;;
        *)
                echo "Usage: $0 {all, gems, maven}"
                ;;
esac
