package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedBot;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState;

/**
 * An improvement upon com.brianstempin.vindiniumClient.bot.simple.MurderBot
 *
 * This class uses a built-in static method to perform the path search via Dijkstra and uses a simple version of
 * behavior trees to determine its next action.
 */
public class AdvancedMurderBot implements AdvancedBot {

    public static class GameContext {
        private final AdvancedGameState gameState;
        private final Map<GameState.Position, DijkstraResult> dijkstraResultMap;

        public GameContext(AdvancedGameState gameState, Map<GameState.Position, DijkstraResult> dijkstraResultMap) {
            this.gameState = gameState;
            this.dijkstraResultMap = dijkstraResultMap;
        }

        public AdvancedGameState getGameState() {
            return gameState;
        }

        public Map<GameState.Position, DijkstraResult> getDijkstraResultMap() {
            return dijkstraResultMap;
        }
    }

    /**
     * Represents the result of a Dijkstra search for a given position
     */
    public static class DijkstraResult {
        private int distance;
        private GameState.Position previous;

        public DijkstraResult(int distance, GameState.Position previous) {
            this.distance = distance;
            this.previous = previous;
        }

        public int getDistance() {
            return distance;
        }

        public GameState.Position getPrevious() {
            return previous;
        }
    }

    public static synchronized Map<GameState.Position, DijkstraResult> dijkstraSearch(AdvancedGameState gameState) {
    	Map<GameState.Position, DijkstraResult> result = new HashMap<>();
        final Map<GameState.Position, Double> mapDistance = new HashMap<GameState.Position, Double>();
        double hvalue= 0;
        PriorityQueue<GameState.Position> distQueue = new PriorityQueue<GameState.Position>(gameState.getBoardGraph().size(), new Comparator<GameState.Position>() {
	        public int compare(GameState.Position p1,GameState.Position p2) {
	            return (mapDistance.get(p1)>= mapDistance.get(p2)) ? (1): (-1);
	        }
	    });
        
        Map<GameState.Position, Boolean> visitedMap = new HashMap<>();

        DijkstraResult startingResult = new DijkstraResult(0, null);
        
        
        
        mapDistance.put(gameState.getMe().getPos(), 0.0);
        
        distQueue.add(gameState.getMe().getPos());
        result.put(gameState.getMe().getPos(), startingResult);
        
        while(!distQueue.isEmpty()) {
            GameState.Position currentPosition = distQueue.poll();
            DijkstraResult currentResult = result.get(currentPosition);
            Vertex currentVertext = gameState.getBoardGraph().get(currentPosition);
            visitedMap.put(currentPosition, true);
            currentVertext.setValue(0);
            if(gameState.getHeroesByPosition().containsKey(currentPosition)
                    && !currentPosition.equals(gameState.getMe().getPos()))
                continue;

            int distance = currentResult.getDistance() + 1;
            //distance = curr + 1
     
            for(Vertex neighbor : currentVertext.getAdjacentVertices()) {
            	if(!visitedMap.containsKey(neighbor.getPosition())){
            		DijkstraResult neighborResult = result.get(neighbor.getPosition());
            		if(neighborResult == null) {
            			double xDiff=(currentPosition.getX() - neighbor.getPosition().getX())*currentPosition.getX() - (neighbor.getPosition().getX());
            			double yDiff=(currentPosition.getY() - neighbor.getPosition().getY())*(currentPosition.getY() - neighbor.getPosition().getY());
            			hvalue = distance + Math.sqrt(xDiff+yDiff);
            			neighbor.setValue(hvalue);
            			mapDistance.put(neighbor.getPosition(), hvalue);
                        neighborResult = new DijkstraResult(distance, currentPosition);
                        result.put(neighbor.getPosition(), neighborResult);
                        distQueue.remove(neighbor.getPosition());
                        distQueue.add(neighbor.getPosition());
                    } else if(mapDistance.get(neighbor.getPosition()) > hvalue) {
                        DijkstraResult newNeighborResult = new DijkstraResult(distance, currentPosition);
                        result.put(neighbor.getPosition(), newNeighborResult);
                        mapDistance.put(neighbor.getPosition(), hvalue);
                        distQueue.remove(neighbor.getPosition());
                        distQueue.add(neighbor.getPosition());
                    }
            		
            	}
            }
        }

        return result;
    	}

    private final Decision<GameContext, BotMove> decisioner;

