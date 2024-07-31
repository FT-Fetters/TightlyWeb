package sql.mapper;

import com.heybcat.tightlyweb.sql.annotation.LiteMapper;
import com.heybcat.tightlyweb.sql.annotation.Select;
import java.util.List;
import sql.entity.User;

@LiteMapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE age >= 18")
    List<User> selectAdultUser(int a);


    @Select(
        value = "SELECT * FROM user ?WHERE $0$",
        expressions = {
            "(?name) -> { `name` = #{name} }"
        }
    )
    User getByName(String name);

    @Select(
        value = "SELECT * from user ?WHERE $0$",
        expressions = {
            "(?names) -> { `name` in #{names} }"
        }
    )
    List<User> listNameIn(List<String> names);

}
