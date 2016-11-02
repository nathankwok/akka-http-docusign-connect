import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val personXml =
    <person>
      <name>John Doe</name>
      <age>25</age>
    </person>

  "Service" should "respond to single IP query" in {
    Get("/") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpInfo] shouldBe ip1Info
    }

    "personXml" should "be unmarshalled" in {
      Unmarshal(personXml).to[Person].futureValue shouldBe Person("John Doe", 25)
    }
  }

  it should "respond to IP pair query" in {
    Post(s"/ip", IpPairSummaryRequest(ip1Info.query, ip2Info.query)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpPairSummary] shouldBe ipPairSummary
    }
  }

  it should "respond with bad request on incorrect IP format" in {
    Get("/ip/asdfg") ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest(ip1Info.query, "asdfg")) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest("asdfg", ip1Info.query)) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }
  }
}
