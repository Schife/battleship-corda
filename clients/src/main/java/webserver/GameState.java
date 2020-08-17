package webserver;

import java.io.Serializable;
import java.util.HashMap;

public class GameState implements Serializable {
    private Placement placement;
    private String identity;
    private boolean myTurn;
    private HashMap<String, Boolean> playerState;
    private HashMap<String, HashMap<Coordinate, String>> shots;

    public GameState(Placement placement, String identity, boolean myTurn, HashMap<String, Boolean> playerState, HashMap<String, HashMap<Coordinate, String>> shots) {
        this.placement = placement;
        this.identity = identity;
        this.myTurn = myTurn;
        this.playerState = playerState;
        this.shots = shots;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public HashMap<String, Boolean> getPlayerState() {
        return playerState;
    }

    public void setPlayerState(HashMap<String, Boolean> playerState) {
        this.playerState = playerState;
    }

    public HashMap<String, HashMap<Coordinate, String>> getShots() {
        return shots;
    }

    public void setShots(HashMap<String, HashMap<Coordinate, String>> shots) {
        this.shots = shots;
    }
}
