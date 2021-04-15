#!/bin/sh
docker network create mynetwork
docker build -t mockbanksystem mock-bank-system
docker run -d --network=mynetwork --name=mockbanksystem mockbanksystem
docker build --network=mynetwork -t decentralizediso20022 decentralizediso20022
docker run --env PROFILE=internal -d --network=mynetwork --name=decentralizediso20022internal decentralizediso20022
docker run --env PROFILE=external -d --network=mynetwork --name=decentralizediso20022external decentralizediso20022
