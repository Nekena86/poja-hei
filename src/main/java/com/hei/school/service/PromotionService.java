package com.hei.school.service;

import com.hei.school.entity.Role;
import com.hei.school.repository.UserRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PromotionService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<Integer> listPromotionYears() {
    return userRepository.findDistinctPromotionYears(Role.STUDENT);
  }
}
