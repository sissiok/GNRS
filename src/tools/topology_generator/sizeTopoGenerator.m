function [ asID, asLinks, asPrefix ] = sizeTopoGenerator( asNum )
% Analyse Number of /32 Addresses in each AS, extract the largest 200 ASes,
% and generate the topology    

%inputFile = csvread('ASNumDataMod-2_1_11.csv');
load ASPrefixData;
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
asID = ASsizeInfo(1:asNum,1);

%prefixFile = fopen('prefix_2_1_11_adjusted.data');
asPrefix = zeros(0,6);
asPrefixNum = 0;

%linedata = sscanf(fgetl(prefixFile), '%s %d');
%fscan(prefixFile, '%s %d', [prefix ASid]);

for i = 1:numPrefix
    if(isempty(find(asID==inputFile(i,1)))==0)
        asPrefixNum = asPrefixNum + 1;
        asPrefix(asPrefixNum,:) = inputFile(i,:);
    end
end

%find links among those asNum ASes
%topologyData = importdata('topology_intra_adjusted.data');
load topologyData;
asLinks = zeros(0,3);
linkNum = 0;

for i= 1:length(topologyData)
    if(isempty(find(asID==topologyData(i,1)))==0 && isempty(find(asID==topologyData(i,2)))==0)
        linkNum = linkNum +1;
        asLinks(linkNum,:) = topologyData(i,:);
    end
end            

end

