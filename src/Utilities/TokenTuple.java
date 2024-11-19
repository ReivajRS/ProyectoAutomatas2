package Utilities;

public class TokenTuple {
    private String id, fullId;
    private Token token;

    public TokenTuple(String id, Token token) {
        this.id = id;
        this.fullId = id;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        return fullId;
    }

    public Token getToken() {
        return token;
    }

    public void setFullId(String fullId) {
        this.fullId = fullId;
    }
}
