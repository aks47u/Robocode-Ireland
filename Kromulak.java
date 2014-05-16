package OCA;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;

/**
 * Kromulak - a robot by Ollie Noonan, Conor Fitzpatrick & Alex Mitchell
 */
public class Kromulak extends Robot {
	/*
	 * different modes to dictate how to react under certain conditions
	 * ====================================================================
	 */
	// when the robot gets setup before it begins killing all the others
	private final byte SETUP_MODE = 0;
	// if another robot is close to this robot
	private final byte CLOSE_COMBAT_MODE = 1;
	// general state for the robot during its over & back movement
	private final byte STANDARD_COMBAT_MODE = 2;
	// for when the robot does 360 sweep of the field
	private final byte SWEEP_MODE = 6;
	// the current mode of the robot
	private byte current_mode = 0;
	/* =================================================================== */

	/*
	 * we split the field into boundries so that the decisions of our robot
	 * change based on the range of another robot
	 * =====================================================================
	 */
	// how close the inner boundry is
	private final double INNER_PERIMETER = 100;
	// how close the outer boundry is
	private final double OUTER_PERIMETER = 300;
	// how far away from the walls we want our bases to be. 50 allosw our bot to
	// get close enough to collide with wall bots
	private final double WALL_PERIMETER = 50;
	/* ==================================================================== */

	/*
	 * Our robot has 2 main bases that is tries to keep at
	 * ======================================================================
	 */
	// the base near the top wall
	private final byte BASE_STATION_TOP = 1;
	// the base near the bottom wall
	private final byte BASE_STATION_BOTTOM = 0;
	// the current base our robot is at. initialised to -1 until it gets to a
	// base
	private byte current_base_station = -1;
	/* ==================================================================== */

	// how many bounces before our robot performs a 360 scan of the field
	private byte QUICK_SWEEP_INTERVAL = 3;
	// a counter for the amount of bounces since the last scan
	private byte sweepClock = 0;

	// a counter of the amount of times the robot has been hit by a bullet
	int bulletsTaken = 0;
	// used to note the time or turn number when our robot takes a bullet, used
	// with timeToRun to decide if the robot is getting shot to often
	long bulletClock = 0;
	// the min amount of turns we want to see in between each time we get hit by
	// a bullet
	int timeToRunCount = 7;

	// 1 is forward, -1 backward
	int direction = 1;

	// for when our poor robot is almost dead we want it to change behaviour
	// private boolean lastStand = false;
	// when our health gets this low its time to make a stand
	private double LAST_STAND_HEALTH = 20;

	private boolean lastChance = false;

	// the turn we last seen an enemy robot
	private long timeRobotLastSeen = -1;

	/**
	 * run: Kromulak's default behavior basically get to the nearest base and
	 * just strafe over and back without hitting a wall
	 */
	public void run() {
		// darn americans, we coloUr in our robot
		// setColors(Color bodyColor, Color gunColor, Color radarColor, Color
		// bulletColor, Color scanArcColor)
		setColors(Color.black, Color.black, Color.black, Color.white,
				Color.white);

		// go to the nearest base
		moveToNearestBase();
		current_mode = STANDARD_COMBAT_MODE;
		printMode();
		while (true) {

			switch (current_mode) {
			case STANDARD_COMBAT_MODE:
				if (!isPositionedCorrectly())
					moveToNearestBase();
				// make sure our gun is facing the right way
				positionGunForBase();
				standardTactics();
				sweepOrPeek();
				break;
			case CLOSE_COMBAT_MODE:
				// sweepOrPeek();
				// ahead(50);
				// back(50);
				scan();
				if (getTime() - 7 >= timeRobotLastSeen) {
					current_mode = STANDARD_COMBAT_MODE;
					printMode();
				}
				break;
			}
		}
	}

	private void printMode() {
		switch (current_mode) {
		case STANDARD_COMBAT_MODE:
			System.out.println("currentmode: STANDARD_COMBAT_MODE");
			break;
		case CLOSE_COMBAT_MODE:
			System.out.println("currentmode: CLOSE_COMBAT_MODE");
			break;
		}
	}

