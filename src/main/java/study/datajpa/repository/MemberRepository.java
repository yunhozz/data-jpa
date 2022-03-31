package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //메소드 명으로 쿼리 생성, 이름이 길어지면 더러워진다.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //select m from Member m where m.username = :username and m.age > :age

    //using NamedQuery, 어플리케이션 동작 시점에 오류 검출, 거의 사용 x
    List<Member> findByUsername(@Param("username") String username);

    //using Query annotation, 많이 사용
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUserNameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();
}