    public AdvancedMurderBot() {

        // Chain decisioners together
        //SquatDecisioner squatDecisioner = new SquatDecisioner();
        UnattendedMineDecisioner unattendedMineDecisioner = new UnattendedMineDecisioner();
        HealDecisioner healDecisioner = new HealDecisioner();
        SquatDecisioner squatDecisioner = new SquatDecisioner();
        BotTargetingDecisioner botTargetingDecisioner = new BotTargetingDecisioner(unattendedMineDecisioner);
        EnRouteLootingDecisioner enRouteLootingDecisioner = new EnRouteLootingDecisioner(botTargetingDecisioner,healDecisioner);

        
        CombatOutcomeDecisioner combatOutcomeDecisioner = new CombatOutcomeDecisioner(botTargetingDecisioner,
                botTargetingDecisioner);
        BotWellnessDecisioner checkBotwellnessBeforefight = new BotWellnessDecisioner(enRouteLootingDecisioner, combatOutcomeDecisioner);

        CombatEngagementDecisioner combatEngagementDecisioner = new CombatEngagementDecisioner(combatOutcomeDecisioner,
                healDecisioner);
        BotWellnessDecisioner botWellnessDecisioner = new BotWellnessDecisioner(enRouteLootingDecisioner, combatEngagementDecisioner);

        this.decisioner = botWellnessDecisioner;

    }

    @Override
    public BotMove move(AdvancedGameState gameState) {

        Map<GameState.Position, DijkstraResult> dijkstraResultMap = dijkstraSearch(gameState);

        GameContext context = new GameContext(gameState, dijkstraResultMap);
        return this.decisioner.makeDecision(context);

    }

    @Override
    public void setup() {
        // No-op
    }

    @Override
    public void shutdown() {
        // No-op
    }
    
    
    
    public static class AstarResult {
        private int distance;
        private GameState.Position previous;

        public AstarResult(int distance, GameState.Position previous) {
            this.distance = distance;
            this.previous = previous;
        }

        public int getDistance() {
            return distance;
        }

        public GameState.Position getPrevious() {
            return previous;
        }

		public void setDistance(int distance) {
			this.distance = distance;
		}
    }

    public static synchronized Map<GameState.Position, AstarResult> AstarSearch(AdvancedGameState gameState) {
    	
    	PriorityQueue<Vertex> tileQueue = new PriorityQueue<Vertex>(gameState.getBoardGraph().size(), new Comparator<Vertex>() {
	        public int compare(Vertex tile1,Vertex tile2) {
	            return (tile1.getValue()>= tile2.getValue()) ? (1): (-1);
	        }
	    });
        Map<GameState.Position, AstarResult> result = new HashMap<>();
        Map<Vertex, Vertex> parent= new HashMap<>();
        Map<Vertex,Integer> mapvalue=new HashMap<>();
        //parent.put(gameState.getMe(), value)
        Map<GameState.Position,Vertex> valuemp=new HashMap<>();

        AstarResult startingResult = new AstarResult(0, null);
        Queue<GameState.Position> queue = new ArrayBlockingQueue<>(gameState.getBoardGraph().size());
        queue.add(gameState.getMe().getPos());
        result.put(gameState.getMe().getPos(), startingResult);
        Vertex sourceVertex=gameState.getBoardGraph().get(gameState.getMe().getPos());
        sourceVertex.setValue(0);
        mapvalue.put(sourceVertex,0);

        while(!queue.isEmpty()) {
            GameState.Position currentPosition = queue.poll();
            AstarResult currentResult = result.get(currentPosition);
            Vertex currentVertext = gameState.getBoardGraph().get(currentPosition);
            

            // If there's a bot here, then this vertex goes nowhere
            if(gameState.getHeroesByPosition().containsKey(currentPosition)
                    && !currentPosition.equals(gameState.getMe().getPos()))
                continue;

            //int distance = currentResult.getDistance() + 1;
            double pathdiff=mapvalue.get(currentVertext)+1;

            for(Vertex neighbor : currentVertext.getAdjacentVertices()) {
            	parent.put(neighbor,currentVertext);
            	int rowdiff=neighbor.getPosition().getX()-sourceVertex.getPosition().getX();
            	int coldiff=neighbor.getPosition().getY()-sourceVertex.getPosition().getY();
            	double aStarDist=pathdiff+Math.sqrt((rowdiff*coldiff)+(rowdiff *coldiff));
            	neighbor.setValue(aStarDist);
                AstarResult neighborResult = result.get(neighbor.getPosition());
                if(neighborResult == null) {
                    neighborResult = new AstarResult((int)(aStarDist), currentPosition);
                    result.put(neighbor.getPosition(), neighborResult);
                    queue.remove(neighbor.getPosition());
                    queue.add(neighbor.getPosition());
                } else if(neighborResult.distance > aStarDist) {
                    AstarResult newNeighborResult = new AstarResult((int)(aStarDist), currentPosition);
                    result.put(neighbor.getPosition(), newNeighborResult);
                    queue.remove(neighbor.getPosition());
                    queue.add(neighbor.getPosition());
                }
            }
        }

        return result;
    }
    
    
    
    
}
