package gui

import java.io.File
import java.net.Socket
import javafx.geometry.HPos

import game.{GameObject, Sound}
import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color._
import scalafx.scene.layout.GridPane
import scalafxml.core.macros.sfxml
import network._
import scalafx.scene.control.{Button, Label, TextField}


@sfxml
class OseloController(val board: GridPane, val statusLabel:Label, val connectButton: Button,
                      val countLabel: Label, val serverButton: Button, val victoryLabel: Label,
                      val ipInput: TextField, val test: Button){

  val gui: AssembleGUI = new AssembleGUI(board, statusLabel, victoryLabel, countLabel)
  val gameObj: GameObject = new GameObject(gui)
  val server: Server = new Server(8000, board, gameObj, gui)
  var client: Client = _
  var isServer: Boolean = _ //自分がサーバーかどうかの判断

  var posX:Int = _
  var posY:Int = _

  initMap()
  //MainGUI.setEvents(server, client)

  serverButton.onMouseClicked = (_: MouseEvent) =>{
    server.start()
    isServer = true
    gameObj.searchReversible("Black")
    serverButton.setDisable(true)
    connectButton.setDisable(true)
    ipInput.setDisable(true)
  }

  connectButton.onMouseClicked = (_: MouseEvent) =>{
    if(ipInput.text.toString() != "") {
      val sc: Socket = new Socket(ipInput.getText(), 8000)
      client = new Client(sc, board, gameObj, gui)
      if(client.initClient) {
        client.start()
        gui.changeStatusLabel("Status: Connected!")
        gui.changeVictoryLabel("相手の番")
        isServer = false
        serverButton.setDisable(true)
        connectButton.setDisable(true)
        ipInput.setDisable(true)
      }else{
        println("通信失敗")
      }
    }
  }

  board.onMouseClicked = (e:MouseEvent) => {
    posX = (e.x / 54).asInstanceOf[Int]
    posY = (e.y / 54).asInstanceOf[Int]
    if (gameObj.flag) {
      if(isServer) gameObj.searchReversible("Black") else gameObj.searchReversible("White")
      if(gameObj.reversible.isEmpty){ //検索した結果空ならばパスとなる
        gameObj.flag = !gameObj.flag //相手に番を渡す。
        gui.changeVictoryLabel("相手の番")
        if(isServer) server.sendMsg("pass") else client.sendMsg("pass")
      }

      if(gameObj.checkPut(posX, posY)) {
        gameObj.playPutSound
        if (isServer) {
          gameObj.piecesList(posX)(posY) = Some("Black")
          gameObj.reverse(posX, posY)
          server.sendMsg(posX.toString + "," + posY.toString)
          gui.createCircle(posX, posY, Black)
          //gui.createCandidateCircle(posX+1, posY+1)
          gameObj.flag = false
        } else {
          gameObj.piecesList(posX)(posY) = Some("White")
          gameObj.reverse(posX, posY)
          client.sendMsg(posX.toString + "," + posY.toString)
          gui.createCircle(posX, posY, White)
          gameObj.flag = false
        }
        gameObj.judgeVictory(victoryLabel)
      }
    }
  }

  def _println(msg: String): Unit ={
    for(x <- 0 to 7){
      for(y <- 0 to 7){
        println(msg + x.toString + "," + y.toString + "::" + gameObj.piecesList(x)(y))
      }
    }
  }

  def initMap(): Unit ={
    //データの初期化
    gameObj.initPieceList
    gameObj.piecesList(3)(3) = Some("Black")
    gameObj.piecesList(4)(4) = Some("Black")
    gameObj.piecesList(3)(4) = Some("White")
    gameObj.piecesList(4)(3) = Some("White")

    //GUIの初期化
    gui.createCircle(3,3,Black)
    gui.createCircle(4,4, Black)
    gui.createCircle(3,4, White)
    gui.createCircle(4,3, White)
  }
}
