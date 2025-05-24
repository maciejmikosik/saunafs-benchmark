package com.saunafs.bm;

import com.saunafs.bm.model.Json;

public class Present {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);

    System.out.println("<html> stub: " + description.benchmark + "</html>");
  }
}
