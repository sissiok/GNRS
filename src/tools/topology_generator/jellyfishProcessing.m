% Generate the required data for Jellyfish modeling
clear all 
close all

topologyData = importdata('topology_intra_adjusted.data');

asInfo = unique([topologyData(:,1);topologyData(:,2)]);
numAS = length(asInfo);
asInfo = [asInfo zeros(numAS,1)];

% All links are bidirectional; 1 entry for both directions
% Replacing latency column with connectivity degree info

for i = 1:length(asInfo)
    asInfo(i,2) = sum(topologyData(:,1) == asInfo(i)) + sum(topologyData(:,2) == asInfo(i));
    if mod(i,1000) == 0
        i
    end
end
asInfo = sortrows(asInfo,-2);

% Form the Core clique
clique = asInfo(1,1);
maxPosClique = 70;

% h = waitbar(0,'Analyzing Topology');
for i = 2:maxPosClique
%     waitbar(i/maxPosClique,h,'Analyzing Topology')
    numCnx = 0;
    for j = 1:length(clique)
        if sum(topologyData(:,1) == asInfo(i,1) & topologyData(:,2) == clique(j)) + ...
                sum(topologyData(:,2) == asInfo(i,1) & topologyData(:,1) == clique(j)) > 0
            numCnx = numCnx + 1;
        else
            break;
        end
    end
    if numCnx == length(clique)
        clique(end+1) = asInfo(i,1);
    end
end

numClique = length(clique)

% Fill in the Shells and Hangs
maxShells = 7;
maxAsPerShell = 14000;
maxHangs = 7;
maxAsPerHang = 10000;

shellMem = zeros(maxShells,maxAsPerShell);
hangMem = zeros(maxHangs,maxAsPerHang);

asLeft = asInfo;
for i = 1:numClique
    asLeft(asLeft (:,1) == clique(i),:) = [];
end

sh1Counter = 1;
hang1Counter = 1;
toRemove = zeros(1,maxAsPerShell+maxAsPerHang);
removeCount = 1;
for i = 1:length(asLeft)
    tmp1 = find(topologyData(:,1) == asLeft(i,1));
    tmp2 = find(topologyData(:,2) == asLeft(i,1));
    for j = 1:numClique
        if sum(topologyData(tmp1,2) == clique(j)) + ...
                sum(topologyData(tmp2,1) == clique(j)) > 0
            if asLeft(i,2) == 1
                hangMem(1,hang1Counter) = asLeft(i,1);
                hang1Counter = hang1Counter + 1;
            else
                shellMem(1,sh1Counter) = asLeft(i,1);
                sh1Counter = sh1Counter + 1;
            end
            toRemove(removeCount) = i;
            removeCount = removeCount + 1;
            break;
        end
    end
    if mod(i,1000) == 0
        i
    end
end
asLeft(toRemove(1:removeCount-1),:) = [];
numel(find(shellMem(1,:)))

for shIndex = 2:maxShells
    shIndex
    shCounter = 1;
    hangCounter = 1;
    toRemove = zeros(1,maxAsPerShell+maxAsPerHang);
    removeCount = 1;

    for i = 1:length(asLeft)
    tmp1 = find(topologyData(:,1) == asLeft(i,1));
    tmp2 = find(topologyData(:,2) == asLeft(i,1));
        for j = 1:numel(find(shellMem(shIndex-1,:)))
            if sum(topologyData(tmp1,2) == shellMem(shIndex-1,j)) + ...
                    sum(topologyData(tmp2,1) == shellMem(shIndex-1,j)) > 0
                if asLeft(i,2) == 1
                    hangMem(shIndex,hangCounter) = asLeft(i,1);
                    hangCounter = hangCounter + 1;
                else
                    shellMem(shIndex,shCounter) = asLeft(i,1);
                    shCounter = shCounter + 1;
                end
                toRemove(removeCount) = i;
                removeCount = removeCount + 1;
                break;
            end
        end
        if mod(i,1000) == 0
            i
        end
    end
    asLeft(toRemove(1:removeCount-1),:) = [];
    numel(find(shellMem(shIndex,:)))
