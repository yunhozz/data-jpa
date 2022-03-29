package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //select m from Member m where m.username = :username and m.age > :age
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //메소드 명으로 쿼리 생성, 이름이 길어지면 더러워진다.

    List<Member> findByUsername(@Param("username") String username); //using NamedQuery, 어플리케이션 동작 시점에 오류 검출, 거의 사용 x

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age); //using Query annotation, 많이 사용
}
