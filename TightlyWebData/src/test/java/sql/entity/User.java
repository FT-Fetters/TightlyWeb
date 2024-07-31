package sql.entity;

import com.heybcat.tightlyweb.sql.annotation.Table;
import com.heybcat.tightlyweb.sql.annotation.TableId;
import lombok.Data;
import lombok.ToString;

@Table(name = "`User`")
@Data
@ToString
public class User {

    @TableId
    private Integer id;

    private String name;

    private Integer age;

}
