#!/bin/sh -e
sed -e '1,/^exit$/d' "$0" | tar xzf - && ./tensorflow/bin/run
exit
