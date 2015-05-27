package pt.akka.workshop

import akka.actor.{ActorLogging, Actor}
import akka.persistence.{RecoveryCompleted, SnapshotOffer, PersistentActor}


/*

  So, our task is to implement a service actor for a votings management API.
  A voting is two items competing against each other for the votes of users.
  The voting ends when one of the items reaches max number of votes that was specified on voting's creation.
  It should be possible to retrieve results for all present and past votings.

  The API is:
  POST /votings - where body is a json with fields: "itemAId":string, "itemBId":string, "maxVotes":int
  POST /votings/<votingid> - where body is a json with fields: "votingId":string, "itemId":string, "userId":string
  GEt  /votings returns json with "winningItemId":string (optional), "votes":int, "finished":boolean

                       ----------------------------
                       |   Voting                 |
  0   User1            |                          |
 /|\  --------         |   --------  --------     |
 / \  | Vote |         |   |Item A|  |Item B|     |
      --------         |   --------  --------     |
         Item A -->    |     V: 4      V: 3       |
                       |                          |
                       ----------------------------
Goals:

   Path 1 (mandatory):
   - creating votings, gathering votes and returning results
   - basic error handling (voting or item does not exist, vote for a finished voting, duplicate item in a voting)
   - all information that is needed to conform to the API must be preserved between application restarts.
     (hence akka-persistence)

   Path 2:
   - it is illegal to create two votings with two the same items
   - it is illegal for a user to vote more than once in a single voting
   - state snapshots are used to allow for faster recovery

   Path 3 (harder):
   - a child actor is spawned to manage the state of each voting that is in progress - with its persistence.
   - to handle increased load, the VotingsManager actor needs to be partitioned
   - use persistAsync instead of persist and deal with the consequences;)

 */

object VotingsManager {
  case class CreateVoting(itemAId:String, itemBId:String, maxVotes:Int)
  case class VotingCreated(votingId:String)

  case class Vote(votingId:String, itemId:String, userId:String)

  /**
   * Confirmation of the vote
   * @param votes number of accumulated votes for the item
   */
  case class VoteDone(votes:Int)

  case class GetResult(votingId:String)

  /**
   *
   * @param winningItemId id of the winning item or None, if it is a draw
   *                      (a draw is only possible if max number of votes is not reached yet).
   * @param votes number of votes of the winning item or votes in a draw
   * @param finished is the voting finished? (was max number of votes reached)
   */
  case class VotingResult(winningItemId:Option[String], votes:Int, finished:Boolean)
}

class VotingsManager extends PersistentActor with ActorLogging {
  import VotingsManager._

  override def receiveCommand: Receive = {
    case CreateVoting(itemAId, itemBId, maxVotes) =>
      val replyTo = sender()
      val created = VotingCreated("not implemented")
      persist(created) { created =>
        // not implemented
        replyTo ! created
        log.debug("Created a new voting with id: " + 0)
      }
    case Vote(votingId, itemId, userId) =>
      sender() ! VoteDone(0) // not implemented

    case GetResult(votingId) =>
      sender() ! VotingResult(Some("not implemented"), 10, true)
  }

  def receiveRecover = {
    case VotingCreated(votingId) =>
      log.debug(s"recovering VotingCreated: " + votingId)
    case SnapshotOffer(_, snapshot: Any) =>
      log.debug(s"Integrating snapshot: " + snapshot)
    case RecoveryCompleted =>
      log.info(s"Recovery of VotingsManager completed.")
    case e =>
      log.error(s"Received unknown event: "+e)
  }

  def persistenceId: String = "VotingsManager"
}
