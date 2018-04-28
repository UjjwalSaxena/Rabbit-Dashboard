package com.rabbit.av.camera

import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

import com.rabbit.av.manager.dashboardManager
import com.rabbit.av.ui.UI

import akka.actor.Actor
import akka.stream.ActorMaterializer
import javax.imageio.ImageIO

class NewServer(top: UI) extends Actor {
  implicit val materializer = ActorMaterializer()(context.system)
  var socketServer: ServerSocket = null
  var server: Socket = null

  println("Creating newServer")
	val host: String = "127.0.0.1"
	val port: Int = 8080
	private val MAX_IMG = 1024 * 1024 * 50
	
	def receive = {
    case "INIT" => run()
  }
  def run() {
    socketServer = new ServerSocket(port)
    while(true) { 
      try {
        server = socketServer.accept()
        if (server.isConnected()){
          val inStream = new BufferedInputStream(server.getInputStream)
          while(true){
            inStream.mark(MAX_IMG)
            val imgStream = ImageIO.createImageInputStream(inStream)
            val imgIterator = ImageIO.getImageReaders(imgStream)
            if (!imgIterator.hasNext()) {
            } else {
              val reader = imgIterator.next()
              reader.setInput(imgStream)
              val img: BufferedImage = reader.read(0)
              dashboardManager.setCurrentNode(img)
              top.displayVideoFrame(img)
              val bytesRead = imgStream.getStreamPosition()
              inStream.reset()
              inStream.skip(bytesRead)
            }
          }
        }
      } catch {
        case se: SocketTimeoutException =>
          println("Socket timed out!")
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }
}