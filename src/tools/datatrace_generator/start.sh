#!/usr/bin/octave -qf

#input: .mat file containing topology data and prefix data which is generated from topology generator
#example: ./start.sh ../topology_generator/datatraceInput.mat

arg_list = argv ();
matFile = arg_list{1};
source('eventGenerator.m')

eventGenerator(matFile)
