#!/bin/bash

killall -9 gnrsd lnrsd gbench
rm -f /var/log/gnrsd.log /var/log/lnrsd.log /var/log/gnrsd-client.log*
