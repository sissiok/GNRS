#!/usr/bin/octave -qf

#input: path to prefix.data and topology.data

arg_list = argv ();
prefixData = arg_list{1};
topoData = arg_list{2};
source('eventGenerator.m')

eventGenerator(prefixData, topoData)
