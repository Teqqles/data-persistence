package pt.akka.workshop

import akka.actor.ActorRef
import akka.actor.{Props, ActorSystem}
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import pt.akka.workshop.VotingsManager._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol


trait JsonFormats extends DefaultJsonProtocol {

  implicit val createVotingFormat = jsonFormat3(CreateVoting.apply)
  implicit val votingCreatedFormat = jsonFormat1(VotingCreated.apply)
  implicit val votingResultFormat = jsonFormat3(VotingResult.apply)

  implicit val votingVoteFormat = jsonFormat2(VotingVote.apply)

  implicit val voteFormat = jsonFormat1(Vote.apply)
  implicit val voteDoneFormat = jsonFormat2(VoteDone.apply)

}

trait ResourceService extends JsonFormats {
  import scala.concurrent.duration._

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: FlowMaterializer
  implicit val votingsManager: ActorRef

  def config: Config

  val logger: LoggingAdapter

  import akka.pattern._
  implicit val timeout = Timeout(5.seconds)
  val routes = {
    logRequestResult("akka-workshop-persistence") {
      pathPrefix("votings") {
        pathEnd {
            (post & entity(as[CreateVoting])) { createVoting =>
              complete {
                (votingsManager ? createVoting).mapTo[VotingCreated].map(Right[String, VotingCreated](_))
                  .recover { case ex => Left(ex.getMessage) }
                  .map[ToResponseMarshallable] {
                  case Right(votingCreated) => votingCreated
                  case Left(errorMessage) => BadRequest -> errorMessage
                }
              }
            }
        } ~
          path(Segment) { votingId =>
            get {
              complete {
              (votingsManager ? GetResult(votingId)).mapTo[VotingResult].map(Right[String, VotingResult](_))
                .recover { case ex => Left(ex.getMessage) }
                .map[ToResponseMarshallable] {
                case Right(votingResult) => votingResult
                case Left(errorMessage) => BadRequest -> errorMessage
              }
            } } ~
              (post & entity(as[Vote])) { vote =>
                complete {
                  (votingsManager ? VotingVote(votingId, vote.userId )).mapTo[VoteDone].map(Right[String, VoteDone](_))
                    .recover { case ex => Left(ex.getMessage) }
                    .map[ToResponseMarshallable] {
                    case Right(votingCreated) => votingCreated
                    case Left(errorMessage) => BadRequest -> errorMessage
                  }
                }
              }
//              pathPrefix("votes") {
//
//              }
          }
      }
    }
  }

}

object VotingsApp extends App with ResourceService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()
  override implicit val votingsManager = system.actorOf(Props(classOf[VotingsManager]), "votings-manager")

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}
