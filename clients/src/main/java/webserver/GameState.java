package webserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameState implements Serializable {
    private Placement placement;
    private String identity;
    private boolean myTurn;
    private GameStatus status;
    private HashMap<String, Boolean> playerState;
    private HashMap<String, HashMap<Coordinate, String>> shots;
    private String winner;
    private int currentRound;
    private Map<String, Placement> playersShipLocations;

    public GameState() {

    }

    public GameState(Placement placement,
                     String identity,
                     boolean myTurn,
                     GameStatus status,
                     HashMap<String, Boolean> playerState,
                     HashMap<String, HashMap<Coordinate, String>> shots,
                     String winner,
                     Map<String, Placement> playersShipLocations,
                     int currentRound) {
        this.placement = placement;
        this.identity = identity;
        this.myTurn = myTurn;
        this.status = status;
        this.playerState = playerState;
        this.shots = shots;
        this.winner = winner;
        this.playersShipLocations = playersShipLocations;
        this.currentRound = currentRound;
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

    public GameStatus getStatus() {
        return this.status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
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

    public String getWinner() {
        return this.winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Map<String, Placement> getPlayersShipLocations() {
        return this.playersShipLocations;
    }

    public void setPlayersShipLocations(Map<String, Placement> playersShipLocations) {
        this.playersShipLocations = playersShipLocations;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
}
