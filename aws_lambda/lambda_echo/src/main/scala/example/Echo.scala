package example

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler


trait EchoBase {
  def echo(phrase: String, context: Context) = {
    println(phrase)
  }
}

class Echo extends EchoBase