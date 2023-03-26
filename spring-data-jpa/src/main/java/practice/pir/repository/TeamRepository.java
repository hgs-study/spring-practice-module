package practice.pir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.pir.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
