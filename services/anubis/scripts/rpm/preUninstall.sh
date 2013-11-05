#!/bin/sh
service="anubis"
serviceAccount="anubis"
if [ $1 -eq 0 ]; then #package is being erased not upgraded
    /sbin/service $service stop >/dev/null 2>&1
    /sbin/chkconfig --del $service
    /usr/sbin/userdel $serviceAccount
fi