package com.saunafs.bm;

import static com.saunafs.bm.present.PresentDescription.present;

import com.saunafs.bm.model.Json;
import com.saunafs.common.html.Serializer;

public class Present {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);
    var html = present(description);
    System.out.println(new Serializer().serialize(html));
  }
}
