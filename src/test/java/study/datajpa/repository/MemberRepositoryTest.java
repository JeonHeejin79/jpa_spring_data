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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testMember() {
        System.out.println("memberRepository.getClass() = " + memberRepository.getClass());
        Member member = new Member("memberA");

        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        // 같은 트랜젝션안에서는 영속성 컨텍스트에서 같은 인스턴스인것이 보장 된다. -> 1차캐시
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();

        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThanTest() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy() {
        List<Member> helloBy = memberRepository.findTop3HelloBy();
    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");

        assertThat(result).isEqualTo(m1);
    }

    @Test
    public void testNamedQuery2() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testNamedQuery3() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();

        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    // @Query 값 dto 조회하기
    @Test
    public void testNamedQuery5() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);

        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    // 파라미터 바인딩
    @Test
    public void testNamedQuery6() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);

        memberRepository.save(m1);

        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member byName : byNames) {
            System.out.println("byName = " + byName);
        }
    }

    // 반환타입
    @Test
    public void testNamedQuery7() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        // 없어도 null 이 아님
        List<Member> aaa = memberRepository.findListByUsername("AAA");

        // 단건은 없으면 null
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
        System.out.println("aaa1 = " + aaa1);

        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");

        Member member = aaa2.get();
        System.out.println("member = " + member);
    }

    @Test
    public void pageing() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        int offset = 0;;
        int limit = 3;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // 0 번쨰에서 3개 가져올때 slice 는 3개 + 1 을해서 4를 가져온다. totalCount 는 없다.
        // Slice<Member> page = memberRepository.findByAge(age, pageRequest);

        // then
        List<Member> content = page.getContent(); // 데이터

        // long totalElements = page.getTotalElements(); // totalCount

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        // System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

        // ***** 중요 ******
        // entity 는 controller 로 반환해서 외부에 노출시키면 안된다.
        // dto 로 변환해야 한다.

        // dto 로 쉽게 변환하는방법 (page 를 유지하면서 dto 로 변환가능)
        Page<MemberDto> dtoMember =
                page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        // response body 로 반환하면 된다.

        // *** 주의 ***
        // page 는 1부터가아니라 0부터 이다.
    }

    @Test
    public void bulkUpdate() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when
        // @Modifying(clearAutomatically = true) 를 적용해야 가져올떄 업데이트된 객체가 나온다.
        int resultCount = memberRepository.bulkAgePlus(20);

        // entityManager.flush(); // 남아있는 쿼리가 날라가고
        // entityManager.clear(); // 연속성 컨텍스트 초기화 된다.

        List<Member> member5 = memberRepository.findByUsername("member5");
        Member member = member5.get(0);
        int age = member.getAge();
        System.out.println("member5 age = " + age);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    // entity graph 객체그래프를 역어허 한번에 조회 (= fetch join)
    @Test
    public void findMemberLazy() {
        // given
        // member1 -> teamA
        // member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        entityManager.flush();
        entityManager.clear();

        // when N + 1
        // select Member 1
        List<Member> members = memberRepository.findAll();
        // List<Member> members = memberRepository.findMemberFetchJoin();
        // memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    // hint && lock
    @Test
    public void queryHint() {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));

        entityManager.flush(); // 쿼리가 날라감
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // when
        // @QueryHints 사용 ->  @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        Member findMember2 = memberRepository.findLockByUsername("member1");

        findMember.setUsername("member2"); // 변경

        entityManager.flush(); // 변경감지 동작 -> 더티체킹안함 (@QueryHints 사용시)

    }
    
    @Test
    public void callCustom() {
        var result = memberRepository.findMemberCustom();
    }

    // open projection -> 엔티티를 다 다겨와서 처리하는것
    //    @Value("#{target.username + ' ' + target.age}")
    //    String getUsername();
    // close projection -> 필요한것을 정확히 가져오는것
    //    String getUsername();
    @Test
    public void projections() {
        // given
        // member1 -> teamA
        Team teamA = new Team("teamA");

        teamRepository.save(teamA);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        entityManager.persist(member1);
        entityManager.persist(member2);

        entityManager.flush();
        entityManager.clear();

        List<UsernameOnly> member11 = memberRepository.findProductionsByUsername("member1");

        for (UsernameOnly usernameOnly : member11) {
            System.out.println("usernameOnly = " + usernameOnly);
            System.out.println("usernameOnly.getUsername() = " + usernameOnly.getUsername());
        }

    }
}