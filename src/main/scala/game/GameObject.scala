package game

import gui.AssembleGUI
import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._

import scala.util.control.Breaks

//ゲームデータを管理するクラス
class GameObject(gui: AssembleGUI) {
  var piecesList: Array[Array[Option[String]]] = Array.ofDim[Option[String]](8,8)
  var reversible: Set[String] = Set() //置ける位置
  var flag: Boolean = true //順番制御

  //サウンド系クラスの初期化
  var sound: Sound = new Sound

  def initPieceList(): Unit ={
    for(x <- Array.range(0, 8)) piecesList(x) = Array.fill(8)(None)
  }

  def judgeVictory(victoryLabel: Label): Unit ={
    var blackCount = 0
    var whiteCount = 0
    for(x <- Array.range(0, 8)) {
      for (y <- Array.range(0, 8)) {
        if (piecesList(x)(y).getOrElse("") == "Black") {
          blackCount += 1
        } else if (piecesList(x)(y).getOrElse("") == "White") {
          whiteCount += 1
        }
      }
    }
    gui.changeCountLabel("黒:" + blackCount.toString + " 白:" + whiteCount.toString)

      if((blackCount + whiteCount) == 64){
        if(blackCount > whiteCount){
          gui.changeVictoryLabel("黒の勝利！！")
        }else if(blackCount < whiteCount){
          gui.changeVictoryLabel("白の勝利！！")
        } else{
          gui.changeVictoryLabel("引き分け！！")
        }
      }
  }

  def checkPut(x: Int, y: Int): Boolean ={
    val pos = x.toString + "," + y.toString

    if(piecesList(x)(y) == None && reversible.contains(pos))true else false
  }

  def pivotReverse(pivot: String): String ={
    if(pivot == "Black"){
      return "White"
    }else{
      return "Black"
    }
  }
  /*
  配置可能領域を検索する。
  */
  private def searchCandidate(): Array[String] ={
    var currentList: Seq[String] = Seq() //今ある駒のリスト
    var candidate: Set[String] = Set() //配置可能な位置リスト

    var _x = 0
    var _y = 0

    var tmpArr: Array[String] = new Array[String](2)
    //今ある駒のリストを取得
    for(x <- Range(0, 7)){
      for(y <- Range(0,7)){
        if(piecesList(x)(y).isDefined){
          currentList :+= (x.toString + "," + y.toString)
        }
      }
    }
    println(currentList)
    // currentListを配置可能なSetに絞り込む
    for(i <- 0 to currentList.length - 1){
     tmpArr = currentList(i).split(",")
      _x = tmpArr(0).toInt
      _y = tmpArr(1).toInt

      //上方向の確認
      if((_y - 1) > -1 && piecesList(_x)(_y - 1).isEmpty) candidate = candidate + (_x.toString + "," + (_y - 1).toString)
      //右上方向の確認
      if((_y - 1) > -1 && (_x + 1) < 8 && piecesList(_x + 1)(_y - 1).isEmpty) candidate = candidate + ((_x + 1).toString + "," + (_y - 1).toString)
      //右方向の確認
      if((_x + 1) < 8 && piecesList(_x + 1)(_y).isEmpty) candidate = candidate + ((_x + 1).toString + "," + _y.toString)
      //右下方向の確認
      if((_x + 1) < 8 && (_y + 1 ) < 8 && piecesList(_x + 1)(_y + 1).isEmpty) candidate = candidate + ((_x + 1).toString + "," + (_y + 1).toString)
      //下方向の確認
      if((_y + 1) < 8 && piecesList(_x)(_y + 1).isEmpty) candidate = candidate + (_x.toString + "," + (_y + 1).toString)
      //左下方向の確認
      if((_x - 1) > -1 && (_y + 1) < 8 && piecesList(_x - 1)(_y + 1).isEmpty) candidate = candidate + ((_x - 1).toString + "," + (_y + 1).toString)
      //左方向の確認
      if((_x - 1) > -1 && piecesList(_x - 1)(_y).isEmpty) candidate = candidate + ((_x - 1).toString + "," + _y.toString)
      //左上方向の確認
      if((_x - 1) > -1 && (_y - 1) > -1 && piecesList(_x - 1)(_y - 1).isEmpty) candidate = candidate + ((_x - 1).toString + "," + (_y - 1).toString)
    }

    println(candidate)
    return candidate.toArray
  }

