#!/system/bin/sh
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 1
done
settings put secure bootanim.real_home_ready 1
for i in 1 2 3 4 5 6 7 8 9 10; do
    settings put secure bootanim.real_home_ready 1
    sleep 2
done
