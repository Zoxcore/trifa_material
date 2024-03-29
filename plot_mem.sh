#! /bin/bash
pp=$(jcmd|grep 'MainKt'|cut -d' ' -f1);while :; do grep -oP '^VmRSS:\s+\K\d+' /proc/$pp/status \
 | numfmt --from-unit Ki --to-unit Mi; sleep 1; done | ttyplot -u Mi
