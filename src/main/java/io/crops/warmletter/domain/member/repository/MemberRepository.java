package io.crops.warmletter.domain.member.repository;

import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialUniqueId(String socialUniqueId);
    boolean existsByZipCode(String ZipCode);

    @Query("select new io.crops.warmletter.domain.member.dto.response.MeResponse(" +
            "m.zipCode, m.temperature.value, sa.provider, m.email, m.warningCount) " +
            "from Member m join m.socialAccounts sa " +
            "where m.id = :id " +
            "order by sa.id asc limit 1")
    Optional<MeResponse> findMeById(Long id);

    // 벌크 연산
    @Modifying
    @Query("UPDATE Member m SET m.isActive = false WHERE m.isActive = true AND m.warningCount >= 3")
    int suspendMembersWithExcessiveWarnings();
}
