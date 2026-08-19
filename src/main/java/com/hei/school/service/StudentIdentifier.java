package com.hei.school.service;

import com.hei.school.entity.User;

public final class StudentIdentifier {

  private static final int LENGTH = 8;

  private StudentIdentifier() {}

  public static String stdOf(User student) {
    String hex = student.getId().toString().replace("-", "");
    return hex.substring(hex.length() - LENGTH).toUpperCase();
  }
}
