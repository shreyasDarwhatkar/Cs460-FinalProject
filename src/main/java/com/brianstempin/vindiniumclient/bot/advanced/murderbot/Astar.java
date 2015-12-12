package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState;



public class Astar {
	public String getShortestPath(AdvancedMurderBot.GameContext context,GameState.Position source,GameState.Position target)
	{
		Map<GameState.Position, Vertex> boardGraph = context.getGameState().getBoardGraph();
		PriorityQueue<Vertex> pQueue = new PriorityQueue<Vertex>(context.getGameState().getBoardGraph().size(), new Comparator<Vertex>() {
	        public int compare(Vertex vertex1,Vertex vertex2) {
	            return (vertex1.getValue() >= vertex2.getValue()) ? (1): (-1);
	        }
	    });
		
		HashMap<Vertex,Integer> vertexValue=new HashMap<Vertex,Integer>();
		HashMap<Vertex,Vertex> parent=new HashMap<Vertex,Vertex>();
		
		for(GameState.Position pos:boardGraph.keySet())
		{
			if(pos.getX()==source.getX() && pos.getY()==source.getY())
			{
				boardGraph.get(pos).setValue(0);
				pQueue.add(boardGraph.get(pos));
			}
			
		}
		
		while(!pQueue.isEmpty())
		{
			Vertex v=pQueue.poll();
			for(Vertex obj:v.getAdjacentVertices())
			{
				
				if (obj.getPosition()==target)
				{
					break;
				}
				pQueue.add(obj);
			}
			
		}
		
		
		
		
		return null;
	}

}
