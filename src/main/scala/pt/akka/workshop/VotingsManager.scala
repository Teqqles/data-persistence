package pt.akka.workshop

import akka.actor.Actor


/*

                       ----------------------------
                       |   Voting                 |
  0   User1            |                          |
 /|\  --------         |   --------  --------     |
 / \  | Vote |         |   |Item A|  |Item B|     |
      --------         |   --------  --------     |
         Item A -->    |     V: 4      V: 3       |
                       |                          |
                       ----------------------------
   Phase 1:
   - creating votings, sending votes and getting results
   - basic error handling (voting or item does not exist, vote for a finished voting, duplicate item in a voting)

   Phase 2:
   - it should be illegal to create two votings with two the same items
   - it should be illegal for a user to vote more than once in a single voting

   Phase 3:
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
   * @param votes number of votes of the winning item or votes in a draw
   * @param finished is the voting finished? (was max number of votes reached)
   */
  case class VotingResult(winningItemId:Option[String], votes:Int, finished:Boolean)
}

class VotingsManager extends Actor {
  import VotingsManager._

  override def receive: Receive = {
    case CreateVoting(itemAId, itemBId, maxVotes) =>
      sender() ! VotingCreated("not implemented")

    case Vote(votingId, itemId, userId) =>
      sender() ! VoteDone(0) // not implemented

    case GetResult(votingId) =>
      sender() ! VotingResult(Some("not implemented"), 10, true)
  }
}
