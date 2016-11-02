import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{RequestContext, RouteResult, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

case class DocuSignEnvelopeInformation(name: String, age: Int)


trait Service extends XmlMarshallers {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  val personXml =
    <person>
      <name>John Doe</name>
      <age>25</age>
    </person>


  def toStrict(timeout: FiniteDuration): Directive[Unit] = {
    def toStrict0(inner: Unit ⇒ Route): Route = {
      val result: RequestContext ⇒ Future[RouteResult] = c ⇒ {
        // call entity.toStrict (returns a future)
        c.request.entity.toStrict(timeout).flatMap { strict ⇒
          // modify the context with the strictified entity
          val c1 = c.withRequest(c.request.withEntity(strict))
          // call the inner route with the modified context
          inner()(c1)
        }
      }
      result
    }
    Directive[Unit](toStrict0)
  }


  val routes = {
    pathEndOrSingleSlash {
      logger.debug("GOT 1")
      complete("OK")
    } ~ pathPrefix("envelope") {
        post {
          logger.debug("i got post 1")
          entity(as[DocusignConnectResponse]) { response =>
            logger.debug(s"DocRoutes - POST /api/doc/envelope/status")
            logger.debug(response.toString)
            complete("OK")
          }
      }

    } ~ pathPrefix("api") {
      pathPrefix("doc") {
        pathPrefix("status") {
          pathEnd {
            post {
              // Marshall as DocusignConnectResponse
              entity(as[DocusignConnectResponse]) { response =>
                logger.debug(s"DocRoutes - POST /api/doc/envelope/status")
                logger.debug(response.toString)
                complete("OK")
              }


            }
          }
        }

      }
    }
  }
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
