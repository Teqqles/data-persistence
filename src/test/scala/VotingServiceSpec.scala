import akka.actor.{ActorRef, Props}
import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import pt.akka.workshop.VotingsManager._
import pt.akka.workshop.{ResourceService, VotingsManager}

class VotingServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with ResourceService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging
  override implicit val votingsManager: ActorRef = system.actorOf(Props(classOf[VotingsManager]))

  it should "create a new voting" in {
    Post(s"/votings", CreateVoting("", "", 1)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VotingCreated] shouldBe VotingCreated("")
    }
  }

  it should "get a voting result" in {
    Get(s"/votings/a") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VotingResult] shouldBe VotingResult(None, 10, true)
    }
  }

  it should "post a vote" in {
    Post(s"/votings/a", Vote("")) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VoteDone] shouldBe VoteDone("a", "")
    }
  }

}
