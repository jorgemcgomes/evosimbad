/*
 * Created on 19-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.applications.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.apache.log4j.Category;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.core.NEATNodeGene;

/**
 * Provides a rudimentary visualisation of a NEAT network.
 * @author MSimmerson
 *
 */public class NEATFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private NEATNeuralNet net;
	
	public NEATFrame(NEATNeuralNet net) {
		super("NEAT Prediction Engine");
		this.net = net;
		this.addWindowListener(new WindowAdapter() {
		    	public void windowClosing(WindowEvent e) {
		    		System.exit(0);
		    	}
			}
		);
	}
	
	public void showNet() {
		Container c = this.getContentPane();
		int width = c.getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();
		int height = c.getGraphicsConfiguration().getDevice().getDisplayMode().getHeight();
		NEATCanvas canvas = new NEATCanvas(this.net, height, width);
		c.add(canvas);
		canvas.display();
		this.pack();
		this.setVisible(true);
	}
}

class NEATCanvas extends Canvas {
	private static final Category cat = Category.getInstance(NEATCanvas.class); 
	private NEATNeuralNet net;
	private int canvasH;
	private int canvasW;
	private static final int X_OFFSET = 50;
	private static final int Y_OFFSET = 30;
	private static final int R_OFFSET = 10;
	private static final int F_OFFSET = 20;
	private static final int N_SIZE = 15;

	public NEATCanvas(NEATNeuralNet net, int height, int width) {
		this.net = net;
		this.canvasH = height;
		this.canvasW = width;
		this.setBackground(Color.DARK_GRAY);
	}

	public Dimension getPreferredSize() {
		return (new Dimension(this.canvasW , this.canvasH));
	}

	public void display() {
		//this.repaint();
	}
	
	private NEATNeuron[][] analyseNeuronStructure() {
		int maxDepth = 1;
		int maxWidth = 0;
		int i;
		int row = 0;
		int col = 0;
		NEATNeuron[] neurons = this.net.neurons();		
		NEATNeuron[][] neuronStructure; 
		NEATNeuron neuron;
		
		// will only need the first few entries, but htis will cope with wierd structures
		int[] nDepthWidth = new int[neurons.length];
		int inputs = this.net.netDescriptor().numInputs();
		
		for (i = 0; i < neurons.length; i++) {
			if (neurons[i].neuronDepth() >= 0 && neurons[i].neuronType() != NEATNodeGene.INPUT) {
				if (neurons[i].neuronType() == NEATNodeGene.OUTPUT) {
					nDepthWidth[0]++; 
				} else if (neurons[i].neuronType() == NEATNodeGene.HIDDEN) {
					if (neurons[i].neuronDepth() > (maxDepth - 1)) { 
						maxDepth = neurons[i].neuronDepth() + 1;
					}
					nDepthWidth[neurons[i].neuronDepth()]++; 
				}
				if (nDepthWidth[neurons[i].neuronDepth()] > maxWidth) {
					maxWidth = nDepthWidth[neurons[i].neuronDepth()];
				}
			}
		}
		// and one for the inputs
		maxDepth++;
		// ensure array is wide enough
		if (inputs > maxWidth) {
			maxWidth = inputs;
		}
		neuronStructure = new NEATNeuron[maxDepth][maxWidth];
		nDepthWidth = new int[neurons.length];
		
		for (i = 0; i < neurons.length; i++) {
			neuron = neurons[i];
			if (neuron.neuronDepth() >= 0) {
				if (neuron.neuronType() == NEATNodeGene.INPUT) {
					row = maxDepth - 1;
				} else {
					row = neuron.neuronDepth();
				}
				col = nDepthWidth[row];
				neuronStructure[row][col] = neuron;
				nDepthWidth[row]++;
			}
		}
		
		return (neuronStructure);
	}
	
