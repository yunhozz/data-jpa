package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    //파라미터 인젝션 생략 가능, 권장 x -> 조회용으로만 사용
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    //웹에서의 페이징
    //http://localhost:8080/members?page=0&size=5&sort=id,desc -> 이런식으로 웹 주소에 조건식을 추가할 수 있게 된다. (default 20개)
    //application.yml 또는 @PageableDefault 를 이용하여 default 값을 조절할 수 있다.
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "id") Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member));

        return map;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
