package gui

import javafx.scene.shape.Circle
import scalafx.scene.control.Label
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color

class AssembleGUI(val board: GridPane, val statusLabel: Label, val victoryLabel: Label, val countLabel: Label) {

  def createCircle(posX: Int, posY: Int, color: Color): Unit ={
    val circle :Circle = new Circle(1,0,26, color)
    circle.setTranslateX(1)
    board.add(circle, posX , posY)
  }

  def createCandidateCircle(posX: Int, posY: Int): Unit ={
    val circle: Circle = new Circle(1,0,6,Color.web("0x1b95ff66"))
    circle.setTranslateX(10)
    board.add(circle, posX, posY)
  }

  def changeStatusLabel(text: String): Unit ={
    statusLabel.text = text
  }

  def changeVictoryLabel(text: String): Unit ={
    victoryLabel.text = text
  }

  def changeCountLabel(text: String): Unit ={
    countLabel.text = text
  }
}
