package webserver;

import java.io.Serializable;

public enum GameStatus implements Serializable {
    CREATED,
    ACTIVE,
    SHIPS_PLACED,
    DONE
}
