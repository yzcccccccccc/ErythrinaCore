#!/bin/bash

# 检查是否提供了起始commit
if [ -z "$1" ]; then
  echo "Usage: $0 <start-commit>"
  exit 1
fi

START_COMMIT=$1

# 获取从指定起始commit开始的所有提交的增加和删除行数
git log $START_COMMIT..HEAD --pretty=tformat: --numstat | awk '
{
    added += $1;
    removed += $2;
}
END {
    printf "Total added lines: %d\nTotal removed lines: %d\nNet change: %d\n", added, removed, added - removed;
}'
