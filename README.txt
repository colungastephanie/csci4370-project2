Members and Contributions

  Stephanie Colunga: I implemented the end to end workflow that allows authenticated uses to create, view, and search posts
on the microblogging platform. This implementation also connects the home feed with the database so that users are able to see posts from accounts they follow,
ordered from newest to oldest.
Features Implemented:
1. Making posts as the logged-in user
- added a PostService class to handle interactions for post creation and retrieval
- implmented the createPost() method to insert a new record into the post table and automatically parse hashtags
from post content and store them in the hashtag and post_hashtag tables
- Connected the backend to the homepage form so that users can submit posts from the user interface
- Integrated UserService to associate posts with an authenticated user
2. Posts can have hashtags
- Implemented a parser inside PostService to detect hashtags within posts
- Added logic to normalize hashtags, insert unique tags into the hashtag table, and record relationships between posts
and hashtags in post_hashtag table
3. Posts Searchable by Hashtags
- Extended PostService to be able to retrieve posts filtered by hashtags
- Added SQL queries that join post, hashtag, and post_hashtag tables
4. Home Page of Followed Users
- Implemented logic that filters home feed to include only posts by users the logged in user follows
- Utilized follow table to determine which users to display
- Ensured posts appeared from most recent to oldest
- Tested end to end by following/unfollowing users and verifying the home feed updates correctly

  Joelia Agbavon: I implemented features that allow usrs to view posts their posts on the profile tab, as well as other
  user on the platform, by clicking on their user icon. I also added the ability to view all users on microblogging platform on 
  the people page, and be able to folow/unfollow other users.
  Featues I implemented:
  1. Profile page should allow seeing posts from a specific user
  - Utillizes UserService to get all user posts from specified user 
  2. Format timestamps as: Mar 07, 2025, 10:54 PM
  - Created a format method in Utility class to correctly format the user timestamps
  3. People page should list all users in the platform
  - Utilizes PeopleService, UserService, PostService to fetch all the users on the platform
  - Displays the last time the user posted on the platform; 
  4. Abilitiy to follow/unfollow users 
  - Utilizes UserService and FollowService to handle follow and unfollow requests 
  
