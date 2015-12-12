package com.brianstempin.vindiniumclient.bot.advanced;

import com.brianstempin.vindiniumclient.dto.GameState;
import com.brianstempin.vindiniumclient.dto.GameState.Position;

import java.util.List;

/**
 * Represents some traversable tile on the board
 */
public class Vertex {
	private double  value;
    private final GameState.Position position;
    private final List<Vertex> adjacentVertices;
    

    public Vertex(double value, Position position, List<Vertex> adjacentVertices) {
		super();
		this.value = value;
		this.position = position;
		this.adjacentVertices = adjacentVertices;
	}

	public Vertex(GameState.Position position, List<Vertex> adjacentVertices) {
        this.position = position;
        this.adjacentVertices = adjacentVertices;
    }

    public GameState.Position getPosition() {
        return position;
    }

    public List<Vertex> getAdjacentVertices() {
        return adjacentVertices;
    }

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
