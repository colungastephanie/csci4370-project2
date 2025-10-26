/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;



import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.utility.Utility;



/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Queries and returns all users that are followable.
     * The list should not contain the user with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        final String sql = """
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
            """;

        List<FollowableUser> followableUsers = new ArrayList<>();
        int viewerId = Integer.parseInt(userIdToExclude);
        try (Connection connect = dataSource.getConnection();
             PreparedStatement statement = connect.prepareStatement(sql)) {
                statement.setInt(1, viewerId);
                statement.setInt(2, viewerId);
            if (userIdToExclude == null || userIdToExclude.isBlank()) {
                statement.setNull(1, Types.VARCHAR);
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(1, userIdToExclude);
                statement.setString(2, userIdToExclude);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("userId");
                    String first = rs.getString("firstName");
                    String last = rs.getString("lastName");
                    java.sql.Timestamp timeStamp = rs.getTimestamp("lastPostAt");
                    boolean followed = rs.getBoolean("isFollowed");

                    String lastActiveDateFormat = Utility.foramtTime(timeStamp);
                    

                    FollowableUser user = new FollowableUser(
                            id,
                            first,
                            last,
                            followed,
                            lastActiveDateFormat
                    );

                    followableUsers.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("PeopleService SQLState=" + e.getSQLState() + " code=" + e.getErrorCode());
    e.printStackTrace();
            throw new RuntimeException("Error retrieving followable users", e);
        }

        return followableUsers;
    }

}
