public class Token {
    String type;
    String value;

    Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}
