<<<<<<< Updated upstream
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;

@Service
public class PostService {
    private final DataSource ds;
    private static final Pattern HASHTAG = Pattern.compile("#(\\w{1,64})");

    @Autowired
    public PostService(DataSource ds) { 
        this.ds = ds; 
    }

    public static List<String> extractTags(String content) {
        List<String> tags = new ArrayList<>();
        Matcher m = HASHTAG.matcher(content);
        while (m.find()) {
            tags.add(m.group(1).toLowerCase(Locale.ROOT));
        }
        return tags;
    }

    public int createPost(int userId, String content) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                int postId;
                try (PreparedStatement ps = c.prepareStatement(
                    "Insert into post (userId, content) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, userId);
                        ps.setString(2, content);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            keys.next();
                            postId = keys.getInt(1);
                        }
                    }
                List<String> tags = extractTags(content);
                if (!tags.isEmpty()) {
                    try (PreparedStatement insTag = c.prepareStatement(
                        "Insert ignore into hashtag(tag) values (?)");
                        PreparedStatement link = c.prepareStatement(
                            "Insert ignore into post_hashtag(postId, tag) values (?, ?)")) {
                                for (String t : tags) {
                                    insTag.setString(1, t);
                                    insTag.addBatch();

                                    link.setInt(1, postId);
                                    link.setString(2, t);
                                    link.addBatch();
                                }
                                insTag.executeBatch();
                                link.executeBatch();
                            }
                        
                    
                }
                c.commit();
                return postId;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<Post> getAllPosts(Integer userId) {
        List<Post> posts = new ArrayList<>();

        final String sql = """
                SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN user u ON p.userId = u.userId
                WHERE (p.userId = ? OR p.userId IN (
                    SELECT followedUserId FROM follow WHERE followerId = ?
                ))
                ORDER BY p.createdAt DESC
                """;
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String content = rs.getString("content");
                    java.sql.Timestamp timeStamp = rs.getTimestamp("createdAt");
                    String useId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    User user = new User(useId, firstName, lastName);
                    String postDate = Utility.foramtTime(timeStamp);
                    Post post = new Post(postId, content, postDate, user, 0, 0, false, false);
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error receiving posts", e);
        }
        return posts;
    }

}
=======
package uga.menik.csx370.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.Comment; 
import uga.menik.csx370.models.User;

import java.util.List;

@Service
public class PostService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // LIKES FEATURE
    public void toggleLike(int userId, int postId) {
        if (hasUserLikedPost(userId, postId)) {
            removeLike(userId, postId);
        } else {
            addLike(userId, postId);
        }
    }
    
    // Add a like to a post
    private void addLike(int userId, int postId) {
        String sql = "INSERT INTO likes (userId, postId) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, postId);
    }
    
    // Remove a like from a post
    private void removeLike(int userId, int postId) {
        String sql = "DELETE FROM likes WHERE userId = ? AND postId = ?";
        jdbcTemplate.update(sql, userId, postId);
    }
    
    // Check if user has liked a post
    public boolean hasUserLikedPost(int userId, int postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE userId = ? AND postId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, postId);
        return count != null && count > 0;
    }
    
    // Get total likes count for a post
    public int getLikesCount(int postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE postId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count != null ? count : 0;
    }
    
    // BOOKMARKS FEATURE
    public void toggleBookmark(int userId, int postId) {
        if (hasUserBookmarked(userId, postId)) {
            removeBookmark(userId, postId);
        } else {
            addBookmark(userId, postId);
        }
    }
    
    // Add a bookmark
    private void addBookmark(int userId, int postId) {
        String sql = "INSERT INTO bookmarks (userId, postId) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, postId);
    }
    
    // Remove a bookmark
    private void removeBookmark(int userId, int postId) {
        String sql = "DELETE FROM bookmarks WHERE userId = ? AND postId = ?";
        jdbcTemplate.update(sql, userId, postId);
    }
    
    // Check if user has bookmarked a post
    public boolean hasUserBookmarked(int userId, int postId) {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE userId = ? AND postId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, postId);
        return count != null && count > 0;
    }
    
    // COMMENTS FEATURE
    public List<Comment> getCommentsByPostId(int postId) {
        String sql = "SELECT c.commentId, c.postId, c.userId, c.content, c.createdAt, " +
                    "u.username, u.firstName, u.lastName " +
                    "FROM comments c " +
                    "JOIN user u ON c.userId = u.userId " +
                    "WHERE c.postId = ? " +
                    "ORDER BY c.createdAt ASC";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            // Create User object
            User user = new User(
                String.valueOf(rs.getInt("userId")),
                rs.getString("firstName"),
                rs.getString("lastName")
            );

            // Create Comment using constructor
            return new Comment(
                String.valueOf(rs.getInt("commentId")),
                rs.getString("content"),
                rs.getTimestamp("createdAt").toString(),
                user
            );
        }, postId);
    }
    
    public void addComment(int userId, int postId, String content) {
        // Validate comment is not empty
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        
        String sql = "INSERT INTO comments (userId, postId, content) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, postId, content);
    }
    
    // Get comment count for a post
    public int getCommentCount(int postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE postId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count != null ? count : 0;
    }
    
    // REPOSTS FEATURE
    public void toggleRepost(int userId, int postId) {
        if (hasUserReposted(userId, postId)) {
            removeRepost(userId, postId);
        } else {
            addRepost(userId, postId);
        }
    }
    
    // Add a repost
    private void addRepost(int userId, int originalPostId) {
        String sql = "INSERT INTO repost (userId, originalPostId) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, originalPostId);
    }
    
    // Remove a repost
    private void removeRepost(int userId, int originalPostId) {
        String sql = "DELETE FROM repost WHERE userId = ? AND originalPostId = ?";
        jdbcTemplate.update(sql, userId, originalPostId);
    }
    
    // Check if user has reposted
    public boolean hasUserReposted(int userId, int originalPostId) {
        String sql = "SELECT COUNT(*) FROM repost WHERE userId = ? AND originalPostId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, originalPostId);
        return count != null && count > 0;
    }
    
    // Get repost count for a post
    public int getRepostCount(int postId) {
        String sql = "SELECT COUNT(*) FROM repost WHERE originalPostId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count != null ? count : 0;
    }
}
>>>>>>> Stashed changes
