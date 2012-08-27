clear all
close all
clc

x=importdata('throughput.data');

[m,n]=size(x);
MAX=max(x(:,n));
start_=0;
end_=0;

for i=1:m
    if(x(i,n)>0&&start_==0)
        start_=i+1;
    end
    if(x(i,n)==MAX&&end_==0)
        end_=i-1;
    end
end

y=zeros(1,end_-start_);
for i=start_:end_-1
    y(i-start_+1)=x(i+1,n)-x(i,n);
end

mean(y)
max(y)
min(y)