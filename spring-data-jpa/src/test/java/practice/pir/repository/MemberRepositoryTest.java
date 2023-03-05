package practice.pir.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import practice.pir.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    public void testMember(){
        Member testA = new Member("testA");
        Member save = memberRepository.save(testA);

        Member findMember = memberRepository.findById(save.getId()).get();

        assertEquals(findMember.getId(), testA.getId());
        assertEquals(findMember.getUserName(), testA.getUserName());
        assertEquals(findMember, testA);
    }
}