#!/bin/bash
PREV=$1
CURR=$2
FILE=$3

if [ -z "$PREV" ] || [ -z "$CURR" ]; then
  echo "Please provide the two tags for the changelog"
  exit 1
fi

if [ -z "$FILE" ]; then
  echo "Please provide a file to write to"
  exit 1
fi

if [ ! -f $FILE ]; then
  touch "$FILE"
fi

git log $PREV..$CURR --pretty=format:'<li>%s &bull; <a href="https://github.com/peshkira/c3po/commit/%H">view</a></li>' | grep -v Merge |cat - $FILE > chlog.tmp && mv chlog.tmp $FILE
echo "<br/><b>Version $CURR</b><br/><br/>" | cat - $FILE > chlog.tmp && mv chlog.tmp $FILE
