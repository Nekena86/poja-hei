package com.hei.school.service;

import com.hei.school.entity.User;

/**
 * The "STD" shown on transcripts and graduate lists. Derived from the student's UUID so it is
 * stable without adding a school-issued number to the model.
 *
 * <p>Taken from the <em>end</em> of the UUID: that part varies between students even when the ids
 * were assigned in a batch and therefore share a prefix.
 */
public final class StudentIdentifier {

  private static final int LENGTH = 8;

  private StudentIdentifier() {}

  public static String stdOf(User student) {
    String hex = student.getId().toString().replace("-", "");
    return hex.substring(hex.length() - LENGTH).toUpperCase();
  }
}
