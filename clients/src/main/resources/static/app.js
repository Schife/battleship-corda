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

/***** Home page capabilities *****/

function populateGamesTable() {
    var gamesTableId = "games-table"
    $.ajax({
        url: "/battleship/games/",
        success: function(result) {
            var resultSize = result.length
            var bodyRowCount = $("#"+gamesTableId+" tbody tr").length;
            if (bodyRowCount < resultSize) {
                showLoader();
                $("#"+gamesTableId).find("tbody").html("");
                renderGames(gamesTableId, result)
                hideLoader();
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

        if (isStartable == true && game.status === "CREATED") {
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

        if (game.status === "ACTIVE") {
            var request = {
                id: game.id
            }
            var playButton = $("<button>", {
                text: "Play Game",
                class: "btn btn-success",
                click: function() {
                    showLoader();
                    location.replace("/game.html?id=" + game.id)
                    hideLoader();
                }
            });
            var playColumn = $("<td>").append(playButton);
        } else {
            var playColumn = $("<td>").append("-");
        }

        var playersJoinedColumn = $("<td>").append(playersList);

        var gameRow = $("<tr>", { id: game.id });
        gameRow.append(gameIdColumn);
        gameRow.append(playersJoinedColumn);
        gameRow.append(joinColumn);
        gameRow.append(startColumn);
        gameRow.append(playColumn);

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
    var ourPlayer = payload.identity;
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

    if (payload.status == "ACTIVE") {
        if (payload.placement != null) {
            drawShip(payload);
        } else {
            $("#place_ship_action").show();
            $("[id='" + ourPlayer + "']").find(".grid_cell").click(function() {
                var row = $(this).attr("data-row");
                var column = $(this).attr("data-column");
                selectCellForShip(row, column, ourPlayer);
            })
        }
    } else if(payload.status == "SHIPS_PLACED") {
        drawShip(payload);
        drawShots(payload);
        //TODO: check if it's our turn and show actions for selecting an attack
    } else if(payload.status == "DONE") {
        alert("Game done!")
        //TODO: 1. show details (e.g. who won etc.) 2. draw ship locations of other players (need to think about coloring differently, depending on whether they were hit or not.)
    }
}

/******
    objects in the form:
    {
        row: 1,
        column: 4
    }

*******/
var cellsSelectedForShip = []
var shipSize = 3;

function selectCellForShip(cellRow, cellColumn, playerName) {
    var alignmentErrorMessage = "Ships can only be placed horizontally or vertically!";

    if (cellsSelectedForShip.length >= shipSize) {
        alert("You cannot select more than 3 positions for a ship!");
    } else if (cellsSelectedForShip.length == 0) {
        addShipLocation(cellRow, cellColumn, playerName);
    } else if (cellsSelectedForShip.length == 1) {
        var alreadySelectedCell = cellsSelectedForShip[0];
        if (alreadySelectedCell.row != cellRow && alreadySelectedCell != cellColumn) {
            alert(alignmentErrorMessage);
        } else if (alreadySelectedCell.row == cellRow) {
            // ship placed horizontally
            if (alreadySelectedCell.column != cellColumn + 1 && alreadySelectedCell.column != cellColumn - 1  ) {
                alert(alignmentErrorMessage);
            } else {
                addShipLocation(cellRow, cellColumn, playerName);
            }
        } else {
            // ship placed vertically
            if (alreadySelectedCell.row != cellRow - 1 && alreadySelectedCell.row != cellRow + 1) {
                alert(alignmentErrorMessage);
            } else {
                addShipLocation(cellRow, cellColumn, playerName);
            }
        }
    } else {
        var cell1 = cellsSelectedForShip[0];
        var cell2 = cellsSelectedForShip[1];
        if (cell1.row == cell2.row) {
            // ship placed horizontally
            var minColumn = Math.min(cell1.column, cell2.column);
            var maxColumn = Math.max(cell1.column, cell2.column);
            if (cell1.row != cellRow || (minColumn != cellColumn + 1 && maxColumn != cellColumn - 1) ) {
                alert(alignmentErrorMessage);
            } else {
                addShipLocation(cellRow, cellColumn, playerName);
            }
        } else {
            // ship placed vertically
            var minRow = Math.min(cell1.row, cell2.row);
            var maxRow = Math.max(cell1.row, cell2.row);
            if (cell1.column != cellColumn || (minRow != cellRow + 1 && maxRow != cellRow - 1) ) {
                alert(alignmentErrorMessage);
            } else {
                addShipLocation(cellRow, cellColumn, playerName);
            }
        }
    }
}

function addShipLocation(row, column, playerName) {
    var shipColor = "#9fa9a3"; // overriding color via JS
    cellsSelectedForShip.push({ row: row, column: column});
    $("[id='" + playerName + "']").find("[data-row='" + row + "'][data-column='" + column + "']").css("background-color", shipColor);
}

function finaliseShipLocation() {
    if (cellsSelectedForShip.length < shipSize) {
        alert("The ship size is " + shipSize + ", you need to select more cells.");
    } else {
        alert("Ship location selected.");
        //TODO: add call to back-end to notify about selected ship location.
    }
}

function drawShip(payload) {
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

function drawShots(gameState) {

}