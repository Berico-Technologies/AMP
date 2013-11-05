#!/bin/sh
service="gts"
serviceAccount="gts"
group="ampere"
ampere_logs="/var/log/ampere/"
gts_logs="$ampere_logs/$service"
home="/opt/ampere/$service"

grep -i $group /etc/group
if [ ! $? -eq 0 ]; then
	/usr/sbin/groupadd $group
fi

mkdir -p $gts_logs
chgrp -R $group $ampere_logs
chmod -R 775 $ampere_logs
useradd -r -M -G $group -d $home $serviceAccount
/sbin/chkconfig --add $service
