package webserver;

import java.io.Serializable;

public class JoinGameRequest implements Serializable {
    private String id;

    public JoinGameRequest() {}

    public JoinGameRequest(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
