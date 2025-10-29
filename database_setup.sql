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

-- Create booksmark table
create table if not exists bookmark (
    userId int not null,
    postId int not null,
    createdAt timestamp default current_timestamp,
    primary key (userId, postId),
    foreign key (userId) references user(userId) on delete cascade,
    foreign key (postId) references post(postId) on delete cascade
);

-- Create likes table 

CREATE TABLE IF NOT EXISTS likes (
    userId    INT NOT NULL,
    postId    INT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, postId),
    CONSTRAINT fk_l_user FOREIGN KEY (userId) REFERENCES `user`(userId) ON DELETE CASCADE,
    CONSTRAINT fk_l_post FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    INDEX idx_l_post (postId)
);

-- Create comments table  

CREATE TABLE IF NOT EXISTS comments (
    commentId INT AUTO_INCREMENT PRIMARY KEY,
    postId    INT NOT NULL,
    userId    INT NOT NULL,
    content   TEXT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_c_post FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    CONSTRAINT fk_c_user FOREIGN KEY (userId) REFERENCES `user`(userId) ON DELETE CASCADE,
    INDEX idx_c_post (postId),
    INDEX idx_c_user (userId),
    INDEX idx_c_createdAt (createdAt)
);

-- Create repost table 

CREATE TABLE IF NOT EXISTS repost (
    userId         INT NOT NULL,           
    postId         INT NOT NULL, 
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,          
    PRIMARY KEY (userId, postId),
    CONSTRAINT fk_repost_user FOREIGN KEY (userId) 
        REFERENCES `user`(userId) ON DELETE CASCADE,
    CONSTRAINT fk_repost_post FOREIGN KEY (postId) 
        REFERENCES post(postId) ON DELETE CASCADE
); 

                    --- Database Data ---

-- Users (password is bcrypt hash for "pass")
INSERT INTO `user` (`userId`, `username`, `password`, `firstName`, `lastName`) 
VALUES (1,'userOne','$2a$10$16ezko9aMz0HeSUE48OcK.2LU/NW926/xmM2O.qhiN8VnCqqhMjcW','user','one'),
(2,'userTwo','$2a$10$d4zdyL5h/r5cKt2tV9yGluw3yIwsyn2sXot3LVzKD8cRmhnvOBsyO','user','two'),
(3,'userThree','$2a$10$GkQz./126O4ACfOumINpZOu8DIu9h82TAJr6fsWZE/WNAJ.1Hhv1.','user','three'),
(4,'userFour','$2a$10$lVLF9E/Er2HsVo6hSHOT/eMrrkSAxyyQ34jsjnKmN20VIQ5BXbKOi','user','four'),
(5,'userFive','$2a$10$CberThTe2gkHq0dvAgQoVOpQ5phSG.jmJk.yZwsAhx3GJaMz6L0ZO','user','five'),
(6,'userSix','$2a$10$z.BmEkq94VXuNwaTWXlwVOZqUzqNSjzMOLkZIMIiDasr2edtAuK5G','user','six');


-- Follow
INSERT INTO `follow` (`followerId`, `followedUserId`, `createdAt`) 
VALUES (1,2,'2025-10-29 00:02:47'),
(1,3,'2025-10-29 00:02:46'),
(1,4,'2025-10-29 00:04:58'),
(2,1,'2025-10-28 18:46:06'),
(2,4,'2025-10-28 18:49:35'),
(3,1,'2025-10-28 18:50:34'),
(3,2,'2025-10-28 18:50:59'),
(3,4,'2025-10-28 18:51:02'),
(3,5,'2025-10-28 18:51:01'),
(4,1,'2025-10-28 19:21:20'),
(4,3,'2025-10-28 19:21:17'),
(5,2,'2025-10-29 00:08:58'),
(5,3,'2025-10-29 00:08:59'),
(6,1,'2025-10-29 00:07:08'),
(6,2,'2025-10-29 00:07:07');

