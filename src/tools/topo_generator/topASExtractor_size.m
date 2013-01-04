% Analyse Number of /32 Addresses in each AS, extract the largest 200 ASes,
% and generate the topology
%************************************************************
clc;
close;
clear;

inputFile = csvread('ASNumDataMod-2_1_11.csv');
numPrefix = length(inputFile);
%addrCount is used to store number of /32 addresses
addrCount = zeros(max(inputFile(:,1)),1);

inputFile = sortrows(inputFile,[1 2 3 4 5 -6]);
addrCount(inputFile(1,1)) = addrCount(inputFile(1,1)) + 2^(32 - inputFile(1,6));
for i = 2:numPrefix
    if isequal(inputFile(i,1:5),inputFile(i-1,1:5)) == 0
        addrCount(inputFile(i,1)) = addrCount(inputFile(i,1)) + 2^(32 - inputFile(i,6));
    end
end
ASsizeInfo = [find(addrCount>0) addrCount(find(addrCount>0))];

%sort the AS based on its prefix size
ASsizeInfo = sortrows(ASsizeInfo, -2);
asTop200 = ASsizeInfo(1:200,1);

%prefixFile = fopen('prefix_2_1_11_adjusted.data');
Top200Prefix = zeros(0,6);
Top200PrefixNum = 0;

%linedata = sscanf(fgetl(prefixFile), '%s %d');
%fscan(prefixFile, '%s %d', [prefix ASid]);

for i = 1:numPrefix
    if(isempty(find(asTop200==inputFile(i,1)))==0)
        Top200PrefixNum = Top200PrefixNum + 1;
        Top200Prefix(Top200PrefixNum,:) = inputFile(i,:);
    end
end

%find links among those 200 ASes
topologyData = importdata('topology_intra_adjusted.data');
Top200Links = zeros(0,3);
linkNum = 0;

for i= 1:length(topologyData)
    if(isempty(find(asTop200==topologyData(i,1)))==0 && isempty(find(asTop200==topologyData(i,2)))==0)
        linkNum = linkNum +1;
        Top200Links(linkNum,:) = topologyData(i,:);
    end
end        
