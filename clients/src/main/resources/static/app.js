"use strict";

function showLoader() {
    $("#loader").show()
}

function hideLoader() {
    $("#loader").hide()
}

$.urlParam = function(name){
	var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
	return results[1] || 0;
}

/***** Home page capabilities *****/

function populateGamesTable() {
    var gamesTableId = "games-table"
    $.ajax({
        url: "/battleship/games/",
        success: function(result) {
            var startedGames = result.filter(game => game.status == "ACTIVE")
            if (startedGames.length > 0) {
                showLoader();
                var firstStartedGame = result[0]
                location.replace("/game.html?id=" + firstStartedGame.id)
                hideLoader();
            } else {
                var resultSize = result.length
                var bodyRowCount = $("#"+gamesTableId+" tbody tr").length;
                if (bodyRowCount < resultSize) {
                    showLoader();
                    $("#"+gamesTableId).find("tbody").html("");
                    renderGames(gamesTableId, result)
                    hideLoader();
                }
            }
        }
    });
}

function createNewGame(gamesTableId) {
    $.ajax({
        url: "/battleship/createGame",
        method: "POST",
        contentType: "application/json",
        dataType: 'json',
        beforeSend: function() {
            showLoader();
        },
        success: function( result ) {
            hideLoader();
            var newGames = []
            newGames.push(result)
            renderGames(gamesTableId, newGames)
        }
    });
}

function renderGames(gamesTableId, gamesPayload) {
    var tableItems = gamesPayload.map(game => {
        var gameIdColumn = $("<td>").text(game.id);
        var playersList = $("<ol>");
        var players = game.players.map(player => $("<li>").text(player));
        playersList.append(players);
        var isJoinable = game.joinable;
        var isStartable = game.startable;
        if (isJoinable == true) {
            var joinButton = $("<button>", {
                text: "Join Game",
                class: "btn btn-success",
                click: function() {
                    $.ajax({
                            url: "/battleship/" + game.id + "/joinGame",
                            method: "POST",
                            contentType: "application/json",
                            beforeSend: function() {
                                showLoader();
                            },
                            success: function( result ) {
                                hideLoader();

                                //remove game from table and re-insert the updated one.
                                $("#" + game.id).remove()
                                var newGames = []
                                newGames.push(result)
                                renderGames(gamesTableId, newGames)
                            }
                        });
                }
            });
            var joinColumn = $("<td>").append(joinButton);
        } else {
            var joinColumn = $("<td>").append("-");
        }

        if (isStartable == true) {
            var request = {
                id: game.id
            }
            var startButton = $("<button>", {
                text: "Start Game",
                class: "btn btn-success",
                click: function() {
                    var url = "/battleship/" + game.id + "/startGame";
                    $.ajax({
                            url: url,
                            method: "POST",
                            contentType: "application/json",
                            data: JSON.stringify(request),
                            dataType: 'json',
                            beforeSend: function() {
                                showLoader();
                            },
                            success: function( result ) {
                                hideLoader();

                                location.replace("/game.html?id=" + game.id)
                            }
                        });
                }
            });
            var startColumn = $("<td>").append(startButton);
        } else {
            var startColumn = $("<td>").append("-");
        }
        var playersJoinedColumn = $("<td>").append(playersList);

        var gameRow = $("<tr>", { id: game.id });
        gameRow.append(gameIdColumn);
        gameRow.append(playersJoinedColumn);
        gameRow.append(joinColumn);
        gameRow.append(startColumn);

        return gameRow;
    });
    tableItems.forEach(function(element) {
        $('#' + gamesTableId + " tbody").append(element);
    });
}

/***** Game board page capabilities *****/

function populateGameBoard(gameId) {
    $.ajax({
        url: "/battleship/" + gameId + "/gameState",
        beforeSend: function() {
            showLoader();
        },
        success: function(result) {
            hideLoader();
            renderBoard(result);
        }
    });
}

function renderBoard(payload) {
    var players = Object.keys(payload.playerState);
    var mapRows = 5;
    var mapColumns = 5;

    // Draw maps
    for(var playerIndex = 1; playerIndex <= players.length; playerIndex++) {
        var playerName = players[playerIndex-1]
        var playerMap = $("<div>", { id: playerName, class: "container"});

        var playerLabel = playerName;
        var playerCell = $("<div class='row'>").text(playerLabel);
        playerMap.append(playerCell);

        for(var row = 1; row <= mapRows; row++) {
            var rowDiv = $("<div class='row'>");
            for(var column = 1; column <= mapColumns; column++) {
                var cell = $("<div>", { "data-row": row, "data-column": column, "data-player": playerName, class: "grid_cell" });
                rowDiv.append(cell);
            }
            playerMap.append(rowDiv);
        }
        $("#game_board").append(playerMap);
    }

    // Draw ship placement
    if(payload.status == "SHIPS_PLACED") {
        var shipStartRow = parseInt(payload.placement.start.x);
        var shipStartColumn = parseInt(payload.placement.start.x);
        var shipEndRow = parseInt(payload.placement.end.x);
        var shipEndColumn = parseInt(payload.placement.end.y);

        var myMap = $("[id='" + payload.identity + "']");
        if(shipStartRow == shipEndRow) {
            // Ship aligned horizontally
            for(var column = shipStartColumn; column <= shipEndColumn; column++) {
                myMap.find("[data-row='" + shipStartRow + "'][data-column='" + column + "']").css('background-color', 'blue');
            }
        } else {
            // Ship aligned vertically
            for(var row = shipStartRow; row <= shipEndRow; row++) {
                myMap.find("[data-row='" + shipStartRow + "'][data-column='" + shipStartColumn + "']").css('background-color', 'blue')
            }
        }
    }
}


const POLL_INTERVAL = 5000;

const poll = async ({ fn, interval}) => {
    console.log('Start poll...');
    let attempts = 0;

    const executePoll = async () => {
        console.log('- poll');
        const result = await fn();
        setTimeout(executePoll, interval);
    };

    return new Promise(executePoll);
};