-- Posts
INSERT INTO `post` (`postId`, `userId`, `content`, `createdAt`) 
VALUES (1,1,'first post! #firstpost\r\n','2025-10-28 18:42:34'),
(2,2,'i love blogging! #firstpost #blogging','2025-10-28 18:45:28'),
(3,2,'database is awesome! ','2025-10-28 18:46:03'),
(4,3,'database is amazinggg #database ','2025-10-28 18:50:31'),
(5,4,'hey guys!\r\n','2025-10-28 19:21:11'),
(6,5,'beautiful weather today! #sunny ','2025-10-28 19:28:24'),
(7,5,'i love uga! #uga','2025-10-28 19:28:41'),
(8,5,'computer science is the best major at uga #compsci #uga','2025-10-28 19:30:23'),
(9,6,'i <3 database #love #database\r\n','2025-10-29 00:06:58');

-- Hashtags
INSERT INTO `hashtag` (`tag`) 
VALUES ('blogging'),('compsci'),
('database'),('firstpost'),
('love'),
('sunny'),
('uga');

-- Post-Hashtag relationships
INSERT INTO `post_hashtag` (`postId`, `tag`) 
VALUES (2,'blogging'),
(8,'compsci'),
(4,'database'),
(9,'database'),
(1,'firstpost'),
(2,'firstpost'),
(9,'love'),
(6,'sunny'),
(7,'uga'),
(8,'uga');

-- Likes
INSERT INTO `likes` (`userId`, `postId`, `createdAt`) 
VALUES (1,1,'2025-10-29 00:10:33'),
(1,6,'2025-10-29 00:02:52'),
(2,1,'2025-10-28 18:46:09'),
(2,2,'2025-10-28 18:49:42'),
(3,1,'2025-10-28 18:50:37'),
(3,2,'2025-10-28 18:51:20'),
(3,3,'2025-10-28 18:51:16'),
(3,4,'2025-10-28 18:59:23'),
(4,1,'2025-10-29 00:09:49'),
(4,4,'2025-10-28 19:21:26'),
(6,1,'2025-10-29 00:07:14'),
(6,3,'2025-10-29 00:07:58'),
(6,8,'2025-10-29 00:07:22');

-- Bookmarks
INSERT INTO `bookmark` (`userId`, `postId`, `createdAt`) 
VALUES (1,1,'2025-10-28 19:13:16'),
(1,3,'2025-10-29 00:03:38'),
(1,6,'2025-10-28 22:39:52'),
(1,7,'2025-10-28 23:46:03'),
(2,1,'2025-10-28 18:46:11'),
(3,2,'2025-10-28 18:54:16'),
(6,3,'2025-10-29 00:42:16'),
(6,9,'2025-10-29 00:07:02');

-- Comments
INSERT INTO `comments` (`commentId`, `postId`, `userId`, `content`, `createdAt`)
 VALUES (1,1,2,'nice first post! #firstpost','2025-10-28 18:48:41'),
 (2,2,2,'first blog btw! :)','2025-10-28 18:50:07'),
 (3,6,1,'the weather is amazing','2025-10-29 00:03:02'),
 (4,6,1,'its so sunny out','2025-10-29 00:03:14'),
 (5,8,1,'i disagree','2025-10-29 00:03:30'),
 (6,6,6,'this weather sucks ;/','2025-10-29 00:07:46'),
 (7,3,6,'ur so right!','2025-10-29 00:08:05'),
 (8,1,6,'welcome!','2025-10-29 00:08:30');

-- Reposts 
INSERT INTO `repost` (`userId`, `postId`, `createdAt`) 
VALUES (1,1,'2025-10-29 00:03:52'),
(1,2,'2025-10-29 00:04:23'),
(1,4,'2025-10-29 00:11:04'),
(1,5,'2025-10-29 00:14:23'),
(1,7,'2025-10-29 00:05:11'),
(4,1,'2025-10-29 00:09:54'),
(4,4,'2025-10-29 00:09:56'),
(5,2,'2025-10-29 00:09:07'),
(5,4,'2025-10-29 00:09:04'),
(5,6,'2025-10-29 00:08:45'),
(5,7,'2025-10-29 00:08:44'),
(5,9,'2025-10-29 00:08:52'),
(6,9,'2025-10-29 00:35:57');