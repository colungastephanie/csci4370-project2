-- Create the database.
CREATE DATABASE IF NOT EXISTS csx370_mb_platform;

-- Use the created database.
USE csx370_mb_platform;

-- ===========================================
-- USER TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS `user` (
    userId      INT AUTO_INCREMENT,
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,         -- stores the bcrypt hash in your seed
    firstName   VARCHAR(255) NOT NULL,
    lastName    VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId),
    UNIQUE (username),
    CONSTRAINT username_min_length CHECK (CHAR_LENGTH(TRIM(username)) >= 2),
    CONSTRAINT firstName_min_length CHECK (CHAR_LENGTH(TRIM(firstName)) >= 2),
    CONSTRAINT lastName_min_length CHECK (CHAR_LENGTH(TRIM(lastName)) >= 2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- FOLLOW TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS follow (
    followerId      INT NOT NULL,
    followedUserId  INT NOT NULL,
    createdAt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (followerId, followedUserId),
    CONSTRAINT fk_follow_follower FOREIGN KEY (followerId)     REFERENCES `user`(userId) ON DELETE CASCADE,
    CONSTRAINT fk_follow_followed FOREIGN KEY (followedUserId) REFERENCES `user`(userId) ON DELETE CASCADE,
    INDEX idx_follow_follower (followerId),
    INDEX idx_follow_followed (followedUserId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- POST TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS post (
    postId      INT AUTO_INCREMENT PRIMARY KEY,
    userId      INT NOT NULL,
    content     TEXT NOT NULL,
    createdAt   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (userId) REFERENCES `user`(userId) ON DELETE CASCADE,
    INDEX idx_post_user (userId),
    INDEX idx_post_createdAt (createdAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- HASHTAG TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS hashtag (
    tag VARCHAR(64) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- POST_HASHTAG (junction)  <-- FIXED to reference hashtag(tag)
-- ===========================================
CREATE TABLE IF NOT EXISTS post_hashtag (
    postId INT NOT NULL,
    tag    VARCHAR(64) NOT NULL,
    PRIMARY KEY (postId, tag),
    CONSTRAINT fk_ph_post FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    CONSTRAINT fk_ph_tag  FOREIGN KEY (tag)    REFERENCES hashtag(tag) ON DELETE CASCADE,
    INDEX idx_ph_post (postId),
    INDEX idx_ph_tag  (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- BOOKMARK TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS bookmark (
    userId    INT NOT NULL,
    postId    INT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, postId),
    CONSTRAINT fk_bm_user FOREIGN KEY (userId) REFERENCES `user`(userId) ON DELETE CASCADE,
    CONSTRAINT fk_bm_post FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    INDEX idx_bm_user (userId),
    INDEX idx_bm_post (postId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- COMMENTS TABLE (used by PostService.addComment / getCommentsByPostId)
-- ===========================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- LIKES TABLE (used by getLikesCount / hasUserLikedPost)
-- ===========================================
CREATE TABLE IF NOT EXISTS likes (
    userId    INT NOT NULL,
    postId    INT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, postId),
    CONSTRAINT fk_l_user FOREIGN KEY (userId) REFERENCES `user`(userId) ON DELETE CASCADE,
    CONSTRAINT fk_l_post FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    INDEX idx_l_post (postId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- SEED DATA
-- ===========================================

-- Users (password is bcrypt hash for "password")
INSERT INTO `user` (userId, username, password, firstName, lastName) VALUES
(1,'userOne',   '$2a$10$yFi/0J6Xi2NfFH6bd5M7LuNRkvd2N1lY03MU46Du2b.CV3DTLEiEe','user','one'),
(2,'userTwo',   '$2a$10$E/dGpJE0iSzP1xz/.VKrzerEm5NYX3FMWSCj2.mVu9bk3vRL0h1/O','user','two'),
(3,'userThree', '$2a$10$lm8enHGulTGQ9RokEEkgMeVYqRDOJ7Dw7/mSgOGH9vAGaje55AVAK','user','three'),
(4,'userFour',  '$2a$10$p.3Yl/gXU7nMl3obSXkad.O5z..fhIBZYqtsCvQqVOMvv6jBLb0Gi','user','four')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Follows
INSERT INTO follow (followerId, followedUserId, createdAt) VALUES
(1,2,'2025-10-27 05:59:50'),
(2,1,'2025-10-27 06:09:56'),
(3,1,'2025-10-27 16:51:51'),
(3,2,'2025-10-27 16:51:41')
ON DUPLICATE KEY UPDATE createdAt = VALUES(createdAt);

-- Posts
INSERT INTO post (postId, userId, content, createdAt) VALUES
(1,1,'first post!','2025-10-27 01:20:53'),
(2,1,'hi #firstpost','2025-10-27 05:51:38'),
(3,2,'database is so amazing! #database','2025-10-27 06:06:17'),
(4,2,'i love database! #database','2025-10-27 16:51:08'),
(5,3,'whats up!','2025-10-27 16:52:03')
ON DUPLICATE KEY UPDATE content = VALUES(content), createdAt = VALUES(createdAt);