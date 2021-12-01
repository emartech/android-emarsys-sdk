#!/usr/bin/env bash

echo Revoke relese with version: $1

git tag -d $1
git push --delete origin $1