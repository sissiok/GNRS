#!/usr/bin/octave -qf

#input: .mat file containing topology data and prefix data which is generated from topology generator

arg_list = argv ();
matFile = arg_list{1};
source('eventGenerator.m')

eventGenerator(matFile)
