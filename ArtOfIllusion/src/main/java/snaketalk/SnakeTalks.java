package snaketalk;

import java.util.Set;

public class SnakeTalks {
    private static final Set<Character> vovels = Set.of('a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y');

    public static void main(String... args) {
        String text = "Hello world";
        final StringBuffer result = new StringBuffer();
        text.chars().forEach(ch -> {
            Character cc = (char)ch;
            if(vovels.contains(cc)) result.append(cc);
            result.append(cc);
        });
        System.out.printf(result.toString());
    }
}
