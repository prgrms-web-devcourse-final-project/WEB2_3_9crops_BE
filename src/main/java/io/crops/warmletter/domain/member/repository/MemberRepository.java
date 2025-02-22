package io.crops.warmletter.domain.member.repository;

import io.crops.warmletter.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialUniqueId(String socialUniqueId);
    boolean existsByZipCode(String ZipCode);
}
