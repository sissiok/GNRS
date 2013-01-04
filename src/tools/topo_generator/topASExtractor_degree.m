% analyze the degree of each AS, extract the first 200 ASes, and generate
% the topology of those 200 ASes (delay, prefix)
clear all;
close all;
clc;

topologyData = importdata('topology_intra_adjusted.data');
asInfo = unique([topologyData(:,1);topologyData(:,2)]);
numAS = length(asInfo);
asInfo = [asInfo zeros(numAS,1)];  %[AS_ID, degree]

for i = 1:length(asInfo)
    asInfo(i,2) = sum(topologyData(:,1) == asInfo(i)) + sum(topologyData(:,2) == asInfo(i));
%    if mod(i,1000) == 0
%        i
%    end
end
asInfo = sortrows(asInfo,-2);

asTop200 = asInfo(1:200,1);
Top200Links = zeros(0,3);
linkNum = 0;

%find links among those 200 ASes
for i= 1:length(topologyData)
    if(isempty(find(asTop200==topologyData(i,1)))==0 && isempty(find(asTop200==topologyData(i,2)))==0)
        linkNum = linkNum +1;
        Top200Links(linkNum,:) = topologyData(i,:);
    end
end

%find the corresponding prefix of those top 200 ASes
inputFile = csvread('ASNumDataMod-2_1_11.csv');
numPrefix = length(inputFile);
Top200Prefix = zeros(0,6);
Top200PrefixNum = 0;

for i = 1:numPrefix
    if(isempty(find(asTop200==inputFile(i,1)))==0)
        Top200PrefixNum = Top200PrefixNum + 1;
        Top200Prefix(Top200PrefixNum,:) = inputFile(i,:);
    end
end

