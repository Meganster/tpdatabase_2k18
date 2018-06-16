package forumdb.DAO;

import forumdb.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@Transactional
@Repository
public class UserDAO {
    static final Integer EMPTY_SQL_STRING_LENGTH = 17;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void create(@NotNull String email, @NotNull String nickname,
                       @NotNull String fullname, @NotNull String about) throws DataAccessException {
        jdbcTemplate.update("INSERT INTO \"User\" (email, nickname, fullname, about) VALUES (?, ?, ?, ?);",
                email, nickname, fullname, about);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateProfile(@NotNull String email, @NotNull String fullname,
                              @NotNull String about, @NotNull String nickname) throws DataAccessException {
        final Boolean conditionEmail = email != null && !email.isEmpty();
        final Boolean conditionAbout = about != null && !about.isEmpty();
        final Boolean conditionFullname = fullname != null && !fullname.isEmpty();

        final StringBuilder sql = new StringBuilder("UPDATE \"User\" SET");

        if (conditionEmail) {
            sql.append(" email='").append(email).append("'::citext");
        }

        if (conditionAbout) {
            if (conditionEmail) {
                sql.append(',');
            }
            sql.append(" about='").append(about).append("'::citext");
        }

        if (conditionFullname) {
            if (sql.length() > EMPTY_SQL_STRING_LENGTH) {
                sql.append(',');
            }
            sql.append(" fullname='").append(fullname).append("'");
        }

        if (sql.length() > EMPTY_SQL_STRING_LENGTH) {
            sql.append(" WHERE nickname='").append(nickname).append("'::citext;");
            jdbcTemplate.update(sql.toString());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User getUser(@NotNull String nickname) throws DataAccessException {
        return jdbcTemplate.queryForObject("SELECT * FROM \"User\" WHERE nickname = ?::CITEXT;",
                new Object[]{nickname}, new UserMapper());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ArrayList<User> getUsers(@NotNull String nickname, @NotNull String email) throws DataAccessException {
        return (ArrayList<User>) jdbcTemplate.query("SELECT * FROM \"User\" WHERE email = ?::CITEXT OR nickname = ?::CITEXT;",
                new Object[]{email, nickname}, new UserMapper());
    }

    public static class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            final User user = new User();
            user.setEmail(resultSet.getString("email"));
            user.setNickname(resultSet.getString("nickname"));
            user.setFullname(resultSet.getString("fullname"));
            user.setAbout(resultSet.getString("about"));
            user.setId(resultSet.getLong("id"));

            if (resultSet.wasNull()) {
                user.setAbout(null);
            }
            return user;
        }
    }
}
