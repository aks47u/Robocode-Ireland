Robocode-Ireland
================
NUI Maynooth entry to Robocode Ireland 2013, hosted by Games Fleadh at LIT Thurles

Robot name: Kromulak

Team members: Ollie Noonan, Conor Fitzpatrick, Alex Mitchell

!!2013 WINNERS!!
----------------
KROMULAK explained.

Our tank started off based on the user ship in space invaders, just sweeping over and back across the bottom of the field, adjustments were made to handle different situations after viewing the tank in multiple battles and seeing its weaknesses.

Basically its decisions are based on 2 modes, standard mode and close combat mode. The tank loops within a mode until the mode changes/death/victory.

- Standard mode:
    + Move to nearest base.
    + Strafe base with gun facing out at 90 degrees from self.
    + At end of each strafe peek into the far corner.
    + Every few strafes do 360 rotate of gun
    + If collide with another tank switch to close combat mode
    + If it sees a tank it considers worth trying to shoot, shoot it
    + If miss too many times swap base
    + If getting shot too often move to opposite base

- Close combat mode:
    + If other guy is weaker turn gun towards it and keep shooting with full power
    + If our health gets too low then run away to other base
    + If we haven’t scanned a robot in a few turns then just switch back to standard mode


We consider a base to be:
- Far enough from either top or bottom wall so that if bullet it shot parallel to back wall from corner it misses our tank but not so far as another tank can hide behind us.
- Far enough away from side walls that we keep out of the line of fire from any wall tank shooting parallel to side wall but close enough to pin a tank if it tries driving past us.

Shooting depending on its own energy, the energy of the tank it wants to shoot, the distance away the other tank is and also how successful its previous shots have been. 

Where possible we used class variables to make it easier to make adjustments to things like how far to keep from walls, how many strafes before doing a 360 scan, how many misses before swapping bases etc.

================
####NOTE:
The GamesFleadh Robocode competition changed its rules in 2014.
This robot does not represent a suitable strategy for future entries due to the new sentry bots and their inability to be used as 'batteries' etc.

As per gamesfleadh.ie/compete/robocode/

>"...using the Sentry robot as an energy and points bank so as to achieve a higher score.
Robocode for 2014 has been redesigned so that a robot receives no energy or points for hitting the SentryBot.
We have implemented a new BorderSentry class to achieve this which uses the idea of a BorderArea (coloured red) which allows the SentryBot to cause damage to robots. If the robots are in the “safe zone” (coloured blue) the SentryBot bullets do not cause any damage. We recommend a safe zone of 100×100 in the middle of the screen which implies a border area of 300. The safe zone concept only applies to bullets from the SentryBot.
These changes are implemented in Robocode 1.9 Beta"

Was this in response to our little robot doing so well (5-1 in semifinal, 7-1 in the final) ?

~~Yes~~

I really hope so :)
