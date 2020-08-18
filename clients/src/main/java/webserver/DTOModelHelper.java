package webserver;

import com.r3.battleship.schemas.GameDTO;
import com.r3.battleship.schemas.GamePlayersDTO;

import java.util.ArrayList;
import java.util.List;

public class DTOModelHelper {

    public static Game toGame(GameDTO gameDTO, String ourPlayer) {

        List<String> players = new ArrayList<>();
        for (GamePlayersDTO gamePlayersDTO : gameDTO.getGamePlayers()) {
            players.add(gamePlayersDTO.getGamePlayerName());
        };
        String status = gameDTO.getGameStatus().name();

        Game game = new Game(gameDTO.getGameId().toString(), players, (!players.contains(ourPlayer)), (players.size() == gameDTO.getNumberOfPlayers()), GameStatus.valueOf(status));

        return game;
    }
}
