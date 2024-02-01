package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(false)
@Slf4j
class MemberRepositoryTest {

	@Autowired MemberRepository memberRepository;
	@Autowired TeamRepository teamRepository;
	@PersistenceContext EntityManager em;

	@Test
	void testMember() {
		log.info("memberRepository.getClass() = {}", memberRepository.getClass());

	    // given
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		// when
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		// then
	    assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	void basicCRUD() {
		// given
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

		// 카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		// 삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deletedCount = memberRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}
	
	@Test
	void testQuery() {
	    // given
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findUser("AAA", 10);

	    // then
	    assertThat(result.get(0)).isEqualTo(m1);
	    
	}

	@Test
	void findUsernameList() {
	    // given
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);

		memberRepository.save(m1);
		memberRepository.save(m2);

		List<String> usernameList = memberRepository.findUsernameList();
		for (String s : usernameList) {
			log.info("s = {}", s);
		}
	}

	@Test
	void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);
		memberRepository.save(m1);

		List<MemberDto> usernameList = memberRepository.findMemberDto();
		for (MemberDto dto : usernameList) {
			log.info("dto = {}", dto);
		}
	}

	@Test
	void findByNames() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		for (Member member : result) {
			System.out.println("member = " + member);
		}
	}

	@Test
	void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		Optional<Member> findMember = memberRepository.findOptionalByUsername("AAA");
		System.out.println("findMember = " + findMember);

	}

	@Test
	void paging() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = of(0, 3, by(DESC, "username"));

		// when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);

		// Entity를 Dto로 변환
		Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

		// then
		List<Member> content = page.getContent();
		long totalCount = page.getTotalElements();

		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getTotalElements()).isEqualTo(5);
		assertThat(page.getNumber()).isEqualTo(0);
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}

	@Test
	void bulkUpdate() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		// when
		int resultCount = memberRepository.bulkAgePlus(20);
//		em.flush();
//		em.clear();

		Member result = memberRepository.findMemberByUsername("member5");
		System.out.println("result = " + result);

		// then
		assertThat(resultCount).isEqualTo(3);

	}

	@Test
	void findMemberLazy() {
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

		em.flush();
		em.clear();

		List<Member> members = memberRepository.findEntityGraphByUsername("member1");

		for (Member member : members) {
			System.out.println("member = " + member.getUsername());
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
			System.out.println("member.team = " + member.getTeam().getName());
		}
	}

	@Test
	void queryHint() {
	    // given
		Member member1 = memberRepository.save(new Member("member1", 10));
		em.flush(); // DB에 insert SQL 전송
		em.clear(); // 영속성 컨텍스트에 있는 결과를 DB에 동기화

	    // when
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");

		em.flush();

	}

	@Test
	void lock() {
		// given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush(); // DB에 insert SQL 전송
		em.clear(); // 영속성 컨텍스트에 있는 결과를 DB에 동기화

		// when
		List<Member> result = memberRepository.findLockByUsername("member1");
	}

	@Test
	void callCustom() {
	    // given
		List<Member> result = memberRepository.findMemberCustom();
	}
}