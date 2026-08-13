package com.hei.school.repository;

import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  List<User> findByRoleAndPromotionYear(Role role, Integer promotionYear);

  List<Integer> findDistinctPromotionYearByRoleOrderByPromotionYearDesc(Role role);
}
