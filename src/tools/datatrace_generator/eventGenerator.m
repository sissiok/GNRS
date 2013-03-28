function [ ] = eventGenerator( matFile )

% Generate Event file contatining Insert, Update and Query
% Note: No Update events currently being generated
% Format of each line of event file: (please check
% https://bitbucket.org/romoore/gnrs/wiki/GNRS%20Workload%20Format for
% detail)
% insert:
% <index> <client_ID> <time_offset> I <GUID> <NA1,TTL1,weight1>, <NA2,TTL2,weight2>,
% update:
% <index> <client_ID> <time_offset> U <GUID> 
% <new <NA1,TTL1,weight1>>, <old <NA1,TTL1,weight1>>;
% <new <NA2,TTL2,weight2>>, <old <NA2,TTL2,weight2>>
% lookup:
% <index> <client_ID> <time_offset> Q <GUID>


%Define Total time over which Inserts & Queries will be generated (in secs)
totalTimeInsert = 20;
totalTimeQuery = 20;

%Define the rough number of Inserts & Queries to be generated
numInsert = 100000;
numQuery = 100000;

%GUIDs are inserted linearly starting from this value - This parameter is
%changed if multiple instances of the simulation are run in parallel.
insertStartNum = 1;

%Mean No. of inserts/queries per ms
lambdaInsert = numInsert/(totalTimeInsert* 1000);   
lambdaQuery = numQuery/(totalTimeQuery* 1000);   

%Source AS is chosen based on the NODE distribution databse - change to
%wherever these files are stored ( here the data is from the matFile)
load matFile;
%in = importdata(prefixData);
in = asPrefix(:,1);
in = sort(in);

% ****** DATA VALIDATION ********
% Done to remove the ASs which are not present in the topology file
%A = importdata(topoData);
A = asLinks;
asListFromEdge = unique([unique(A(:,1));unique(A(:,2))]);
uniqueIn = unique(in);

count = 0;
for i = 1:numel(uniqueIn)
    if (isempty(find(asListFromEdge == uniqueIn(i),1)))
        in(find(in == uniqueIn(i))) = [];
        count = count + 1;
    end
end

disp(strcat(num2str(count),' ASs were removed from the NODEs database'));


%Alternate way of deterministic Source AS generation: Used for debugging to
%make the inserts/queries come from specific sets of ASs

%China: inAlt = [4134;4538;4611;4808;4809;4812;4815;4816;4835;4847;4859;7499;7641];
%inAlt = [4816;7641]; %China alternate
%Europe: inAlt = [5400;5413;5631;12541;12659;12793;13127;15421;15533;15890;20766;20830;21083];
%Japan: inAlt = [23774;23812;23816;23912;24229;24252;24282;24336;24512;17707;17944];

% count = 0;
% for i = 1:numel(in)
%     if (isempty(find(asListFromEdge == inAlt(i),1)))
%         in(find(in == in(i))) = [];
%         count = count + 1;
%         disp(in(i));
%     end
% end


%Determines the No. of inserts/queries for every 1 ms slot
%Currently simple set to be a poisson randon number but could be changed to
%add the Mobility model
R = round(poissrnd(lambdaInsert,totalTimeInsert*1000,1));  %# of arrivals per ms
R1 = round(poissrnd(lambdaQuery,totalTimeQuery*1000,1));  %# of queries per ms

%Write the Events in File
fid = fopen('datatrace.data', 'wt');
guidCounter = insertStartNum;

%index starts from 1
index = 1;
%hard code TTL and weight here
TTL = 999999;
weight = 1;

%Write the Inserts First
for i = 1:length(R)
    for j = 1:R(i)
        tmp = round(1 + (length(in)-1) * rand); % Pick a random AS from the list
        fprintf(fid, '%d %d %d %c %d %d,%d,%d\n', index, in(tmp), i, 'I', guidCounter, guidCounter, TTL, weight);
        guidCounter = guidCounter + 1;
        index = index + 1;
    end
end

%Then write the Query events
for k = i+1 : i + length(R1)
    for l = 1:R1(k - i)
        tmp = round(1 + (length(in)-1) * rand); % Pick a random AS from the list
        fprintf(fid, '%d %d %d %c %d\n', index, in(tmp), k, 'Q', round(insertStartNum + (guidCounter-insertStartNum)*rand));
        index = index + 1;
    end
end

fclose(fid);

