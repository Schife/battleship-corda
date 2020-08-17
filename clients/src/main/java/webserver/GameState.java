package webserver;

import java.io.Serializable;
import java.util.HashMap;

public class GameState implements Serializable {
    private Placement placement;
    private boolean myTurn;
    private HashMap<String, Boolean> playerState;
    private HashMap<Coordinate, String> shots;

    public GameState(Placement placement, boolean myTurn, HashMap<String, Boolean> playerState, HashMap<Coordinate, String> shots) {
        this.placement = placement;
        this.myTurn = myTurn;
        this.playerState = playerState;
        this.shots = shots;
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

    public HashMap<Coordinate, String> getShots() {
        return shots;
    }

    public void setShots(HashMap<Coordinate, String> shots) {
        this.shots = shots;
    }
}
