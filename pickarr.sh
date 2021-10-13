#!/bin/bash

mongod --config ./mongod.conf &
./pickarr &

wait -n
exit $?
