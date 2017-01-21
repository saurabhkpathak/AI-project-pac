package pacman.controllers.examples;

import java.awt.Color;
import java.util.*;

import com.sun.org.apache.bcel.internal.Constants;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.game.GameView;

import static pacman.game.Constants.*;

public class TeamPacman extends Controller<MOVE>
{
	private ArrayList<SubsumptionLayer> layers = new ArrayList<SubsumptionLayer>();

	//add the subsumption layers
	//uses default values found through testing
	public TeamPacman()
	{
		layers.add(new RunLayer(5));
		layers.add(new KillLayer(75));
		layers.add(new EatLayer());
	}

	public TeamPacman(double killDistance,double runDistance)
	{
		layers.add(new RunLayer(runDistance));
		layers.add(new KillLayer(killDistance));
		layers.add(new EatLayer());
	}

	@Override
	//cycle through the layers and apply the first move we come across
	public MOVE getMove(Game game, long timeDue)
	{
		for(int i = 0 ; i < layers.size() ; i++)
		{
			MOVE move = layers.get(i).generateMove(game, timeDue);
			if(move!=null)
			{
				return move;
			}
		}
		return null;
	}

	//abstract subsumption layer class to allow a polymorphic subsumption architecture
	public abstract class SubsumptionLayer
	{
		public abstract MOVE generateMove(Game game, long timeDue);
	}

	//attempts to kill ghosts that are within the specified kill distance and over a certain edible time
	public class KillLayer extends SubsumptionLayer
	{
		private double m_killDistance;
		public KillLayer(double killDistance)
		{
			m_killDistance = killDistance;
		}
		@Override
		public MOVE generateMove(Game game, long timeDue) {
			//get ms pac-man's current position
			int currentNode = game.getPacmanCurrentNodeIndex();
			//get the indices of the ghosts so we can figure out where they are
			int[] ghostNodes = {
					game.getGhostCurrentNodeIndex(GHOST.BLINKY),
					game.getGhostCurrentNodeIndex(GHOST.INKY),
					game.getGhostCurrentNodeIndex(GHOST.PINKY),
					game.getGhostCurrentNodeIndex(GHOST.SUE)
			};
			int[] edibleTimes = {
					game.getGhostEdibleTime(GHOST.BLINKY),
					game.getGhostEdibleTime(GHOST.INKY),
					game.getGhostEdibleTime(GHOST.PINKY),
					game.getGhostEdibleTime(GHOST.SUE)
			};
			//find the closest ghost
			int closestGhost = -1;
			double closestGhostDistance = Double.MAX_VALUE;
			for(int i = 0 ; i < ghostNodes.length ; i++)
			{
				double dist = game.getDistance(currentNode, ghostNodes[i], DM.PATH);
				//make sure they aren't in the spawn zone and aren't edible
				if(dist<0 || edibleTimes[i]<=0)
				{
					dist = Double.MAX_VALUE;
				}
				//see if they are the closest
				else if (dist<closestGhostDistance)
				{
					closestGhostDistance = dist;
					closestGhost = i;
				}
			}
			if(closestGhost!=-1 && closestGhostDistance<m_killDistance)
			{
				return game.getNextMoveTowardsTarget(currentNode, ghostNodes[closestGhost], DM.PATH);
			}
			return null;
		}

	}

	//attempts to run from the nearest ghost - if they are within the specified runDistance
	//and under the specified edible time
	public class RunLayer extends SubsumptionLayer
	{
		private double m_runDistance;

		public RunLayer(double runDistance)
		{
			m_runDistance = runDistance;
		}
		@Override
		public MOVE generateMove(Game game, long timeDue) {
			//get ms pac-man's current position
			int currentNode = game.getPacmanCurrentNodeIndex();
			//get the indices of the ghosts so we can figure out where they are
			int[] ghostNodes = {
					game.getGhostCurrentNodeIndex(GHOST.BLINKY),
					game.getGhostCurrentNodeIndex(GHOST.INKY),
					game.getGhostCurrentNodeIndex(GHOST.PINKY),
					game.getGhostCurrentNodeIndex(GHOST.SUE)
			};
			int[] edibleTimes = {
					game.getGhostEdibleTime(GHOST.BLINKY),
					game.getGhostEdibleTime(GHOST.INKY),
					game.getGhostEdibleTime(GHOST.PINKY),
					game.getGhostEdibleTime(GHOST.SUE)
			};
			//find the closest ghost
			int closestGhost = -1;
			double closestGhostDistance = Double.MAX_VALUE;
			for(int i = 0 ; i < ghostNodes.length ; i++)
			{
				double dist = game.getDistance(currentNode, ghostNodes[i], DM.PATH);
				//make sure they aren't in the spawn zone or aren't edible
				if(dist<0 || edibleTimes[i]>0)
				{
					dist = Double.MAX_VALUE;
				}
				//see if they are the closest
				else if (dist<closestGhostDistance)
				{
					closestGhostDistance = dist;
					closestGhost = i;
				}
			}
			//if ghost is too close avoid
			if(closestGhost!=-1 && closestGhostDistance<m_runDistance)
			{
				return game.getNextMoveAwayFromTarget(currentNode, ghostNodes[closestGhost], DM.PATH);
			}
			return null;
		}

	}

	//eat behaviour - gets all pills in the game world and directs Ms Pac-Man towards the nearest one
	public class EatLayer extends SubsumptionLayer
	{

		@Override
		public MOVE generateMove(Game game, long timeDue) {
			//get ms pac-man's current position
			int currentNode = game.getPacmanCurrentNodeIndex();
			//get all current pills and add them as targets
			int length1 = game.getActivePillsIndices().length;
			int length2 = game.getActivePowerPillsIndices().length;
			int[] targets = new int[length1+length2];
			System.arraycopy(game.getActivePillsIndices(), 0, targets, 0, length1);
			System.arraycopy(game.getActivePowerPillsIndices(), 0, targets, length1, length2);
			//find closest pill
			int target = game.getClosestNodeIndexFromNodeIndex(currentNode, targets, DM.PATH);
			return game.getNextMoveTowardsTarget(currentNode, target, DM.PATH);
		}

	}


}
