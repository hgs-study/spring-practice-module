package practice.pir.repository;

import org.springframework.stereotype.Repository;
import practice.pir.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Member save(Member member){
        entityManager.persist(member);
        return member;
    }

    public Member find(Long id){
        return entityManager.find(Member.class,id);
    }
}
