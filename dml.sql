

-- Description: Registers a new user to the website
-- URL: http://localhost:8081/register
insert into user (username, password, firstName, lastName) values (?, ?, ?, ?)

-- Description: Verifies the logged in user 
-- URL: http://localhost:8081/login
select * from user where username = ?

-- Description: Creates a post for the logged in user
-- URL: http://localhost:8081/
Insert into post (userId, content) values (?, ?)

-- Description: Adds parsed hashtag when the logged in user creates a post
-- URL: http://localhost:8081/
Insert ignore into hashtag(tag) values (?)

-- Description: Links a post to a hashtag
-- URL: http://localhost:8081/
Insert ignore into post_hashtag(postId, tag) values (?, ?)

-- Description: Shows all posts made by followed users of the logged in user
-- URL: http://localhost:8081/
SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN user u ON p.userId = u.userId
                WHERE (? IS NULL OR u.userId IN (
                    SELECT followedUserId FROM follow WHERE followerId = ?
                ))
                ORDER BY p.createdAt DESC


-- Description: Shows all of the logged in users posts 
-- URL path: http://localhost:8081/profile
select P.postId, P.content, P.createdAt, U.userId, U.firstName, U.lastName 
from post P join `user` U on P.userId = U.userId 
where P.userId = ? 
order by P.createdAt desc;


-- Description: Makes the logged in user follow another user
-- URL: http://localhost:8081/people
Insert ignore into follow (followerId, followedUserId) values (?, ?)


-- Description: Makes the logged in user unfollow another user
-- URL:  http://localhost:8081/people
Delete from follow where followerId = ? and followedUserId = ?


-- Description: Shows all the users on the platform (besides the logged in use), as well as their 
-- last post time
-- URL: http://localhost:8081/people
SELECT
              U.userId,                         
              U.firstName,                  
              U.lastName,                          
              LP.lastPostAt, 
              (F.followerId IS NOT NULL) AS isFollowed
            FROM `user` U
            LEFT JOIN (
                SELECT P.userId, MAX(P.createdAt) AS lastPostAt
                FROM post P
                GROUP BY P.userId
            ) LP ON LP.userId = U.userId
            LEFT JOIN follow F
                   ON F.followedUserId = U.userId
                  AND F.followerId    = ?
            WHERE U.userId <> ?
            ORDER BY (LP.lastPostAt IS NULL), LP.lastPostAt DESC, U.lastName, U.firstName;

-- Description: Links a user with a bookmarked post, adding it to the bookmarks page
-- URL path: http://localhost:8081/bookmarks
Insert ignore into bookmark (userId, postId) values (?, ?)

-- Description: Unlinks a user with a bookmarked post, removing it from the bookmarks page
-- URL path: http://localhost:8081/bookmarks
Delete from bookmark where userId = ? and postId = ?

-- Description: Fetches all the bookmarked posts of a specific user. Connected the bookmark
-- and user table and orders it by the date it was created.
-- URL path: http://localhost:8081/bookmarks
SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN bookmark b ON b.postId = p.postId
                JOIN user u ON u.userId = p.userId
                WHERE b.userId = ?
                ORDER BY p.createdAt DESC

-- Description: Fetches all the posts with a specific hashtag. Includes user input to search for
-- for the exact hashtag.
-- URL path: http://localhost:8081/bashtagsearch
SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN user u ON p.userId = u.userId
                JOIN post_hashtag ph ON p.postId = ph.postId 
                WHERE ph.tag IN (%s)
                GROUP BY p.postId
                HAVING COUNT(DISTINCT ph.tag) = ?
                ORDER BY p.createdAt DESC


