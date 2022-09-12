package bgu.spl.net.impl.BGSServer.Objects;

public class Filter {
    private final String[] forbiddenWords = {/*Insert forbidden words as separate strings within this array*/};

    public String filter(String content) {
        String filtered = (" " + content + " ");
        for (String word: forbiddenWords) {
            word = (" " + word + " ");
            filtered = filtered.replaceAll(word," <filtered> ");
            filtered = filtered.replaceAll(word," <filtered> ");
        }
        filtered = filtered.substring(1,filtered.length()-1);
        return filtered;
    }

}
