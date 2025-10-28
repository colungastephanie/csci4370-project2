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

My Phuong Ly: I implemented the post interaction features for the microblogging platform, including liking, bookmarking, and commenting on posts. My work follows the project requirement to include a new UI part, a controller, a service, and database operations for each feature
  Featues I implemented:
  1. Like posts
  - Users can like/unlike posts; updated database and UI to reflect current like count
  2. Bookmark posts
  - Users can bookmark/unbookmark posts; database and UI updated accordingly
  3. Comments: 
  - Users can add comments; comments displayed oldest to newest; database operations handle storing and retrieving comments
  
Susan Awad: I implemented the bookmarks page and hashtage search. The user can view all the posts that they have bookmarked and can
unbookmark the post to remove it from the page. I also implemented the hashtag search where users can search for specific tags in the form
"#hashtag" and any post that has that specific tag will appear in the list.
Features I implemented:
1. Bookmarks Page
- Show posts that the logged in user has bookmarked
- Can bookmark and unbookmark a post
2. Hashtag Search
- User can type in one or more hashtags into the search field (ie: #2025 #fireworks)
- The results should show posts that have all hashtags with the most recent posts first

Mohammed Nizar Meskine: I implemented the post detail page and backend features that allow users to comment on posts, view engagement counts (likes and comments), and see properly formatted post timestamps. I also connected these features to the database and integrated bookmark functionality for posts.
 1.	Post Page and Commenting
- Implemented the /post/{postId} route in PostController to display a full post with all associated comments.
- Added form handling logic that allows authenticated users to submit new comments through the interface.
- Integrated PostService.addComment() to insert new comments into the database and display them in chronological order.
- Ensured empty comments are not allowed and errors are displayed gracefully.
	2.	Display of Like and Comment Counts
- Added backend logic to fetch like and comment totals for each post using getLikesCount() and getCommentCount() methods in PostService.
- Connected these counts to the user interface so that each post dynamically displays its current engagement metrics.
	3.	Post Date Formatting
- Used Java’s DateTimeFormatter to display post creation dates in the format: Mar 07, 2025, 10:54 PM
- Applied the same formatting for comment timestamps to maintain consistency.
	4.	Bookmark Integration
- Connected the bookmark toggle to BookmarksServices, allowing users to bookmark or unbookmark posts.
- Verified that the database and UI stay synchronized when bookmarks are added or removed.
