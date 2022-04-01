package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

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

        List<Member> members = memberRepository.findListByUsername("A"); //null 상황을 가정할 필요 x
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
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null)); //dto 로 변환

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
}