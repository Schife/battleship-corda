package webserver;

import java.io.Serializable;

public class StartGameRequest implements Serializable {
    private String id;

    public StartGameRequest() {}

    public StartGameRequest(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

