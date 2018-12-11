package network

import java.io._
import java.net._

import gui.AssembleGUI
import game.GameObject
import scalafx.application.Platform
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color._

class Server(val port: Int, val board: GridPane, val gameObj: GameObject, val gui: AssembleGUI) extends Thread{
  var ss: ServerSocket = _
  var sc: Socket = _
  var br: BufferedReader = _
  var pw: PrintWriter = _
  var clientMsg: String = _
  var tmpArr: Array[String] = _

  var isActive: Boolean = true

  def createServer: Unit ={
    try{
      Platform.runLater(() -> gui.changeStatusLabel("Status: Waiting・・・"))
      ss = new ServerSocket(port)
      println("Waiting For・・・")
      sc = ss.accept()
      Platform.runLater(() -> gui.changeStatusLabel("Status: Connected!"))
      Platform.runLater(() -> gui.changeVictoryLabel("あなたの番"))
      println("Welcome!!")
      //以下、メッセージやり取りのための変数初期化
      try {
        br = new BufferedReader(new InputStreamReader(sc.getInputStream))
        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream)))
      }catch {
        case e: Exception => e.printStackTrace
      }

    } catch {
      case e:Exception => e.printStackTrace
      ss.close()
    }
  }

  def sendMsg(msg: String): Unit = {
    gameObj.reversible = Set()
    println(gameObj.reversible)
    tmpArr = msg.split(",")
    pw.println(msg)
    pw.flush()
    gui.changeVictoryLabel("相手の番")
  }

  //別スレッドで行いたい処理はここに書く。
  override def run(): Unit ={
    createServer
    receiveMsg
  }

  def receiveMsg: Unit ={
    while (isActive) {
      clientMsg = br.readLine()
      if(clientMsg == "pass"){
        gameObj.flag = true
        Platform.runLater(() -> gui.changeVictoryLabel("あなたの番"))
      }else {
        tmpArr = clientMsg.split(",")
        gameObj.piecesList(tmpArr(0).toInt)(tmpArr(1).toInt) = Some("White")
        gameObj.flag = true
        Platform.runLater(() -> gui.createCircle(tmpArr(0).toInt, tmpArr(1).toInt, White))
        Platform.runLater(() -> gameObj.reverse(tmpArr(0).toInt, tmpArr(1).toInt))
        Platform.runLater(() -> gui.changeVictoryLabel("あなたの番"))
        Platform.runLater(() -> gameObj.judgeVictory(gui.victoryLabel))
        gameObj.playPutSound()
      }
    }
  }

  def _println(msg: String, al: (Int, Int)): Unit ={
    if(al != null){
      println(msg + al._1.toString + "," + al._2.toString)
    }
  }
  def stopThread(): Unit ={
    this.isActive = false
  }
}
