package game

import java.io.File

import scalafx.scene.media.{Media, MediaPlayer}

class Sound() {
  val m = new Media(getClass().getResource("resources/komaPut.mp3").toString)
  val putMp = new MediaPlayer(m)

  def playPutSound (){
    putMp.stop
    putMp.play()
  }
}
