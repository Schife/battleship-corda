package webserver;

import java.io.Serializable;
import java.util.List;

public class Game implements Serializable {
    private String id;
    private List<String> players;
    private boolean joinable;
    private boolean startable;
    private GameStatus status;

    public Game(String id, List<String> players, boolean joinable, boolean startable, GameStatus status) {
        this.id = id;
        this.players = players;
        this.joinable = joinable;
        this.startable = startable;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public boolean isJoinable() {
        return joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    public boolean isStartable() {
        return startable;
    }

    public void setStartable(boolean startable) {
        this.startable = startable;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
