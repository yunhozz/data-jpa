package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //select m from Member m where m.username = :username and m.age > :age
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //메소드 명으로 쿼리 생성
}
