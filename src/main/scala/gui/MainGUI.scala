package gui

import network._
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafxml.core.{FXMLView, NoDependencyResolver}

object MainGUI extends JFXApp{
    val resource = getClass.getResource("MainGUI.fxml")
    val root = FXMLView(resource, NoDependencyResolver)

  stage = new JFXApp.PrimaryStage() {
    title = "Lets Play Oselo"
    scene = new Scene(root)
  }

  def setEvents(server:  Server, client: Client): Unit ={
    stage.setOnCloseRequest((event) => server.stopThread)
    stage.setOnCloseRequest((event) => client.stopThread)
  }
}