	private void standardTactics() {
		if (direction == 1) {
			// go forward as far as we are allowed
			ahead((getBattleFieldWidth() - getX()) - WALL_PERIMETER);
			// change the direction for next time
			direction = -1;
		} else {
			// go backward as far as we are allowed
			back((getX() - WALL_PERIMETER));
			// change the direction for next time
			direction = 1;
		}
	}

	private void sweepOrPeek() {
		sweepClock++;

		// check if its time to sweep, we use >= in case we didnt get to scan
		// when we were last meant to
		if (sweepClock >= QUICK_SWEEP_INTERVAL) { // possibly increase sweep
			// rate as player count
			// decreases
			// switch to sweep mode
			// current_mode = SWEEP_MODE;
			// do our 360 scan to see if theres anything worth shooting
			turnGunLeft(360);
			// reset our sweep clock
			sweepClock = 1;
			// switch back to standard mode
			// current_mode = STANDARD_COMBAT_MODE;
		} else { // if we arent doing the 360 scan we should peek towards the
			// far corners
			if (current_base_station == BASE_STATION_BOTTOM) {
				turnGunLeft(20 * direction);
				turnGunRight(20 * direction);
			} else {
				turnGunRight(20 * direction);
				turnGunLeft(20 * direction);
			}
		}
	}

	/**
	 * checks to see if the robot is positioned correctly at a base.<br>
	 * The robot is considered correctly positioned if it is WALL_PERIMETER
	 * distance away from the top our bottom wall
	 * 
	 * @see WALL_PERIMETER
	 * @return boolean, returns true if positioned correctly otherwise false.
	 */
	private boolean isPositionedCorrectly() {
		System.out.println("Currently Y= " + getY());
		return (((getY() + WALL_PERIMETER) == getBattleFieldHeight()) || (getY() == WALL_PERIMETER));
	}

	/**
	 * Tells the robot to switch to the opposite base.
	 */
	private void swapPositions() {
		if (current_base_station == BASE_STATION_BOTTOM)
			moveToBase(BASE_STATION_TOP);
		else
			moveToBase(BASE_STATION_BOTTOM);
	}

	/**
	 * Tells the robot to bove to whatever base is nearest<br>
	 */
	private void moveToNearestBase() {
		if (getY() < getBattleFieldHeight() / 2)
			moveToBase(BASE_STATION_BOTTOM);
		else
			moveToBase(BASE_STATION_TOP);
	}

	/**
	 * Tells the robot to move to the base represented by the byte passed to
	 * this method<br>
	 * If the robot is already stationed there this method does nothing.
	 * 
	 * @see BASE_STATION_TOP
	 * @see BASE_STATION_BOTTOM
	 * @param byte base_station, the byte representation of the base to move to.
	 */
	private void moveToBase(byte base_station) {
		// if already at the base the robot has been told to move to just return
		if (base_station == current_base_station)
			return;

		// If the robot has been told to move to particular base
		if (base_station == BASE_STATION_BOTTOM) {
			// turn right until we are facing straight down
			// /// turnRight(180-getHeading());
			turnShortestDirection(180 - getHeading());
			// move forward until we are WALL_PERIMETER away from the bottom
			// wall
			ahead(getY() - WALL_PERIMETER);
			// turn 90 degrees to get parallel to the wall
			turnLeft(90);
			// the robot should now be at the base and ready to go so update the
			// current base location
			current_base_station = BASE_STATION_BOTTOM;
		} else if (base_station == BASE_STATION_TOP) {
			// turn right until we are facing straight up
			// /// turnRight(360-getHeading());
			turnShortestDirection(360 - getHeading());
			// move forward until we are WALL_PERIMETER away from the top wall
			ahead((getBattleFieldHeight() - getY()) - WALL_PERIMETER);
			// turn 90 degrees to get parallel to the wall
			turnRight(90);
			// the robot should now be at the base and ready to go so update the
			// current base location
			current_base_station = BASE_STATION_TOP;
		}

		// turn the gun 90 degrees out toward the field
		positionGunForBase();
	}