  /*
  反転可能領域の配列を返す。
  params:
    candidate:配置可能位置の入った配列　=> "x,y"
    pivot: 現在置こうとしている色
  return: 反転可能位置の入った配列
  */
  def searchReversible(pivot: String): Unit ={
    val candidate: Array[String] = searchCandidate() //駒の周囲のマス全ての洗い出し

    var count = 0
    val rPivot = pivotReverse(pivot)//現在見ている駒と逆色
    var x: Int = 0
    var y: Int = 0
    var _x = 0
    var _y = 0
    var tmpArr: Array[String] = new Array[String](2)

    val b = new Breaks

    for(i <- 0 to candidate.length - 1) {

      tmpArr = candidate(i).split(",")
      x = tmpArr(0).toInt
      y = tmpArr(1).toInt

      println(x + "," + y)

      count = 0
      _x = x
      _y = y - 1
      b.breakable {
        while (_y > -1) { //上方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("上方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y -= 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("上方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("上方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _y = y - 1
      _x = x + 1
      b.breakable {
        while (_y > -1 && _x < 8) { //右上方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("右上方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y -= 1
            _x += 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("右上方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("右上方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _x = x + 1
      _y = y
      b.breakable {
        while (_x < 8) {
          //右方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("右方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _x += 1
            println(count.toString)
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("右方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("右方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _y = y + 1
      _x = x + 1
      b.breakable {
        while (_y < 8 && _x < 8) { //右下方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("右下方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y += 1
            _x += 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("右下方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("右下方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _x = x
      _y = y + 1
      b.breakable {
        while (_y < 8) {
          //下方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("下方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y += 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("下方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("下方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _y = y + 1
      _x = x - 1
      b.breakable {
        while (_y < 8 && _x > -1) { //左下方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("左下方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y += 1
            _x -= 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("左下方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("左下方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _x = x - 1
      _y = y
      b.breakable {
        while (_x > -1) { //左方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("左方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _x -= 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("左方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("左方向:" + count)
            b.break
          }
        }
      }

      count = 0
      _y = y - 1
      _x = x - 1
      b.breakable {
        while (_y > -1 && _x > -1) { //左上方向の確認
          if (piecesList(_x)(_y).getOrElse("") == rPivot) {
            println("左上方向:" + _x.toString + "," + _y.toString + "の位置で発見")
            count += 1
            _y -= 1
            _x -= 1
          } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
            if(count > 0) reversible = reversible + (x.toString + "," + y.toString)
            println("左上方向:countが" + count.toString + "なので入りました！")
            println("pieceList:" + piecesList(_x)(_y).getOrElse("Else") + " pivot:" + pivot)
            b.break
          } else {
            //Nullの時
            println("左上方向:" + count)
            b.break
          }
        }
      }
      println("---------------------")
    }
    print("reversible:")
    println(reversible)
  }

  def reverse(x: Int, y: Int): Unit ={
    var count: Int = 0
    val pivot: String = piecesList(x)(y).get //今置かれたのが白か黒か
    var rPivot: String = "" //今置かれたのと逆の色
    var _x = x
    var _y = y

    val b = new Breaks
    var color: Color = Black

    rPivot = pivotReverse(pivot)
    if(rPivot == "Black"){
      color = White
    }else{
      color = Black
    }

    _y = y - 1
    b.breakable {
      while (_y > -1) { //上方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y -= 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _y += 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("上方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _y = y - 1
    _x = x + 1
    b.breakable {
      while (_y > -1 && _x < 8) { //右上方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y -= 1
          _x += 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x -= 1
            _y += 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("右上方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _x = x + 1
    _y = y
    b.breakable {
      while (_x < 8) {
        //右方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _x += 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x -= 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("右方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _y = y + 1
    _x = x + 1
    b.breakable {
      while (_y < 8 && _x < 8) { //右下方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y += 1
          _x += 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x -= 1
            _y -= 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("右下方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _x = x
    _y = y + 1
    b.breakable {
      while (_y < 8) {
        //下方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y += 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _y -= 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("下方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _y = y + 1
    _x = x - 1
    b.breakable {
      while (_y < 8 && _x > -1) { //左下方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y += 1
          _x -= 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x += 1
            _y -= 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("左下方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _x = x - 1
    _y = y
    b.breakable {
      while (_x > -1) { //左方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _x -= 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x += 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("左方向:" + count)
          b.break
        }
      }
    }

    count = 0
    _y = y - 1
    _x = x - 1
    b.breakable {
      while (_y > -1 && _x > -1) { //左上方向の確認
        if (piecesList(_x)(_y).getOrElse("") == rPivot) {
          count += 1
          _y -= 1
          _x -= 1
        } else if (piecesList(_x)(_y).getOrElse("") == pivot) {
          //TODO countの分だけひっくり返していく。
          while (count != 0) {
            _x += 1
            _y += 1
            piecesList(_x)(_y) = Some(pivot)
            gui.createCircle(_x, _y, color)
            count -= 1
          }
          b.break
        } else {
          //Nullの時
          //println("左上方向:" + count)
          b.break
        }
      }
    }
  }
  def playPutSound(): Unit ={
    sound.playPutSound()
  }
}