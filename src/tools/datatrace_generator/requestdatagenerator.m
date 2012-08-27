clear all
close all
clc

fid = fopen('request.data', 'wt');
guid=1;
insert_num=10000;
TTL=99999;
weight=1;

for i = 1:insert_num
    fprintf(fid, '%d %c %d %d,%d,%d\n', i, 'I',guid, guid, TTL, weight);        
    guid = guid + 1;   
end

lookup_num=50000;
for i=1:lookup_num
    guid=randint(1,1,[1,insert_num]);
    if i<lookup_num
        fprintf(fid, '%d %c %d\n', i+insert_num, 'Q', guid);  
    else
        fprintf(fid, '%d %c %d', i+insert_num, 'Q', guid);
    end
end


fclose(fid);
