%file to generate jellyfish topology for orbit grid testbed

clear all
close all
clc

%AS number format: 5 digits XYUVW. X stands for shell(1) or Hang(0),
%Y stands for the layer, UVW is the tag
%e.g. 11002 stands for the AS 002 for Shell-1

%we emulate the Internet as the following layers: shell-0, hang-0, 
% shell-1, hang-1, shell-2, hang-2


%%% generate node for each layer
total_node_num=30;
node_num=zeros(1,6);
node_num(1)= ceil(total_node_num*0.09/100);   %shell-0
node_num(2)= ceil(total_node_num*8.04/100);   %hang-0
node_num(4)= ceil(total_node_num*20.5/100);   %hang-1
node_num(5)= ceil(total_node_num*22.43/100);   %shell-2
node_num(6)= ceil(total_node_num*0.85/100);   %hang-2
node_num(3)= total_node_num-node_num(1)-node_num(2)-node_num(4)-node_num(5)-node_num(6);   %shell-1

as_fid = fopen('AS.data', 'wt');
%shell-0
fprintf(as_fid, '10001\n');
%hang-0
for i=1:node_num(2)
    fprintf(as_fid, '00%d%d%d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10));
end
%shell-1
for i=1:node_num(3)
    fprintf(as_fid, '11%d%d%d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10));
end
%hang-1
for i=1:node_num(4)
    fprintf(as_fid, '01%d%d%d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10));
end
%shell-2
for i=1:node_num(5)
    fprintf(as_fid, '12%d%d%d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10));
end
%hang-2
for i=1:node_num(6)
    fprintf(as_fid, '02%d%d%d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10));
end

fid = fopen('jellyfish_topo.data', 'wt');

%%% generate transit connection
%% delay: uniform distributed between 555~65555 us

%%TODO: a node from lower layer might have multiple connections to upper layer 

% shell-0  <--> hang-0
for j=1:node_num(2)
    fprintf(fid, '10001 00%d%d%d %d\n',floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
end

% shell-0  <--> shell-1
for j=1:node_num(3)
    fprintf(fid, '10001 11%d%d%d %d\n',floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
end

% shell-1  <--> hang-1
for j=1:node_num(4)
    i=floor(1+ node_num(3)*rand(1));
    fprintf(fid, '11%d%d%d 01%d%d%d %d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10),floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
end

% shell-1  <--> shell-2
for j=1:node_num(5)
    i=floor(1+ node_num(3)*rand(1));
    fprintf(fid, '11%d%d%d 12%d%d%d %d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10),floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
end

% shell-2  <--> hang-2
for j=1:node_num(6)
    i=floor(1+ node_num(5)*rand(1));
    fprintf(fid, '12%d%d%d 02%d%d%d %d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10),floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
end

%%% generate peer connection
% shell-1
for i=1:node_num(3)-1
    for j=i+1:node_num(3)
        if rand(1)<0.5
            fprintf(fid, '11%d%d%d 11%d%d%d %d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10),floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
        end
    end
end

% shell-2
for i=1:node_num(5)-1
    for j=i+1:node_num(5)
        if rand(1)<0.5
            fprintf(fid, '12%d%d%d 12%d%d%d %d\n',floor(i/100),floor((i-100*floor(i/100))/10),i-100*floor(i/100)-10*floor((i-100*floor(i/100))/10),floor(j/100),floor((j-100*floor(j/100))/10),j-100*floor(j/100)-10*floor((j-100*floor(j/100))/10),floor(555+65000*rand(1))); 
            %if i<node_num(5)-1||j<node_num(5)
            %    fprintf(fid, '\n');
            %end
        end
    end
end

%% TODO: some shell node might only have 0 or 1 connection ( generate redundant connections between different layers)

