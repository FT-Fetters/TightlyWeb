package other;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class BaseMappingTest {

    @Test
    public void testParseCreateSql2FieldMap(){
        String sql = "CREATE TABLE IF NOT EXISTS `runoob_tbl`(\n"
            + "   `runoob_id` INT UNSIGNED AUTO_INCREMENT,\n"
            + "   `runoob_title` VARCHAR(100) NOT NULL,\n"
            + "   `runoob_author` VARCHAR(40) NOT NULL,\n"
            + "   `submission_date` DATE,\n"
            + "   PRIMARY KEY ( `runoob_id` )\n"
            + ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        Map<String, String> map = parseCreateSql2FieldMap(sql);

    }


    private Map<String, String> parseCreateSql2FieldMap(String createSql) {
        String[] typeKeywords = {
            // 数值类型
            "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT",
            "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC",

            // 日期和时间类型
            "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR",

            // 字符串类型
            "CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYBLOB", "BLOB",
            "MEDIUMBLOB", "LONGBLOB", "TINYTEXT", "TEXT", "MEDIUMTEXT",
            "LONGTEXT", "ENUM", "SET",

            // 空间数据类型
            "GEOMETRY", "POINT", "LINESTRING", "POLYGON", "MULTIPOINT",
            "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION",

            // JSON数据类型
            "JSON",

            // 位字段类型
            "BIT"
        };
        Map<String, String> fields = new HashMap<>(8);

        createSql = createSql.substring(createSql.indexOf("(") + 1);
        createSql = createSql.substring(0, createSql.lastIndexOf(")"));

        String splitPattern =",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        String[] splitFields = createSql.split(splitPattern);
        for (String splitField : splitFields) {
            splitField = splitField.trim();
            String[] word = splitField.split(" ");
            Arrays.stream(typeKeywords).forEach(typeKeyword -> {
                if (word.length > 1 && word[1].toUpperCase().contains(typeKeyword)){
                    fields.put(word[0], typeKeyword);
                }
            });
        }
        return fields;
    }

}