	/**
	 * onScannedRobot: What to do when you see another robot<br>
	 * The action performed here depends on the current_mode variable.<br>
	 * Currently if the robot that was scanned is weak and helpless our robot
	 * takes a shot at it,<br>
	 * Otherwise only fire depending on the distance the other robot was away
	 * from our robot.
	 * 
	 * @param e
	 *            , A ScannedRobotEvent of the robot that was scanned.
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		timeRobotLastSeen = getTime();
		// if the robot is performing a 360 sweep only fire if it sees a
		// disabled/weak robot
		if ((current_mode == SWEEP_MODE) && (e.getEnergy() < 1)) {
			fire(1);
		} else
			fireBasedOnDistance(e.getDistance(), e.getVelocity());

		if (e.getDistance() <= INNER_PERIMETER)
			current_mode = CLOSE_COMBAT_MODE;
		else
			current_mode = STANDARD_COMBAT_MODE;

		printMode();
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet<br>
	 * The robot shouldnt mind taking a few bullets but it shouldnt be afraid to
	 * run away either.
	 * 
	 * @param e
	 *            , A HitByBulletEvent of the bullet that hit our robot.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// if the robot has been shot within timeToRunCount increment the
		// bulletCounter otherwise start monitoring again
		if ((bulletClock < (getTime() - timeToRunCount)) && (getTime() != 0))
			bulletsTaken++;
		else {
			bulletsTaken = 1;
			bulletClock = getTime();
		}

		// if we are getting hit too quick or our health has ran low, move to
		// the other base
		if ((bulletsTaken == timeToRunCount)
				|| (getEnergy() <= LAST_STAND_HEALTH))
			swapPositions();
	}

	/**
	 * fire a bullet but set the power based on how far away the enemy is and
	 * its velocity
	 * 
	 * @param double distance, the distance away to determine how to fire.<br>
	 * @vel double vel, the velocity of the enemy
	 */
	public void fireBasedOnDistance(double distance, double vel) {
		if (distance <= INNER_PERIMETER)
			fire(3);
		else if (distance <= OUTER_PERIMETER)
			fire(1.5);
		else {// long shot
			if (lastChance)
				fire(0.05);
			else
				// if(vel < 3) {
				fire(0.1);
			// }
			// else if(getOthers() <2)
			// fire(0.5);
		}
	}

	/**
	 * @param HitRobotEvent
	 *            e, The event generated when a robot hit our robot
	 */
	public void onHitRobot(HitRobotEvent e) {
		// if our robots health drops under
		if ((getEnergy() < LAST_STAND_HEALTH) && (!lastChance)) {
			lastChance = true;
			swapPositions();
		}

		current_mode = CLOSE_COMBAT_MODE;
		printMode();
		double gunTurnAmt = normalRelativeAngleDegrees(e.getBearing()
				+ (getHeading() - getRadarHeading()));
		turnGunRight(gunTurnAmt);
		fire(3);
		guyWhoHitMe = e.getName();
		// scan();

		// if(! isPositionedCorrectly())
		// swapPositions();
		/*
		 * ahead(500); turnRight(360-getHeading());
		 * ahead((getBattleFieldHeight()-getY())-50);
		 * 
		 * turnRight(90); turnGunRight(getHeading()); turnGunRight(90);
		 * ahead((getBattleFieldWidth()-getX())-50);
		 */
	}

	/*
	 * public void onRobotDeath(RobotDeathEvent e) {
	 * if(e.getName().equals(guyWhoHitMe))// { current_mode =
	 * STANDARD_COMBAT_MODE; //moveToNearestBase();
	 * 
	 * // positionGunForBase();
	 * 
	 * //} //sweepClock = QUICK_SWEEP_INTERVAL; }
	 */
	private void positionGunForBase() {
		if (current_base_station == BASE_STATION_TOP) {
			double gunTurnAmt = normalRelativeAngleDegrees(getHeading()
					- getGunHeading() + 90);
			turnGunRight(gunTurnAmt);
		} else {
			double gunTurnAmt = normalRelativeAngleDegrees(getHeading()
					- getGunHeading() - 90);
			turnGunRight(gunTurnAmt);
		}
	}

	private void turnShortestDirection(double degrees) {
		if (degrees < 180)
			turnRight(degrees);
		else
			turnLeft(360 - degrees);
	}

	private void turnGunShortestDirection(double degrees) {
		if (degrees < 180)
			turnGunRight(degrees);
		else
			turnGunLeft(360 - degrees);
	}

	private void curiousity() {

	}

	private String[][] grids = new String[5][5];

	String guyWhoHitMe = "";
}
