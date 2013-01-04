%file to generate jellyfish topology for orbit grid testbed

clear all
close all
clc

% we emulate the Internet as the following layers: shell-0, hang-0, shell-1, hang-1, shell-2, hang-2
% AS number starts from 1, and maps to different layers of the Internet in the following sequence: shell-0, hang-0, shell-1, hang-1, shell-2, hang-2


%%% generate AS for each layer
total_AS_num=30;
AS_num=zeros(1,6);
AS_num(1)= ceil(total_AS_num*0.09/100);   %shell-0
AS_num(2)= ceil(total_AS_num*8.04/100);   %hang-0
AS_num(4)= ceil(total_AS_num*20.5/100);   %hang-1
AS_num(5)= ceil(total_AS_num*22.43/100);   %shell-2
AS_num(6)= ceil(total_AS_num*0.85/100);   %hang-2
AS_num(3)= total_AS_num-AS_num(1)-AS_num(2)-AS_num(4)-AS_num(5)-AS_num(6);   %shell-1

%generate the AS list file
as_fid = fopen('AS.data', 'wt');
as = 1;
for i=1:6
    for j=1:AS_num(i);
    	fprintf(as_fid, '%d\n', as);
	as = as +1;
    end
end


%%% generate transit connection
%% delay: uniform distributed between 0.555~100 ms

%%TODO: a AS from lower layer might have multiple connections to upper layer 
fid = fopen('jellyfish_topo.data', 'wt');
as = 1;

for i=2:6
    for j=1:AS_num(i)
	dst = as + j;
	% i=2: shell-0  <--> hang-0
	% i=3: shell-0  <--> shell-1
	if(i==2 || i==3)
		src = floor(1+ AS_num(1)*rand(1));
	% i=4: shell-1  <--> hang-1
	% i=5: shell-1  <--> shell-2
	elseif(i==4 || i==5)
		src = floor(AS_num(1) + AS_num(2) + 1 + AS_num(3)*rand(1));
	% i=6: shell-2  <--> hang-2
	else
		src= floor(AS_num(1) + AS_num(2) + AS_num(3) + AS_num(4) + 1 + AS_num(5)*rand(1));
	end
	fprintf(fid, '%d %d %d\n', src, dst, floor(0.555+100*rand(1)));
    end
    as = as + AS_num(i);
end


%%% generate peer connection
% shell-1
for i=1:AS_num(3)-1
    for j=i+1:AS_num(3)
        if rand(1)<0.5  %TODO: the value needs to be tuned
	    src = AS_num(1) + AS_num(2) + i;
	    dst = AS_num(1) + AS_num(2) + j;
            fprintf(fid, '%d %d %d\n', src, dst, floor(0.555+100*rand(1)));
        end
    end
end

% shell-2
for i=1:AS_num(5)-1
    for j=i+1:AS_num(5)
        if rand(1)<0.5 %TODO: the value needs to be tuned
            src = AS_num(1) + AS_num(2) + AS_num(3) + AS_num(4) + i;
            dst = AS_num(1) + AS_num(2) + AS_num(3) + AS_num(4) + j;
            fprintf(fid, '%d %d %d\n', src, dst, floor(0.555+100*rand(1)));
        end
    end
end

%% TODO: some shell AS might only have 0 or 1 connection, need to make sure that all nodes within one layer are connected with each other
%% TODO: generate redundant connections between different layers

