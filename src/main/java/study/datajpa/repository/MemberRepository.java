package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
리포지토리 interface 를 여러개 만들어 확장 가능
JpaSpecificationExecutor 은 사용하지 말자!
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member> {

    //메소드 명으로 쿼리 생성, 이름이 길어지면 더러워진다.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //select m from Member m where m.username = :username and m.age > :age
    List<Member> findTop3HelloBy();

    //using NamedQuery, 어플리케이션 동작 시점에 오류 검출, 거의 사용 x
    List<Member> findByUsername(@Param("username") String username);

    //using Query annotation, 많이 사용
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUserNameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    //반환 타입 (by 바로 앞에 어떤 단어가 와도 상관 x)
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    //페이징 (Page, Slice)
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable); //데이터의 양이 많을 땐 countQuery 로 분리해야한다. (단, 다대일 한정)

    //벌크성 수정
    @Modifying(clearAutomatically = true) //자동으로 영속성 컨텍스트를 flush, clear 해준다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    //페치 조인 (Member -> Team) : 간단한 JPQL 을 작성해야 하는 것이 귀찮다 -> @EntityGraph
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
//    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    //JPA Hint
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true")) //읽기 전용 -> 내부적으로 성능 최적화
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE) //for update
    List<Member> findLockByUsername(String username);

    //프로젝션
//    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);
    List<UsernameOnlyDto> findProjectionsByUsername(@Param("username") String username);
    <T> List<T> findProjectionsByUsername(@Param("username") String username, Class<T> type);

    //네이티브 쿼리
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    @Query(value = "select m.member_id as id, m.username, t.name as teamName from member m left join team t",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
