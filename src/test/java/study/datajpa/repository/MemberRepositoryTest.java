package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("B", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("B");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("A");

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("A", 10);

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> userNameList = memberRepository.findUserNameList();

        for (String username : userNameList) {
            System.out.println("username = " + username);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);
        
        Member m1 = new Member("A", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("A", "B"));

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {
        Member m1 = new Member("A", 10);
        Member m2 = new Member("B", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findListByUsername("A"); //null 일 때 빈 컬렉션 반환 -> 검증 필요 x
        System.out.println("members = " + members);

        Member member = memberRepository.findMemberByUsername("B"); //null 가능성 o -> Optional
        System.out.println("member = " + member);

        Optional<Member> optionalMember = memberRepository.findOptionalByUsername("C"); //Optional.empty
        System.out.println("optionalMember = " + optionalMember);
    }

    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest); //totalCount 쿼리도 생성 -> 성능 저하 우려 -> countQuery
//        Slice<Member> slice = memberRepository.findByAge(age, pageRequest);
        Page<MemberDto> pageDto = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null)); //dto 로 변환

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20); //20살 이상 +1, 벌크 연산
//        em.flush(); //flush 함으로써 연산 결과 반영
//        em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        //(flush 사용 x) age = 40 -> 영속성 컨텍스트에는 기존 age 정보가 남아있음
        //(flush 사용 o) age = 41
        System.out.println("member5 = " + member5);

        /*
          <CASE1: 엔티티를 먼저 조회해둔 상황>
          1. 엔티티를 조회함
           엔티티 상태:(id=1, age=40) | DB 상태(id=1, age=40)

          2. 벌크 연산으로 +1
           엔티티 상태:(id=1, age=40) | DB 상태(id=1, age=41)

          3. findByUsername 조회
           select m from Member; 로 조회했지만, 결과 id=1 값이 이미 영속성 컨텍스트에 있으므로 DB 에서 조회한 값을 버리고
           영속성 컨텍스트에서 조회한 값을 반환함

          결과적으로 조회된 최종 데이터는 (id=1, age=40)
         */

        /*
          <CASE2: 엔티티를 먼저 조회하지 않은 상황>
        1. 영속성 컨텍스트가 관련하는 엔티티가 없음
         엔티티 없음 | DB 상태(id=1, age=40)

        2. 벌크 연산으로 +1
         엔티티 없음 | DB 상태(id=1, age=41)

        3. findByUsername 조회

          결과적으로 엔티티 상태:(id=1, age=41) | DB 상태(id=1, age=41)
         */

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
//        List<Member> members = memberRepository.findAll(); //select Member (Team x) -> N + 1 문제 발생!!
//        List<Member> members = memberRepository.findMemberFetchJoin(); //select Member, Team -> 객체 그래프 생성
//        List<Member> members = memberRepository.findMemberEntityGraph();
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");


        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("teamClass = " + member.getTeam().getClass());
            System.out.println("team = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
//        Member findMember = memberRepository.findById(member1.getId()).get();
        Member findMember = memberRepository.findReadOnlyByUsername("member1"); //변경 감지 체크 x -> update x
        findMember.setUsername("member2"); //update
        em.flush();
    }

    @Test
    public void lock() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void specBasic() {
        //given
        Team team = new Team("teamA");
        em.persist(team);

        Member member1 = new Member("member1", 10, team);
        Member member2 = new Member("member2", 10, team);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        //when
        Specification<Member> spec = MemberSpec.username("member1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void queryByExample() {
        //given
        Team team = new Team("teamA");
        em.persist(team);

        Member member1 = new Member("member1", 0, team);
        Member member2 = new Member("member2", 0, team);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        //when
        Member findMember = new Member("member1");
        Team findTeam = new Team("teamA");
        findMember.setTeam(findTeam);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");
        Example<Member> example = Example.of(findMember, matcher);

        List<Member> result = memberRepository.findAll(example);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    public void projections() {
        //given
        Team team = new Team("teamA");
        em.persist(team);

        Member member1 = new Member("member1", 0, team);
        Member member2 = new Member("member2", 0, team);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        //when
//        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("member1");
//        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("member1");
//        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("member1", UsernameOnlyDto.class);
        List<NestedClosedProjections> result = memberRepository.findProjectionsByUsername("member1", NestedClosedProjections.class);

//        for (UsernameOnlyDto usernameOnlyDto : result) {
//            System.out.println("usernameOnly = " + usernameOnlyDto);
//        }

        for (NestedClosedProjections nestedClosedProjections : result) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);

            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName = " + teamName);
        }

        /*
        프로젝션 대상이 root entity 면 유용하다.
        프로젝션 대상이 root entity 를 넘어가면 JPQL SELECT 최적화가 안된다!
        실무의 복잡한 쿼리를 해결하기에는 한계가 있다. -> QueryDSL
         */
    }

    @Test
    public void nativeQuery() {
        //given
        Team team = new Team("teamA");
        em.persist(team);

        Member member1 = new Member("member1", 0, team);
        Member member2 = new Member("member2", 0, team);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        //when
//        Member result = memberRepository.findByNativeQuery("member1");
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();

        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }
    }
}