package practice.pir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.pir.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
