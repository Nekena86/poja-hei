package com.hei.school.repository;

import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  List<User> findByRoleAndPromotionYear(Role role, Integer promotionYear);

  /**
   * Derived queries cannot project "distinct" onto a single property — they select whole entities —
   * so the projection has to be written out.
   */
  @Query(
      "select distinct u.promotionYear from User u "
          + "where u.role = :role and u.promotionYear is not null "
          + "order by u.promotionYear desc")
  List<Integer> findDistinctPromotionYears(@Param("role") Role role);
}
