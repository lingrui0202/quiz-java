package com.lingrui.quiz.mapper;

import com.lingrui.quiz.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户 Mapper。
 *
 * 这一套用注解写 SQL（@Select / @Insert / @Update），
 * 和 QuestionMapper 的 XML 写法形成对比——两种都是 MyBatis 的常规用法。
 *
 * 全表都带上了 is_delete = 0 的过滤：
 * 逻辑删除只是把标记位置 1，数据还在表里，查询必须自己排除掉，
 * 否则"删了还能查到"——这是逻辑删除最容易漏的一步。
 */
public interface UserMapper {

    String COLUMNS = "id, username, password_hash, email, is_delete, created_at";

    @Select("SELECT " + COLUMNS + " FROM t_user WHERE is_delete = 0 ORDER BY id")
    List<User> findAll();

    @Select("SELECT " + COLUMNS + " FROM t_user WHERE id = #{id} AND is_delete = 0")
    User findById(@Param("id") Long id);

    @Select("SELECT " + COLUMNS + " FROM t_user WHERE username = #{username} AND is_delete = 0")
    User findByUsername(@Param("username") String username);

    /** 用户名是否已存在（9-5）。执行返回 > 0 视为存在。 */
    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND is_delete = 0")
    int countByUsername(@Param("username") String username);

    @Insert("""
            INSERT INTO t_user (username, password_hash, email)
            VALUES (#{username}, #{passwordHash}, #{email})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
            UPDATE t_user
            SET username = #{username},
                password_hash = #{passwordHash},
                email = #{email}
            WHERE id = #{id} AND is_delete = 0
            """)
    int update(User user);

    /** 逻辑删除：把 is_delete 置 1，不删数据（9-6） */
    @Update("UPDATE t_user SET is_delete = 1 WHERE id = #{id} AND is_delete = 0")
    int logicDeleteById(@Param("id") Long id);

    /** 逻辑删除：按用户名（9-6） */
    @Update("UPDATE t_user SET is_delete = 1 WHERE username = #{username} AND is_delete = 0")
    int logicDeleteByUsername(@Param("username") String username);

    /** 硬删除（9-6 留的练习）：真的从表里删掉 */
    @Delete("DELETE FROM t_user WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /** 有效用户总数（9-7 分页要用的 total） */
    @Select("SELECT COUNT(*) FROM t_user WHERE is_delete = 0")
    int countAll();

    @Select("SELECT COUNT(*) FROM t_user WHERE is_delete = 0 AND username LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(@Param("keyword") String keyword);

    /** 分页：LIMIT start, pageSize（9-7） */
    @Select("SELECT " + COLUMNS + " FROM t_user WHERE is_delete = 0 ORDER BY id LIMIT #{start}, #{pageSize}")
    List<User> selectPage(@Param("start") int start, @Param("pageSize") int pageSize);

    @Select("SELECT " + COLUMNS
            + " FROM t_user WHERE is_delete = 0 AND username LIKE CONCAT('%', #{keyword}, '%')"
            + " ORDER BY id LIMIT #{start}, #{pageSize}")
    List<User> selectPageByKeyword(@Param("start") int start,
                                   @Param("pageSize") int pageSize,
                                   @Param("keyword") String keyword);

    /** 用户名模糊查询，不分页（9-7 第二种查询方式） */
    @Select("SELECT " + COLUMNS
            + " FROM t_user WHERE is_delete = 0 AND username LIKE CONCAT('%', #{keyword}, '%') ORDER BY id")
    List<User> findByKeyword(@Param("keyword") String keyword);
}