end

% peerLinks = zeros(maxShells,1);

%save('poPjellyfishResultCaida.mat','clique','shellMem','hangMem');

% disp('Counting Peer Links');
% for shIndex = 1:maxShells
%     shIndex
%     numElements = numel(find(shellMem(shIndex,:)));
%     linkCount = 0;
%     for j = 1:numElements
%         tmp1 = find(topologyData(:,1) == shellMem(shIndex,j));
%         tmp2 = find(topologyData(:,2) == shellMem(shIndex,j));
%         for k = j + 1 : numElements
%             linkCount = linkCount + sum(topologyData(tmp1,2) == shellMem(k)) + ...
%                 sum(topologyData(tmp2,1) == shellMem(k));
%         end
%     end
%     peerLinks(shIndex) = linkCount;
% end
%  
% 
% save('poPjellyfishResultMod2.mat','clique','shellMem','hangMem','peerLinks');



% process topologyData to map AS to each layer: replace the first two
% columns in the topologyData with layer ID
% layer ID:
% clique: 0; shell-1: 1; shell-2: 2; shell-3: 3
% hang-0: 4; hang-1: 5; hang-2: 6; hang-3: 7
for i=1:length(topologyData)
    for j=1:2
        if(isempty(find(clique==topologyData(i,j)))==0)
            topologyData(i,j)=0;
        elseif(isempty(find(shellMem(1,:)==topologyData(i,j)))==0)
            topologyData(i,j)=1;
        elseif(isempty(find(shellMem(2,:)==topologyData(i,j)))==0)
            topologyData(i,j)=2;
        elseif(isempty(find(shellMem(3,:)==topologyData(i,j)))==0)
            topologyData(i,j)=3;
        elseif(isempty(find(hangMem(1,:)==topologyData(i,j)))==0)
            topologyData(i,j)=4;
        elseif(isempty(find(hangMem(2,:)==topologyData(i,j)))==0)
            topologyData(i,j)=5;
        elseif(isempty(find(hangMem(3,:)==topologyData(i,j)))==0)
            topologyData(i,j)=6;           
        elseif(isempty(find(hangMem(4,:)==topologyData(i,j)))==0)
            topologyData(i,j)=7;            
        end
    end
    if mod(i,1000) == 0
        i
    end
end

for i=1:length(topologyData)
    if(topologyData(i,1) > topologyData(i,2))
        temp = topologyData(i,1);
        topologyData(i,1) = topologyData(i,2);
        topologyData(i,2) = temp;
    end
end

topologyData = sortrows(topologyData,[1 2]);

%layerLinks: different types of links for each column:
% column 1: shell-0  shell-0
% column 2: shell-0  shell-1
% column 3: shell-0  hang-0
% column 4: shell-1  shell-1
% column 5: shell-1  shell-2
% column 6: shell-1  hang-1
% column 7: shell-2  shell-2
% column 8: shell-2  shell-3
% column 9: shell-2  hang-2
% column 10: shell-3  hang-3
layerLinks = zeros(90000, 10);
delim = zeros(1, 11);
delim(1,1)=0;
delim(1,11)=length(topologyData);
count = 1;

for i=2:length(topologyData)
    if(topologyData(i,1)==topologyData(i-1,1) && topologyData(i,2)==topologyData(i-1,2))
        ;
    else
        count = count+1;
        delim(count)=i-1;
    end
end

for i=2:length(delim)
    %unit: ms
    layerLinks(1:delim(i)-delim(i-1),i-1) = round(topologyData(delim(i-1)+1:delim(i),3)/1000);
end

% plot CDF of link delay in each layer
[y,x] = ecdf(topologyData(:,3)/1000);

