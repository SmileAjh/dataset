%[car_scale_label,car_scale_inst] = libsvmread('E:\Users\matlab\experience\car_scale_incre');
%car_scale_inst(:,17:24)=car_scale_inst(:,17:24)*1000;
[zero_car_scale_label,zero_car_scale_inst] = libsvmread('E:\Users\matlab\experience\zero');
zero_car_scale_inst(:,17:24)=zero_car_scale_inst(:,17:24)*1000;
[one_car_scale_label,one_car_scale_inst] = libsvmread('E:\Users\matlab\experience\one');
one_car_scale_inst(:,17:24)=one_car_scale_inst(:,17:24)*1000;
[two_car_scale_label,two_car_scale_inst] = libsvmread('E:\Users\matlab\experience\two');
two_car_scale_inst(:,17:24)=two_car_scale_inst(:,17:24)*1000;
[three_car_scale_label,three_car_scale_inst] = libsvmread('E:\Users\matlab\experience\three');
three_car_scale_inst(:,17:24)=three_car_scale_inst(:,17:24)*1000;
[four_car_scale_label,four_car_scale_inst] = libsvmread('E:\Users\matlab\experience\four');
four_car_scale_inst(:,17:24)=four_car_scale_inst(:,17:24)*1000;
[five_car_scale_label,five_car_scale_inst] = libsvmread('E:\Users\matlab\experience\five');
five_car_scale_inst(:,17:24)=five_car_scale_inst(:,17:24)*1000;
value=[];
train_value=[];
for i=1:10
zero=randperm(328); 
one=randperm(271);
two=randperm(215);
three=randperm(103);
four=randperm(89);
five=randperm(325);
a_label = [zero_car_scale_label(zero(1:228),:);one_car_scale_label(one(1:207),:);two_car_scale_label(two(1:135),:);
    three_car_scale_label(three(1:71),:);four_car_scale_label(four(1:61),:);five_car_scale_label(five(1:261),:)];
a=[zero_car_scale_inst(zero(1:228),:);one_car_scale_inst(one(1:207),:);two_car_scale_inst(two(1:135),:);
    three_car_scale_inst(three(1:71),:);four_car_scale_inst(four(1:61),:);five_car_scale_inst(five(1:261),:)];
b=[zero_car_scale_inst(zero(229:328),:);one_car_scale_inst(one(208:271),:);two_car_scale_inst(two(136:215),:);
    three_car_scale_inst(three(72:103),:);four_car_scale_inst(four(62:89),:);five_car_scale_inst(five(262:325),:)];
b_label=[zero_car_scale_label(zero(229:328),:);one_car_scale_label(one(208:271),:);two_car_scale_label(two(136:215),:);
    three_car_scale_label(three(72:103),:);four_car_scale_label(four(62:89),:);five_car_scale_label(five(262:325),:)];
% trian_inst_rand = car_scale_inst(a(1:800),:);
% trian_label_rand=car_scale_label(a(1:800),:);
% test_inst_rand = car_scale_inst(a(801:1484),:);
% test_label_rand =car_scale_label(a(801:1484 ),:);
train_inst = a(:,[1:3,5:9,11:11,13,16:17,20]);
train_label = a_label(:,:);
test_inst = b(:,[1:3,5:9,11:11,13,16:17,20]);
test_label = b_label(:,:);  
train_test_inst = a(:,[1:3,5:9,11:11,13:13,16:17,20]);
train_test_label = a_label(:,:);
% train_inst = car_scale_inst([16:17,18:68],:);                            
% test_inst = car_scale_inst(1:15,:);
% test_label = car_scale_label(1:15,:);
nb=NaiveBayes.fit(train_inst,train_label);
predict_label=predict(nb,test_inst);%test_data为测试数据；predict_label为预计的类型,N行1列；
accuracy=length(find(predict_label==test_label))/length(test_label)*100;%精确度判断
value=[value accuracy(1,1)];
predict_train_label=predict(nb,train_test_inst);%test_data为测试数据；predict_label为预计的类型,N行1列；
train_accuracy=length(find(predict_train_label==train_test_label))/length(train_test_label)*100;%精确度判断
train_value=[train_value train_accuracy(1,1)];
end
for i=1:10
    accuracy_average = mean(value);
    train_accuracy_average = mean(train_value);
end