	public void paint(Graphics g) {
		int row = 0;
		int col = 0;
		int i;
		int j;
		NEATNeuron[][] structure = this.analyseNeuronStructure();
		NEATNeuron neuron;
		DisplayNeuron[] displayNeurons = new DisplayNeuron[structure.length * structure[0].length];
		DisplayNeuron[] displaySources;
		DisplayNeuron from;
		DisplayNeuron to;
		ArrayList structureList = new ArrayList();
		ArrayList rowList;
		
		for (row = 0; row < structure.length; row++) {
			rowList = new ArrayList(); 
			for (col = 0; col < structure[0].length; col++) {
				neuron = structure[row][col];
				if (neuron != null) {
					rowList.add(neuron);
				}
			}
			structureList.add(rowList);
		}
		
		for (row = 0; row < structureList.size(); row++) {
			rowList = (ArrayList)structureList.get(row);
			for (col = 0; col < rowList.size(); col++) {
				neuron = (NEATNeuron)rowList.get(col);
				displayNeurons[(row * structure[0].length) + col] = new DisplayNeuron(neuron, ((this.canvasW / (rowList.size() + 1)) * (col + 1)), (((this.canvasH / structureList.size()) - (2 * F_OFFSET)) * (row + 1)));
			}
		}
		
		// now create the links
		for (i = 0; i < displayNeurons.length; i++) {
			from = displayNeurons[i];
			if (from != null) {
				displaySources = this.findDisplaySources(displayNeurons, from);
				for (j = 0; j < displaySources.length; j++) {
					this.drawLink(from, displaySources[j], g);
				}
				this.drawNeuron(from, g);
			}
		}
	}
	
	private DisplayNeuron[] findDisplaySources(DisplayNeuron[] displayNeurons, DisplayNeuron from) {
		ArrayList sourceNeurons = from.neuron().sourceNeurons();
		DisplayNeuron[] targets = new DisplayNeuron[sourceNeurons.size()];
		int i;
		
		for (i = 0; i < targets.length; i++) {
			targets[i] = this.findTarget(displayNeurons, ((NEATNeuron)sourceNeurons.get(i)).id());
		}		
		
		return (targets);
	}
	
	private DisplayNeuron findTarget(DisplayNeuron[] displayNeurons, int id) {
		int i = 0;
		boolean found = false;
		DisplayNeuron target = null;
		
		while (i < displayNeurons.length && ! found) {
			if (displayNeurons[i] != null) {
				if (displayNeurons[i].neuron().id() == id) {
					target = displayNeurons[i];
					found = true;
				}
			}
			i++;
		}		
		
		return (target);
	}
	
	private void drawNeuron(DisplayNeuron neuron, Graphics g) {
		if (neuron.neuron().neuronType() == NEATNodeGene.INPUT) {
			g.setColor(Color.MAGENTA);
		} else if (neuron.neuron().sourceNeurons().size() == 0) {
			g.setColor(Color.GREEN);
		} else if (neuron.neuron().neuronType() == NEATNodeGene.OUTPUT) {
			g.setColor(Color.ORANGE);
		} else {
			g.setColor(Color.LIGHT_GRAY);
		}
		g.fillRoundRect(neuron.x(), neuron.y(), N_SIZE, N_SIZE,5,5);
		g.setColor(Color.WHITE);
		g.drawString("id=" + neuron.neuron().id(), neuron.x(), neuron.y());
	}
	
	private void drawLink(DisplayNeuron from, DisplayNeuron to, Graphics g) {
		if (from.neuron().id() == to.neuron().id()) {
			g.setColor(Color.BLUE);
			g.drawLine(from.x() + N_SIZE, from.y() + R_OFFSET, to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET);
			g.drawLine(to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET, to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET + N_SIZE);
			g.drawLine(to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET + N_SIZE, to.x() + R_OFFSET, to.y() + R_OFFSET + N_SIZE);
			g.drawLine(to.x() + R_OFFSET, to.y() + R_OFFSET + N_SIZE, to.x() + R_OFFSET, to.y());
		} else if (from.neuron().neuronDepth() >= to.neuron().neuronDepth()) {
			g.setColor(Color.YELLOW);
			g.drawLine(to.x(), to.y(), to.x() - R_OFFSET, to.y() - R_OFFSET);
			g.drawLine(to.x() - R_OFFSET, to.y() - R_OFFSET, to.x() - R_OFFSET, to.y() + R_OFFSET);
			g.drawLine(to.x() - R_OFFSET, to.y() + R_OFFSET, from.x(), from.y());
		} else {
			g.setColor(Color.RED);
			g.drawLine(to.x() + (N_SIZE / 2), to.y() + (N_SIZE / 2), to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2));
			g.drawLine(to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2), to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2) - F_OFFSET);
			g.drawLine(to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2) - F_OFFSET, from.x() + (N_SIZE / 2), from.y() + (N_SIZE / 2));
		}
	}
}

class DisplayNeuron {
	private NEATNeuron neuron;
	private int x;
	private int y;
	
	public DisplayNeuron(NEATNeuron neuron, int x, int y) {
		this.neuron = neuron;
		this.x = x;
		this.y = y;
	}
	
	public int x() {
		return (this.x);
	}
	
	public int y() {
		return (this.y);
	}
	
	public NEATNeuron neuron() {
		return (this.neuron);
	}
}