package entity;

import com.heybcat.tightlyweb.sql.annotation.Table;
import com.heybcat.tightlyweb.sql.annotation.TableId;

@Table(name = "job")
public class Job {

    @TableId
    private Integer id;

    private String name;

    private String description;

}