figure
[y1,x1] = ecdf(layerLinks(find(layerLinks(:,1)),1));
[y2,x2] = ecdf(layerLinks(find(layerLinks(:,4)),4));
[y3,x3] = ecdf(layerLinks(find(layerLinks(:,7)),7));
f = semilogx(x,y);
hold on;
f1 = semilogx(x1,y1);
hold on
f2 = semilogx(x2,y2);
f3 = semilogx(x3,y3);
%f1 = cdfplot(layerLinks(find(layerLinks(:,1)),1));
%f2 = cdfplot(layerLinks(find(layerLinks(:,4)),4));
%f3 = cdfplot(layerLinks(find(layerLinks(:,7)),7));
set(f,'color','k','LineWidth',2);
set(f1,'color','r','LineWidth',2);
set(f2,'color','b','LineWidth',2);
set(f3,'color','g','LineWidth',2);
%axis([0 4000 0 1]);
xlabel('link delay (ms) (log scale)');
ylabel('fraction');
title('CDF of peer link delay within shell');
legend('Overall Internet','shell-0','shell-1','shell-2');

figure
[y1,x1] = ecdf(layerLinks(find(layerLinks(:,2)),2));
[y2,x2] = ecdf(layerLinks(find(layerLinks(:,5)),5));
[y3,x3] = ecdf(layerLinks(find(layerLinks(:,8)),8));
f = semilogx(x,y);
hold on;
f1 = semilogx(x1,y1);
hold on
f2 = semilogx(x2,y2);
f3 = semilogx(x3,y3);
% f1 = cdfplot(layerLinks(find(layerLinks(:,2)),2));
% f2 = cdfplot(layerLinks(find(layerLinks(:,5)),5));
% f3 = cdfplot(layerLinks(find(layerLinks(:,8)),8));
set(f,'color','k','LineWidth',2);
set(f1,'color','r','LineWidth',2);
set(f2,'color','b','LineWidth',2);
set(f3,'color','g','LineWidth',2);
%axis([0 4000 0 1]);
xlabel('link delay (ms) (log scale)');
ylabel('fraction');
title('CDF of transit link delay between two shells');
legend('Overall Internet','shell-0 <-> shell-1','shell-1 <-> shell-2','shell-2 <-> shell-3');

figure
[y1,x1] = ecdf(layerLinks(find(layerLinks(:,3)),3));
[y2,x2] = ecdf(layerLinks(find(layerLinks(:,6)),6));
[y3,x3] = ecdf(layerLinks(find(layerLinks(:,9)),9));
[y4,x4] = ecdf(layerLinks(find(layerLinks(:,10)),10));
f = semilogx(x,y);
hold on;
f1 = semilogx(x1,y1);
hold on
f2 = semilogx(x2,y2);
f3 = semilogx(x3,y3);
f4 = semilogx(x4,y4);
% f1 = cdfplot(layerLinks(find(layerLinks(:,3)),3));
% f2 = cdfplot(layerLinks(find(layerLinks(:,6)),6));
% f3 = cdfplot(layerLinks(find(layerLinks(:,9)),9));
% f4 = cdfplot(layerLinks(find(layerLinks(:,10)),10));
set(f,'color','k','LineWidth',2);
set(f1,'color','r','LineWidth',2);
set(f2,'color','b','LineWidth',2);
set(f3,'color','m','LineWidth',2);
set(f4,'color','g','LineWidth',2);
%axis([0 500 0 1]);
xlabel('link delay (ms) (log scale)');
ylabel('fraction');
title('CDF of transit link delay between shell and hang');
legend('Overall Internet','shell-0 <-> hang-0','shell-1 <-> hang-1','shell-2 <-> hang-2','shell-3 <-> hang-3'); 



% process prefixData to map AS to each layer
inputFile = csvread('ASNumDataMod-2_1_11.csv');
numPrefix = length(inputFile);
%addrCount is used to store number of /32 addresses
addrCount = zeros(max(inputFile(:,1)),1);

