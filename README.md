This project tests Pubnub's ability to do a synchronous canvas on Android. At this stage the project only allows for two users to join the same channel and draw on the same canvas.

Android screens have different size canvasses, and this is not scaled between devices at this stage. The upper left corner of your device should definitely be part of the shared canvas between two devices.

To build this app for yourself, get a publish and subscribe key from Pubnub and fill it in in the PubnubClient.java file where it is indicated.

At this stage there is somewhat of a delay in sending over the coordinates - it's not entirely realtime. The idea is that a line is being sent over as it is being drawn (as is the current implementation), not that each line is only sent over as soon as the entire line is drawn.

