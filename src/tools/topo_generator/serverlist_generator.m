
clear all
close all
clc

server_fid = fopen('server.list', 'wt');

num=100;

for i=1:num
    fprintf(server_fid, '192.168.1.%d\n',99+i); 
end

fclose('all');