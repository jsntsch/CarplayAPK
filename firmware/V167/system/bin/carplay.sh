#! /system/bin/sh

ip -6 route add fe80::/64 dev usb0 proto static table local_network
#ifconfig usb0 up
