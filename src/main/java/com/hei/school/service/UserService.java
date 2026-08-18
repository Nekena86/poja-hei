package com.hei.school.service;

import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.CreateUserRequest;
import com.hei.school.service.dto.UserView;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserView create(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("A user already exists with email " + request.email());
    }
    if (request.role() == Role.STUDENT && request.promotionYear() == null) {
      throw new IllegalArgumentException("A student must have a promotionYear");
    }
    if (request.role() != Role.STUDENT && request.promotionYear() != null) {
      throw new IllegalArgumentException("Only students belong to a promotion");
    }
    User saved =
        userRepository.save(
            User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .promotionYear(request.promotionYear())
                .build());
    return toView(saved);
  }

  @Transactional(readOnly = true)
  public List<UserView> listStudents(int promotionYear) {
    return userRepository.findByRoleAndPromotionYear(Role.STUDENT, promotionYear).stream()
        .map(UserService::toView)
        .toList();
  }

  public static UserView toView(User user) {
    return new UserView(
        user.getId(),
        user.getRole() == Role.STUDENT ? StudentIdentifier.stdOf(user) : null,
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getRole(),
        user.getPromotionYear());
  }
}
