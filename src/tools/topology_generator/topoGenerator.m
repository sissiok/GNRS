function [ asID, asLinks, asPrefix ] = topoGenerator( method, asNum )
% this function generates topology files (topology file containing links,
% prefix file) based on different methods
% method=0: extract the top asNum ASes from DIMES dataset based on degree
% method=1: extract the top asNum ASes from DIMES dataset based on AS size
% (prefix)
% method=2: generate a synthetic AS-level topology based on jellyfish model

if(method==0)
    % analyze the degree of each AS, extract the first asNum ASes, and generate
    % the topology of those asNum ASes (delay, prefix)
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

    asID = asInfo(1:asNum,1);
    asLinks = zeros(0,3);
    linkNum = 0;

    %find links among those asNum ASes
    for i= 1:length(topologyData)
        if(isempty(find(asID==topologyData(i,1)))==0 && isempty(find(asID==topologyData(i,2)))==0)
            linkNum = linkNum +1;
            asLinks(linkNum,:) = topologyData(i,:);
        end
    end

    %find the corresponding prefix of those top asNum ASes
    inputFile = csvread('ASNumDataMod-2_1_11.csv');
    numPrefix = length(inputFile);
    asPrefix = zeros(0,6);
    asPrefixNum = 0;

    for i = 1:numPrefix
        if(isempty(find(asID==inputFile(i,1)))==0)
            asPrefixNum = asPrefixNum + 1;
            asPrefix(asPrefixNum,:) = inputFile(i,:);
        end
    end
    
elseif(method==1)
    % Analyse Number of /32 Addresses in each AS, extract the largest 200 ASes,
    % and generate the topology    
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
    topologyData = importdata('topology_intra_adjusted.data');
    asLinks = zeros(0,3);
    linkNum = 0;

    for i= 1:length(topologyData)
        if(isempty(find(asID==topologyData(i,1)))==0 && isempty(find(asID==topologyData(i,2)))==0)
            linkNum = linkNum +1;
            asLinks(linkNum,:) = topologyData(i,:);
        end
    end            

elseif(method==2)
    
end

%asID mapping: map asID to a continuous integer space starting from 1. For
%orbit grid evaluation purpose
for i=1:length(asLinks)
    asLinks(i,1)=find(asID==asLinks(i,1));
    asLinks(i,2)=find(asID==asLinks(i,2));
end
for i=1:length(asPrefix)
    asPrefix(i,1)=find(asID==asPrefix(i,1));
end

end
