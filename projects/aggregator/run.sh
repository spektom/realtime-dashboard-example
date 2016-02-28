#!/bin/sh

cd /projects/aggregator
exec sbt -mem 1024 run
