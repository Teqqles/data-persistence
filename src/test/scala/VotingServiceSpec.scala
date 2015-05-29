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
    Post(s"/votings", CreateVoting("thingAId", "thingBId", 2)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VotingCreated] shouldBe a [VotingCreated]
      responseAs[VotingCreated] shouldBe VotingCreated("thingAId|thingBId")
    }
  }

  it should "get an empty voting result when no results have been counted" in {
    Get(s"/votings/thingAId|thingBId") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VotingResult] shouldBe VotingResult(None, 0, false)
    }
  }

  it should "throw an error if a voting does not exist when requested" in {
    Get(s"/votings/notAThing") ~> routes ~> check {
      status shouldBe BadRequest
    }
  }

  it should "post a vote" in {
    Post(s"/votings/thingAId|thingBId", Vote("thingAId", "davidLong")) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VoteDone] shouldBe VoteDone(1)
    }
    Post(s"/votings/thingAId|thingBId", Vote("thingAId", "avaLong")) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[VoteDone] shouldBe VoteDone(2)
    }
  }

}
