

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








