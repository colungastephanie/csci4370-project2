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

                            -- Database Data --

-- User Data (password is password for all users)
INSERT INTO `user` (`userId`, `username`, `password`, `firstName`, `lastName`) 
VALUES (1,'userOne','$2a$10$yFi/0J6Xi2NfFH6bd5M7LuNRkvd2N1lY03MU46Du2b.CV3DTLEiEe','user','one'),
(2,'userTwo','$2a$10$E/dGpJE0iSzP1xz/.VKrzerEm5NYX3FMWSCj2.mVu9bk3vRL0h1/O','user','two'),
(3,'userThree','$2a$10$lm8enHGulTGQ9RokEEkgMeVYqRDOJ7Dw7/mSgOGH9vAGaje55AVAK','user','three'),
(4,'userFour','$2a$10$p.3Yl/gXU7nMl3obSXkad.O5z..fhIBZYqtsCvQqVOMvv6jBLb0Gi','user','four');

-- Post Data 
INSERT INTO `post` (`postId`, `userId`, `content`, `createdAt`) 
VALUES (1,1,'first post!','2025-10-27 01:20:53'),
(2,1,'hi #firstpost\r\n','2025-10-27 05:51:38'),
(3,2,'database is so amazing! #database\r\n','2025-10-27 06:06:17'),
(4,2,'i love database! #database','2025-10-27 16:51:08'),
(5,3,'whats up! \r\n','2025-10-27 16:52:03');

-- Follow Data 
INSERT INTO `follow` (`followerId`, `followedUserId`, `createdAt`) 
VALUES (1,2,'2025-10-27 05:59:50'),
(2,1,'2025-10-27 06:09:56'),
(3,1,'2025-10-27 16:51:51'),
(3,2,'2025-10-27 16:51:41');


-- Add other data 
