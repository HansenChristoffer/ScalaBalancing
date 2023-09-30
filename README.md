# ScalaBalancing

Wanted to do something in Scala, so I created a very basic "loadbalancer".

# How to test it out

Clone the repository.

Either comment out the code that uses and initializes the LogHandler or setup MongoDB locally with this data:
<url>mongodb://localhost:</url>
<port>27017</port>
<database>loadbalancer</database>
<collection>session_log</collection>
<user>lb_writer</user>
<secret>AdminRoot1337</secret>
Or change it in the repo. The file is called "mongo.xml" and is located in the resources folder.

Open a terminal and run the following command (You need "socat", you can use other solutions as well if you want to):
socat -d -d TCP4-LISTEN:8001,reuseaddr,fork EXEC:"/bin/cat"
This will just start up a "host" or "destination" server. Basically, it would represent some service in the backend,
that there exists more than one of.
You can start another server if you'd like, just make sure to change the PORT and add it to the hostconfiguration.xml
file in resources folder.

Run the "main" class.
Make sure no issue (exception) occur when you start ScalaBalancing.

Open another terminal and type the following command:
echo "I like big butts and I cannot lie!" | socat -d -d - TCP4:localhost:9999

If you changed the PORT for the TransceiverServer (transceiverconfiguration.xml) then you need to change the "9999" part
of the previous socat command!

# Heads up

I have no intentions of continuing more on this. This was just to play about with Scala a little and nothing else.
