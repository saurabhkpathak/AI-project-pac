package pacman.controllers.examples;

import java.awt.Color;
import java.util.*;

import com.sun.org.apache.bcel.internal.Constants;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.game.GameView;

import static pacman.game.Constants.*;

public class IcarusMan extends Controller<MOVE> 
{
	private ArrayList<SubsumptionLayer> layers = new ArrayList<SubsumptionLayer>();
	
	//add the subsumption layers
	//uses default values found through testing
	public IcarusMan()
	{
		layers.add(new EvadeLayer(5));
		layers.add(new HuntLayer(75));
		layers.add(new GatherLayer());
	}
	
	public IcarusMan(double huntDistance,double evadeDistance)
	{
		layers.add(new EvadeLayer(evadeDistance));
		layers.add(new HuntLayer(huntDistance));
		layers.add(new GatherLayer());
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
	
	//attempts to hunt ghosts that are within the specified hunt distance and over a certain edible time
	public class HuntLayer extends SubsumptionLayer
	{
		private double m_huntDistance;
		public HuntLayer(double huntDistance)
		{
			m_huntDistance = huntDistance;
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
			if(closestGhost!=-1 && closestGhostDistance<m_huntDistance)
			{
				return game.getNextMoveTowardsTarget(currentNode, ghostNodes[closestGhost], DM.PATH);
			}
			return null;
		}
		
	}
	
	//attempts to evade the nearest ghost - if they are within the specified evadeDistance
	//and under the specified edible time
	public class EvadeLayer extends SubsumptionLayer
	{
		private double m_evadeDistance;
		
		public EvadeLayer(double evadeDistance)
		{
			m_evadeDistance = evadeDistance;
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
			if(closestGhost!=-1 && closestGhostDistance<m_evadeDistance)
			{
				return game.getNextMoveAwayFromTarget(currentNode, ghostNodes[closestGhost], DM.PATH);
			}
			return null;
		}
		
	}
	
	//gather behaviour - gets all pills in the game world and directs Ms Pac-Man towards the nearest one
	public class GatherLayer extends SubsumptionLayer
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
