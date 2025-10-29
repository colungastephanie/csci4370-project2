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



-- Follows


-- Posts


-- Hashtags

-- Post-Hashtag relationships

-- Likes


-- Bookmarks


-- Comments
