Exercise 2, assessed
--------------------

* Summary:

  You will implement a client-server system that allows users in
  different machines to play tic-tac-toe against each other.

  The system should prevent races and deadlocks.

* Given    Week 3 Tuesday  2016/01/26 morning
  Deadline Week 4 Thursday 2016/02/04 night
  Duration 10 days

* The exercise is specified in two parts.

    (1) One in this document.
    (2) A second one to be released on Friday, Week 3.

* Your code may be based on the lecture files

     communication-and-concurrency/4-sockets/3-counter/*

  and Jon Rowe's tic-tac-toe lecture code (first term, Week 10).

* Your first task, unassessed, is to make sure you can run the above
  code in the lab. Instructions are given in the lecture log for
  Week 3, second term, under modules on Canvas.

  Then you should make sure you understand how this works.

* Your server should listen to a port waiting for a client to connect,
  and start one thread for each client.

  It should be run as

  $ java Server <port number>

* Your client should be run as

  $ java Client <user nickname> <port number> <machine name>

  e.g.

  $ java Client John 4444 ug04-0043

  where the machine name is that where your server is running.

* A client user may

  (1) Ask for the nicknames of the users connected to the server.

  (2) Request to play against a user identified by a nickname.

      (2.1) If the user is available (not playing with somebody else),
      then a game is initiated (let's say the initiator starts).

      (2.2) The client should then accept moves from the user,
      communicate them to the opponent via the server, get moves from
      the opponent via the server, and display the updated board,
      until the game ends. After this we go to the initial state,
      ready for one of (1)-(4).

  (3) Accept or reject a request for playing against another client,
      sent via the server.

  (4) Request the table of scores for the players, kept by the server.

  (5) Quit the client.

* The server should allow the above behaviour by the clients, in
  particular facilitating the communication between them, and keeping
  a table of scores by suitable communication with the clients.

* You may of course need to use additional classes to implement the
  above.

* To get any mark, your submission should compile.

  You will get marks for correctness, style, comments, javadoc etc. as
  usual.

* I use an automatic plagiarism detection tool in programming modules
  (this and Functional Programming):

     https://theory.stanford.edu/~aiken/moss/ 

  Here is the science behind it:

     http://theory.stanford.edu/~aiken/publications/papers/sigmod03.pdf

  Lecture-provided code doesn't count as plagiarism, of course, which
  is supplied to the tool as such. The tool doesn't decide anything by
  itself: it just shows me the pairs of submissions which are
  suspiciously similar, for me, or the Senior Tutor, or the College or
  the University to decide.

  We monitor plagiarism not only because we are required to, but also
  to be fair with the vast majority of you who work hard to produce
  your own submissions.
