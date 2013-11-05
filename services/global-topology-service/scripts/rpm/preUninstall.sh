#!/bin/sh
service="gts"
serviceAccount="gts"
if [ $1 -eq 0 ]; then #package is being erased not upgraded
    /sbin/service $service stop >/dev/null 2>&1
    /sbin/chkconfig --del $service
    /usr/sbin/userdel $serviceAccount
fi