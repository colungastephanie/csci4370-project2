-- Create the database.
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

-- Create follow table
create table if not exists follow (
    followerId int not null,
    followedUserId int not null,
    createdAt timestamp default current_timestamp,
    primary key (followerId, followedUserId),
    constraint fk_follow_follower foreign key (followerId) references user(userId) on delete cascade,
    constraint fk_follow_followed foreign key (followedUserId) references user(userId) on delete cascade
);   

-- Create post table
create table if not exists post (
    postId int auto_increment primary key,
    userId int not null,
    content text not null,
    createdAt timestamp default current_timestamp,
    foreign key (userId) references user(userId) on delete cascade
);

-- Create hashtag table
create table if not exists hashtag (
    tag varchar(64) primary key
);

-- Create post and hashtag table
create table if not exists post_hashtag (
    postId int not null,
    tag varchar(64) not null,
    primary key (postId, tag),
    foreign key (postId) references post(postId) on delete cascade,
    foreign key (tag) references hashtag(tag) on delete cascade
);
