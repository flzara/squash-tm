#! /bin/sh
mvn -N versions:update-child-modules
hg commit -m"Upgraded versions after release" -u jenkins
