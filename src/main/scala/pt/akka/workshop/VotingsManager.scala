package pt.akka.workshop

import akka.actor.Actor


/*


 */

object VotingsManager {
  case class CreateVoting(thingAId:String, thingBId:String, maxVotes:Int)
  case class VotingCreated(votingId:String)

  case class Vote(votingId:String, userId:String)
  case class VoteDone(votingId:String, userId:String)

  case class GetResult(votingId:String)
  case class VotingResult(winningThingId:Option[String], votes:Int, finished:Boolean)
}

class VotingsManager extends Actor {
  import VotingsManager._

  override def receive: Receive = {
    case CreateVoting(thing1Id, thing2Id, maxVotes) =>
      sender() ! VotingCreated("not implemented")

    case Vote(votingId, userId) =>
      sender() ! VoteDone(votingId, userId)

    case GetResult(votingId) =>
      sender() ! VotingResult(Some("not implemented"), 10, true)
  }
}
