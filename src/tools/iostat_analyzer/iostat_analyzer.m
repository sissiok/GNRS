clear all
close all
clc

x=importdata('iostat.data');

[m,n]=size(x);

start_=0;
end_=0;

for i=1:m
    if(x(i,n)<94&&start_==0)
        start_=i+1;
    end
    if(start_>0&&x(i,n)>94&&end_==0)
        end_=i-1;
    end
end

y=x(start_:end_,n);

100-mean(y)
100-max(y)
100-min(y)