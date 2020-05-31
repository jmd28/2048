# 2048
implementation of 2048 game in kotlin

**build using:**
`kotlinc .\game.kt -include-runtime -d 2048.jar`

**run using:**
`java -jar 2048.jar`

**goal:**
reach 2048

**to play:**
use w, a, s or d followed by ENTER to make a move.
upon a move, cells are pushed as far as the will go in the given direction.
if two cells of the same value are pushed together, they will combine to become one cell holding the sum of their values.
