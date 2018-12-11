package network

import java.io._
import java.net._

import game.GameObject
import gui.AssembleGUI
import scalafx.scene.paint.Color._
import scalafx.scene.layout.GridPane
import scalafx.application.Platform

class Client(val sc: Socket, val board: GridPane, val gameObj: GameObject, val gui: AssembleGUI) extends Thread{
  var br: BufferedReader = _
  var pw: PrintWriter = _
  var serverMsg: String = _
  var tmpArr: Array[String] = _

  var isActive: Boolean = true

  def initClient(): Boolean ={
    try {
      println("You are conected!")
      br = new BufferedReader(new InputStreamReader(sc.getInputStream))
      pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream)))
      return true
    }catch {
      case e: Exception => {
        gui.changeStatusLabel("相手が見つかりませんでした。")
        e.printStackTrace
        return false
      }
    }
  }

  def sendMsg(msg: String): Unit = {
    gameObj.reversible = Set()
    tmpArr = msg.split(",")
    pw.println(msg)
    pw.flush()
    gui.changeVictoryLabel("相手の番")
  }

  //別スレッドで行いたい処理はここに書く。
  override def run(): Unit ={
   receiveMsg
  }

  def receiveMsg: Unit ={
    while(isActive) {
      serverMsg = br.readLine()
      if(serverMsg == "pass"){
        gameObj.flag = true
        Platform.runLater(() -> gui.changeVictoryLabel("あなたの番"))
      }else {
        tmpArr = serverMsg.split(",")
        gameObj.piecesList(tmpArr(0).toInt)(tmpArr(1).toInt) = Some("Black")
        gameObj.flag = true
        Platform.runLater(() -> gui.createCircle(tmpArr(0).toInt, tmpArr(1).toInt, Black))
        Platform.runLater(() -> gameObj.reverse(tmpArr(0).toInt, tmpArr(1).toInt))
        Platform.runLater(() -> gui.changeVictoryLabel("あなたの番"))
        Platform.runLater(() -> gameObj.judgeVictory(gui.victoryLabel))
        gameObj.playPutSound()
      }
    }
  }

  def stopThread(): Unit ={
    this.isActive = false
  }

}

