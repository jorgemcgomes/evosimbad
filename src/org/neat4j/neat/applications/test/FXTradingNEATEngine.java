/*
 * Created on 05-Jun-2005,
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.applications.test;

import java.io.IOException;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.core.ApplicationEngine;
import org.neat4j.neat.applications.core.NEATApplicationEngine;
import org.neat4j.neat.applications.gui.NEATFrame;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.data.core.ModifiableNetworkInput;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkInputSet;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVDataLoader;
import org.neat4j.neat.data.modify.ModifiableInput;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 * Forex Trading application based on a NEAT network. 
 * @author MSimmerson
 *
 */public class FXTradingNEATEngine extends NEATApplicationEngine {
	private static final Category cat = Category.getInstance(NEATApplicationEngine.class);
	private NetworkDataSet netData;
	private static final double STERLING_AMOUNT = 5000;
	private static final int TRADE_AMOUNT = 100000;
	private static final double CALL_AMOUNT = 500; 
	private static final double MARGIN = 0.02;
	private static final double PIP_COMMISSION = 0.0004;
	private static final double SELL_BOUNDARY = 1.0 / 3.0;
	private static final double BUY_BOUNDARY = 2.0 / 3.0;
	private static final double[] FX_RATES = new double[]{   
		1.742,
		1.7467,
		1.7424,
		1.7349,
		1.7468,
		1.7369,
		1.7398,
		1.7539,
		1.7514,
		1.7509,
		1.7416,
		1.7428,
		1.7496,
		1.7496,
		1.7512,
		1.7507,
		1.7699,
		1.7822,
		1.7907,
		1.7777,
		1.7817,
		1.7855,
		1.7879,
		1.7846,
		1.8014,
		1.8251,
		1.8231,
		1.8364,
		1.8408,
		1.8512,
		1.8589,
		1.8579,
		1.8658,
		1.8584,
		1.8842,
		1.8941,
		1.8774,
		1.8857,
		1.8821,
		1.8945,
		1.8774,
		1.8844,
		1.8788,
		1.8695,
		1.8741,
		1.8583,
		1.8583,
		1.881,
		1.8708,
		1.8665,
		1.8814,
		1.8714,
		1.8605,
		1.857,
		1.8429,
		1.8404,
		1.8405,
		1.8345,
		1.844,
		1.8509,
		1.8501,
		1.8404,
		1.8448,
		1.8464,
		1.828,
		1.8187,
		1.8255,
		1.8213,
		1.8176,
		1.8277,
		1.8499,
		1.8427
	};

	/**
	 * Initialises application environment
	 */
	public void initialise(AIConfig config) throws InitialisationFailedException {	
		String aiSource = config.configElement("AI.SOURCE");
		// what type is the AI?
//		String aiType = config.configElement("AI.TYPE");
		try {
//			if (GA.equalsIgnoreCase(aiType)) {
			Chromosome chromo = (Chromosome)this.readObject(aiSource);
			// need to create a nn based on this chromo.
			this.setNet(this.createNet(config));
			((NEATNetDescriptor)(this.net().netDescriptor())).updateStructure(chromo);
			((NEATNeuralNet)this.net()).updateNetStructure();
			this.showNet();
//			} else {
//				throw new InitialisationFailedException("Illegal AI Type:" + aiType);
//			}
			
			// now setup the input data
			String dataFile = config.configElement("INPUT.DATA");
			if (dataFile != null) {
				this.setNetData(new CSVDataLoader(dataFile, 0).loadData());
			}
		} catch (IOException e) {
			throw new InitialisationFailedException("Problem loading " + aiSource + ":" + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new InitialisationFailedException("Cannot find class for " + aiSource + ":" + e.getMessage());
		} catch (ClassCastException e) {
			throw new InitialisationFailedException("Incompatable AI source and type" + aiSource);
		}
	}
	
	private void showNet() {
		NEATFrame frame = new NEATFrame((NEATNeuralNet)this.net());
		frame.showNet();
	}
	
	/**
	 * creates a NEAT neural net based on data described by config 
	 */
	public NeuralNet createNet(AIConfig config) throws InitialisationFailedException {
		String nnConfigFile;
		AIConfig nnConfig;
		NEATNetManager netManager;
		
		nnConfigFile = config.configElement("NN.CONFIG");
		nnConfig  = new NEATLoader().loadConfig(nnConfigFile);
		nnConfig.updateConfig("INPUT_SIZE", config.configElement("INPUT.NODES"));
		nnConfig.updateConfig("OUTPUT_SIZE", config.configElement("OUTPUT.NODES"));
		netManager = new NEATNetManager();
		netManager.initialise(nnConfig);
		
		return ((NEATNeuralNet)netManager.managedNet());
	}
	
	/**
	 * @see org.neat4j.ailibrary.applications.core.ApplicationEngine#runApplication()
	 */
	public void runApplication() {
		NetworkDataSet dataSet = this.netData();
		this.trade(dataSet.inputSet());
	}
	
	private void trade(NetworkInputSet ipSet) {
		Trade trade = null;
		int i;
		double sterlingAmount = STERLING_AMOUNT;
		NetworkOutputSet opSet = null;
		NetworkInput ip;
		ModifiableNetworkInput modified = null;
		double[] op = null;
		int buys = 0;
		int sells = 0;
		int holds = 0;
		int profitableTrades = 0;
		int losingTrades = 0;
		double profit;
		int numAmounts = 1;
		double winnerVal = 0.5;
		double totalWinAmount = 0;
		double totalLossAmount = 0;

		for (i = 0; i < ipSet.size(); i++) {
			// close previous trade
			profit = this.closeTrade(trade, FX_RATES[i]);
			if (profit > 0) {
				totalWinAmount += profit;
				profitableTrades++;
				winnerVal = 1;
			} else if (profit < 0) {
				totalLossAmount -= profit;
				losingTrades++;
				winnerVal = 0;
			} else {
				winnerVal = 0.5;
			}
			sterlingAmount += profit;
			trade = null;
			if (i < (ipSet.size() - 1)) {
				ip = ipSet.inputAt(i);
				modified = new ModifiableInput(ip);
				modified.modifyLastInput(winnerVal);
				//opSet = this.net().execute(modified);
				opSet = this.net().execute(ip);
				op = opSet.nextOutput().values();
				cat.info("Net OP:" + op[0]);
				if (op[0] > BUY_BOUNDARY) {
					// buy
					//cat.info(op[0] + ":Buy at " + FX_RATES[i]);
					trade = new Trade(TRADE_AMOUNT, FX_RATES[i], Trade.BUY);
					buys++;
				} else if (op[0] > SELL_BOUNDARY) {
					// sell
					//cat.info(op[0] + ":Sell at " + FX_RATES[i]);
					trade = new Trade(TRADE_AMOUNT, FX_RATES[i], Trade.SELL);
					sells++;
				} else {
					//cat.info(op[0] + ":Hold at " + FX_RATES[i]);
					holds++;
				}
			}
			//cat.info("Sterling Amount:£" + sterlingAmount);	
			if (sterlingAmount - (TRADE_AMOUNT * FX_RATES[i] * MARGIN) < CALL_AMOUNT) {
				sterlingAmount += STERLING_AMOUNT;
				numAmounts++;
				cat.info("Adding Sterling, new Amount:£" + sterlingAmount);	
			}
		}
		cat.info("Summary:Trade Details: Buys:" + buys + " sells:" + sells + " holds:" + holds);
		cat.info("Summary:Trade Results: Winners:" + profitableTrades + " making " + totalWinAmount + " Losers:" + losingTrades + " losing " + totalLossAmount);
		cat.info("Profit on:" + (numAmounts * STERLING_AMOUNT) + " GBP is " + (sterlingAmount - (numAmounts * STERLING_AMOUNT)) + " GBP");	
	}

	private double closeTrade(Trade trade, double currentRate) {
		double profit = 0;
		
		if (trade != null) {
			if (trade.type() == Trade.BUY) {
				profit = (int)((currentRate - trade.rate() - PIP_COMMISSION) * trade.unitsTraded());
			} else if (trade.type() == Trade.SELL) {
				profit = (int)((trade.rate() - currentRate - PIP_COMMISSION) * trade.unitsTraded());
			}
			cat.info((trade.type() == Trade.SELL ? "Sell " : "Buy ") + "outcome:" + profit + " GBP");
		} else {
			cat.info("Hold");
		}
		
		return (profit);
	}

	public static void main(String[] args) {
		ApplicationEngine fpe = new FXTradingNEATEngine();
		AIConfig config = new NEATLoader().loadConfig(args[0]);
		try {
			fpe.initialise(config);
			fpe.runApplication();
		} catch (InitialisationFailedException e) {
			cat.error("Failed to initialise FXTradingNEATEngine:" + e.getMessage());
		}
	}
}

class Trade {
	public static int BUY = 1;
	public static int SELL = 2;
	private int unitsTraded;
	private double rate;
	private int tradeType;
	
	public Trade(int unitsTraded, double rate, int tradeType) {
		this.unitsTraded = unitsTraded;
		this.rate = rate;
		this.tradeType = tradeType;
	}
	
	public double unitsTraded() {
		return (this.unitsTraded);
	}
	
	public double rate() {
		return (this.rate);
	}
	
	public int type() {
		return (this.tradeType);
	}
}