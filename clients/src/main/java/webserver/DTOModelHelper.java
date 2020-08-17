package webserver;

import com.r3.battleship.schemas.GameDTO;
import com.r3.battleship.schemas.GamePlayersDTO;

import java.util.ArrayList;
import java.util.List;

public class DTOModelHelper {

    public static Game toGame(GameDTO gameDTO) {

        List<String> players = new ArrayList<>();
        for (GamePlayersDTO gamePlayersDTO : gameDTO.getGamePlayers()) {
            players.add(gamePlayersDTO.getGamePlayerName());
        };
        String status = gameDTO.getGameStatus().name();
        Game game = new Game(gameDTO.getGameId().toString(), players, (players.size()<4), (players.size() == 4), GameStatus.valueOf(status));

        return game;
    }
}
