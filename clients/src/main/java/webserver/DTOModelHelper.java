package webserver;

import com.r3.battleship.schemas.*;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DTOModelHelper {

    public static Game toGame(GameDTO gameDTO, String ourPlayer) {

        List<String> players = new ArrayList<>();
        for (GamePlayersDTO gamePlayersDTO : gameDTO.getGamePlayers()) {
            players.add(gamePlayersDTO.getGamePlayerName());
        }
        String status = gameDTO.getGameStatus().name();

        Game game = new Game(gameDTO.getGameId().toString(), players, (!players.contains(ourPlayer)), (players.size() == gameDTO.getNumberOfPlayers()), GameStatus.valueOf(status));

        return game;
    }


    public static Placement toPlacement(ShipPositionDTO shipPositionDTO) {
        Coordinate start = new Coordinate(shipPositionDTO.getFromX(), shipPositionDTO.getFromY());
        Coordinate end = new Coordinate(shipPositionDTO.getToX(), shipPositionDTO.getToY());
        Placement placement = new Placement(start, end);
        return placement;
    }

    public static GameState toGameState(List<HitPositionDTO> turns, String ourPlayer) {
        HashMap<String, HashMap<Coordinate, String>> shots = new HashMap<>();
        GameState gameState = new GameState();
        gameState.setIdentity(ourPlayer);

        GameDTO latestGame = null;
        if (CollectionUtils.isNotEmpty(turns)) {
            int currentRound = 0;
            boolean haveIMovedThisRound = false;

            for (HitPositionDTO hitPositionDTO :turns) {
                //have i moved this round?
                if (currentRound < hitPositionDTO.getRoundNum()) {
                    currentRound = hitPositionDTO.getRoundNum();
                    haveIMovedThisRound = false;
                }

                if (ourPlayer.equals(hitPositionDTO.getGamePlayer().getGamePlayerName())) {
                    haveIMovedThisRound = true;
                };

                //add players shots to map
                HashMap<Coordinate, String> hitMap = new HashMap<>();
                Coordinate coordinate = new Coordinate(hitPositionDTO.getHitX(), hitPositionDTO.getHitY());
                hitMap.put(coordinate, hitPositionDTO.getHitStatus().toString());
                shots.put(hitPositionDTO.getGamePlayer().getGamePlayerName(), hitMap);

                //latest game will have latest player states etc
                latestGame = hitPositionDTO.getGame();
            }

            HashMap<String, Boolean> playerStates = DTOModelHelper.getPlayerStates(latestGame.getGamePlayers());
            gameState.setPlayerState(playerStates);

            gameState.setStatus(GameStatus.valueOf(latestGame.getGameStatus().name()));
            gameState.setShots(shots);
            gameState.setCurrentRound(currentRound);

            gameState.setMyTurn(true);
            //gameState.setMyTurn(!haveIMovedThisRound);
            if (gameState.getStatus() == GameStatus.DONE) {
                gameState.setWinner(getWinner(playerStates));
            }
        }

        return gameState;
    }

    public static HashMap<String, Placement> toPlayersShipLocations(HashMap<String, ShipPositionDTO> shipPositionsDTO) {
        HashMap<String, Placement> playerPositions = new HashMap<>();

        for (String gamePlayer : shipPositionsDTO.keySet()) {
            Placement placement = DTOModelHelper.toPlacement(shipPositionsDTO.get(gamePlayer));
            playerPositions.put(gamePlayer, placement);
        }

        return playerPositions;
    }

    public static HashMap<String, Boolean> getPlayerStates(List<GamePlayersDTO> gamePlayersDTOList) {
        HashMap<String, Boolean> playerStates = new HashMap<>();

        for (GamePlayersDTO gamePlayer : gamePlayersDTOList) {
            boolean isAlive = PlayerStatus.ACTIVE == gamePlayer.getPlayerStatus();
            playerStates.put(gamePlayer.getGamePlayerName(), isAlive);
        }

        return playerStates;
    }

    private static String getWinner(HashMap<String, Boolean> playerStates) {
        int liveCount = 0;
        String alivePlayer = "";
        for (String player : playerStates.keySet()) {
            if (playerStates.get(player)) {
                alivePlayer = player;
                liveCount++;
            }
        }

        if (liveCount == 1) {
            return alivePlayer;
        }

        return null;
    }
}
