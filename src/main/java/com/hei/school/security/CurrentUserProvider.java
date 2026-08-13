package com.hei.school.security;

import com.hei.school.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  public User getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
      throw new IllegalStateException("No authenticated user in context");
    }
    return details.getUser();
  }
}
