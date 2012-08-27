#!/bin/bash

cat temp | grep -v avg-cpu | grep -v Linux > iostat.data
