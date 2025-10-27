package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;

@Service
public class BookmarksServices {
    private final DataSource datasource;

    @Autowired
    public BookmarksServices(DataSource dataSource) {
        this.datasource = dataSource;
    }

    public void bookmarkPost(int userId, String postId, Boolean isAdd) throws SQLException {
        try (Connection conn = datasource.getConnection()) {
            if (isAdd == true) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "Insert ignore into bookmark (userId, postId) values (?, ?)")) {
                        ps.setInt(1, userId);
                        ps.setString(2, postId);
                        ps.executeUpdate();
                    };
            } else {
                try(PreparedStatement ps = conn.prepareStatement(
                    "Delete from bookmark where userId = ? and postId = ?")) {
                        ps.setInt(1, userId);
                        ps.setString(2, postId);
                        ps.executeUpdate();
                };
            }
        }
    }

    public List<Post> getAllBookmarks(Integer userId) {
        List<Post> bookmarks = new ArrayList<>();

        final String sql = """
                SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN bookmark b ON b.postId = p.postId
                JOIN user u ON u.userId = p.userId
                WHERE b.userId = ?
                ORDER BY p.createdAt DESC
                """;
        try (Connection conn = datasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
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
                    Post bookmark = new Post(postId, content, postDate, user, 0, 0, false, true);
                    bookmarks.add(bookmark);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error receiving posts", e);
        }
        return bookmarks;
    }
}
