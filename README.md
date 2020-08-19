<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Battleship in Corda

## Instructions to setup the environment

* Deploy nodes
```
./gradlew deployNodes
```
* Start the nodes by moving to the subdirectory of every node (e.g. `build/nodes/Captain1`) for all the 4 nodes and start them with the following command:
```
java -jar corda.jar
```
* Start all the 4 web servers using the following commands:
```
./gradlew runServerA
./gradlew runServerB
./gradlew runServerC
./gradlew runServerD
```

## Instructions to play the game

The home page contains all the currently existing games:
* Create a new game by using the "Create a new game" button.
* Join an existing game by using the "Join Game" button.
* Start/activate a game by using the "Start Game" button.
* Play an already started/activated game by using the "Play Game" button.

After entering the game page:
* First, place your ship by clicking on 3 contiguous cells and then click the button "Place ship".
* You then have to wait until everyone has placed their ships. After everyone has placed the ships, the UI will contain actions you can use to perform attacks.
* You can perform an attack by selecting the cell you want to attack and then clicking the "Attack" button.
* If your ship is sunk, you are not able to perform attacks anymore. The last player that remains with a ship not sunk is the winner.