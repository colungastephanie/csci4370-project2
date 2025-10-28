package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
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
                        "INSERT INTO post (userId, content) VALUES (?, ?)",
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
                             "INSERT IGNORE INTO hashtag(tag) VALUES (?)");
                         PreparedStatement link = c.prepareStatement(
                             "INSERT IGNORE INTO post_hashtag(postId, tag) VALUES (?, ?)")) {
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
                SELECT p.postId, p.content, p.createdAt,
                       u.userId, u.firstName, u.lastName
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
                    String postDate = Utility.foramtTime(timeStamp); // note: method name as in your Utility
                    // counts/toggles on feed are optional; keep zeros/false here (controller can populate as needed)
                    Post post = new Post(postId, content, postDate, user, 0, 0, false, false);
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error receiving posts", e);
        }
        return posts;
    }

  

    /** Insert a new comment for a post. */
    public void addComment(int userId, int postId, String content) throws SQLException {
        final String sql = "INSERT INTO comments (postId, userId, content) VALUES (?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    /** Return all comments for a post (oldest -> newest). */
    public List<Comment> getCommentsByPostId(int postId) {
        final String sql = """
                SELECT c.commentId, c.postId, c.userId, c.content, c.createdAt,
                       u.firstName, u.lastName
                FROM comments c
                JOIN user u ON u.userId = c.userId
                WHERE c.postId = ?
                ORDER BY c.createdAt ASC
                """;
        List<Comment> out = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String commentId = String.valueOf(rs.getInt("commentId"));
                    String content = rs.getString("content");
                    java.sql.Timestamp ts = rs.getTimestamp("createdAt");

                    // Build lightweight user for the comment
                    User user = new User(
                            String.valueOf(rs.getInt("userId")),
                            rs.getString("firstName"),
                            rs.getString("lastName"));

                    // Pretty date using your existing helper
                    String commentDate = Utility.foramtTime(ts);

                    out.add(new Comment(commentId, content, commentDate, user));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error getting comments for postId=" + postId, e);
        }
        return out;
    }

    /** Count comments for a post. */
    public int getCommentCount(int postId) {
        final String sql = "SELECT COUNT(*) AS cnt FROM comments WHERE postId = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            throw new RuntimeException("error counting comments for postId=" + postId, e);
        }
        return 0;
    }

    /** Count likes/hearts for a post. */
    public int getLikesCount(int postId) {
        final String sql = "SELECT COUNT(*) AS cnt FROM likes WHERE postId = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            throw new RuntimeException("error counting likes for postId=" + postId, e);
        }
        return 0;
    }

    /** Did this user like this post? */
    public boolean hasUserLikedPost(int userId, int postId) {
        final String sql = "SELECT 1 FROM likes WHERE userId = ? AND postId = ? LIMIT 1";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("error checking like state userId=" + userId + " postId=" + postId, e);
        }
    }

    /** Did this user bookmark this post? */
    public boolean hasUserBookmarked(int userId, int postId) {
        final String sql = "SELECT 1 FROM bookmark WHERE userId = ? AND postId = ? LIMIT 1";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("error checking bookmark state userId=" + userId + " postId=" + postId, e);
        }
    }
}