inputFile = sortrows(inputFile,[1 2 3 4 5 -6]);
addrCount(inputFile(1,1)) = addrCount(inputFile(1,1)) + 2^(32 - inputFile(1,6));
for i = 2:numPrefix
    if isequal(inputFile(i,1:5),inputFile(i-1,1:5)) == 0
        addrCount(inputFile(i,1)) = addrCount(inputFile(i,1)) + 2^(32 - inputFile(i,6));
    else
        addrCount(inputFile(i,1)) = addrCount(inputFile(i,1)) + 2^(32 - inputFile(i,6)) - 2^(32 - inputFile(i-1,6));        
    end
end
ASsizeInfo = [find(addrCount>0) addrCount(find(addrCount>0))];

layerASsizeInfo = ASsizeInfo;
% layer ID:
% clique: 0; shell-1: 1; shell-2: 2; shell-3: 3
% hang-0: 4; hang-1: 5; hang-2: 6; hang-3: 7
for i=1:length(layerASsizeInfo)
    if(isempty(find(clique==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=0;
    elseif(isempty(find(shellMem(1,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=1;
    elseif(isempty(find(shellMem(2,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=2;
    elseif(isempty(find(shellMem(3,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=3;
    elseif(isempty(find(hangMem(1,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=4;
    elseif(isempty(find(hangMem(2,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=5;
    elseif(isempty(find(hangMem(3,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=6;           
    elseif(isempty(find(hangMem(4,:)==layerASsizeInfo(i,1)))==0)
        layerASsizeInfo(i,1)=7;            
    end
end
layerASsizeInfo = sortrows(layerASsizeInfo,1);

%layerSizes: different types of links for each column:
% column 1: shell-0
% column 2: shell-1
% column 3: shell-2
% column 4: shell-3
% column 5: hang-0
% column 6: hang-1
% column 7: hang-2
% column 8: hang-3
layerSizes = zeros(25000, 8);
delim = zeros(1,9);
delim(1,1)=0;
delim(1,9)=length(layerASsizeInfo);
count = 1;

for i=2:length(layerASsizeInfo)
    if(layerASsizeInfo(i,1)==layerASsizeInfo(i-1,1))
        ;
    else
        count = count+1;
        delim(count)=i-1;
    end
end

for i=2:length(delim)
    layerSizes(1:delim(i)-delim(i-1),i-1) = layerASsizeInfo(delim(i-1)+1:delim(i),2);
end

% plot CDF of AS size in overall Internet and each layer
[y0,x0] = ecdf(ASsizeInfo(:,2));
figure
f = semilogx(x0,y0);
hold on;
set(f,'color','k','LineWidth',2);
for i=1:length(delim)-1
    [y,x] = ecdf(layerSizes(find(layerSizes(:,i)),i));
    f = semilogx(x,y);
    hold on
    %f = cdfplot(layerSizes(find(layerSizes(:,i)),i));
    xlabel('size (log scale)');
    ylabel('fraction');
    if i==1
        set(f,'color','r','LineWidth',2);
    elseif i==2
        set(f,'color','b','LineWidth',2);
    elseif i==3
        set(f,'color','m','LineWidth',2);
    elseif i==4
        set(f,'color','g','LineWidth',2);
        axis([0 6*10^7 0 1]);
        title('CDF of AS IP space size in shell');
        legend('Overall Internet','shell-0','shell-1','shell-2','shell-3');
        
        figure
        f = semilogx(x0,y0);
        hold on;
        set(f,'color','k','LineWidth',2);
    elseif i==5  
        set(f,'color','r','LineWidth',2);
    elseif i==6  
        set(f,'color','b','LineWidth',2);
    elseif i==7
        set(f,'color','m','LineWidth',2);
    elseif i==8
        set(f,'color','g','LineWidth',2);
        axis([0 2*10^6 0 1]);
        title('CDF of AS IP space size in hang');
        legend('Overall Internet','hang-0','hang-1','hang-2','hang-3');
    end
end