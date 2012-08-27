clear all
close all
clc

x=[2857.306132 9625.103193 12886.59794 19360.42825 23881.19583 29140.16126 48551.93844];
y1=[2840 9580 12635 18550 23500 23000 18800];
y2=[4.22 8.28 10.65 20.25 23.02 22.88 22.33];
y3=[50000 50000 50000 46661 44500 31874 21664];

figure
plot(x,y1,'r:o');
xlabel('total client lookup sending rate');
ylabel('server lookup processing rate');

figure
plot(x,y2,'r:o');
xlabel('total client lookup sending rate');
ylabel('server cpu usage (%)');

figure
plot(x,y3,'r:o');
xlabel('total client lookup sending rate');
ylabel('total num of lookup response received per client');

