# data-persistence

So, our task is to implement a service actor for a votings management API.
    
- A voting is two items competing against each other for the votes of users.
- The voting ends when one of the items reaches max number of votes that was specified on voting's creation.
- It should be possible to retrieve results for all present and past votings.

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

##  Path 1 (mandatory):
- creating votings, gathering votes and returning results
- basic error handling (voting or item does not exist, vote for a finished voting, duplicate item in a voting)
- all information that is needed to conform to the API must be preserved between application restarts. (hence akka-persistence)

###  Path 2:
- it is illegal to create two votings with two the same items
- it is illegal for a user to vote more than once in a single voting
- state snapshots are used to allow for faster recovery

###  Path 3 (harder):
- a child actor is spawned to manage the state of each voting that is in progress - with its persistence.
- to handle increased load, the VotingsManager actor needs to be partitioned
