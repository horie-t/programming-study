package example

import java.util.Base64

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

import scala.beans.BeanProperty

class Request {
  @BeanProperty
  var body: String = _
}

class Response(bodyJson: String) {
  @BeanProperty
  val statusCode = 200

  @BeanProperty
  val headers: Object = new Object

  @BeanProperty
  var body: String = bodyJson
}

trait EchoBase extends RequestHandler[Request, Response]{
  override def handleRequest(request: Request, context: Context) = {
    val decodedBody = new String(Base64.getDecoder.decode(request.body))
    new Response(decodedBody)
  }
}

class Echo extends EchoBase