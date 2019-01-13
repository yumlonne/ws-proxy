#!/bin/bash

export MODE=develop
export CMD=~jetty:start
export PROJECT=scalatra_first_app

docker run -it -e MODE -e CMD -e PROJECT -p 80:8080 -v `pwd`:/root/project/ws-proxy ws-proxy:1.0
