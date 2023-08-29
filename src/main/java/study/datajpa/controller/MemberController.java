package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        // 스프링 data jpa 가 해주는 기능 - 도메인 클래스 컨버터, 권장하지 않는다.
        // 도메인 클래스 컨버터도 레파지토리를 사용해서 엔티티를 찾음
        // 조회용으로만 사용해야 한다. 트랜잭션이 없는 범위에서 엔티티를 조회햇으므로 엔티티를 변경해도 db 에 반영되지 않는다. 
        return member.getUsername();
    }

    @PostConstruct
    public void init() {
        memberRepository.save(new Member("userA"));
    }
}
