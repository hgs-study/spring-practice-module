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
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    public void testMember(){
        Member testUser = new Member("testUser");
        Member savedMember = memberJpaRepository.save(testUser);

        Member findMember = memberJpaRepository.find(savedMember.getId());

        assertThat(findMember.getId()).isEqualTo(testUser.getId());
        assertThat(findMember.getUserName()).isEqualTo(testUser.getUserName());
        assertThat(findMember).isEqualTo(testUser);
    }
}