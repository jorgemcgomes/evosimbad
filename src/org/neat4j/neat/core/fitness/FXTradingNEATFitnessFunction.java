package org.neat4j.neat.core.fitness;

import org.apache.log4j.Category;
import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.data.core.ModifiableNetworkInput;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkInputSet;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.modify.ModifiableInput;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 * 
 * @author MSimmerson
 *
 * Example fitness function
 */
public class FXTradingNEATFitnessFunction extends NEATFitnessFunction {
	private static final Category cat = Category.getInstance(FXTradingNEATFitnessFunction.class);
	private static final double STERLING_AMOUNT = 5000;
	private static final int TRADE_AMOUNT = 100000;
	private static final double MARGIN = 0.02;
	private static final double PIP_COMMISSION = 0.0004;
	private static final double CALL_AMOUNT = 500;
	private static final double ONE_THIRD = 1.0 / 3.0;
	private static final double TWO_THIRDS = 2.0 / 3.0;
	private static final double[] FX_RATES = new double[]{
		1.8496,
		1.8385,
		1.844,
		1.8319,
		1.8395,
		1.8231,
		1.8238,
		1.8182,
		1.8195,
		1.8249,
		1.8235,
		1.8253,
		1.8228,
		1.8414,
		1.8399,
		1.8278,
		1.8322,
		1.8219,
		1.8436,
		1.8394,
		1.8291,
		1.8224,
		1.831,
		1.8183,
		1.8085,
		1.7909,
		1.7957,
		1.7955,
		1.7897,
		1.7949,
		1.8029,
		1.7934,
		1.7911,
		1.7766,
		1.7804,
		1.7741,
		1.7865,
		1.7875,
		1.7982,
		1.7978,
		1.7971,
		1.7763,
		1.7932,
		1.7926,
		1.7847,
		1.7957,
		1.7935,
		1.7976,
		1.8057,
		1.8113,
		1.8128,
		1.7999,
		1.8127,
		1.7991,
		1.7837,
		1.784,
		1.7795,
		1.7816,
		1.7945,
		1.7975,
		1.7906,
		1.7943,
		1.7952,
		1.8042,
		1.7963,
		1.8029,
		1.8171,
		1.8266,
		1.8287,
		1.8405,
		1.8348,
		1.8277,
		1.8317,
		1.8376,
		1.8324,
		1.8393,
		1.8474,
		1.8435,
		1.8547,
		1.8544,
		1.8554,
		1.8463,
		1.8431,
		1.8561,
		1.846,
		1.8529,
		1.8592,
		1.8484,
		1.8572,
		1.8595,
		1.8682,
		1.8809,
		1.8914,
		1.8938,
		1.8921,
		1.9094,
		1.9327,
		1.9237,
		1.9436,
		1.9364,
		1.9446,
		1.9369,
		1.923,
		1.9147,
		1.9236,
		1.927,
		1.9421,
		1.931,
		1.9416,
		1.9466,
		1.9288,
		1.9149,
		1.9236,
		1.9237,
		1.9342,
		1.9271,
		1.9175,
		1.9234,
		1.9181,
		1.9043,
		1.883,
		1.8851,
		1.8761,
		1.8706,
		1.8769,
		1.8782,
		1.8904,
		1.8808,
		1.8704,
		1.857,
		1.8627,
		1.8704,
		1.8702,
		1.8769,
		1.8784,
		1.8638,
		1.8813,
		1.8904,
		1.8879,
		1.8841,
		1.883,
		1.8843,
		1.8814,
		1.8757,
		1.8579,
		1.8535,
		1.859,
		1.8678,
		1.8683,
		1.8868,
		1.895,
		1.8842,
		1.8945,
		1.8942,
		1.8962,
		1.911,
		1.9062,
		1.9071,
		1.9193,
		1.9188,
		1.9182,
		1.9126,
		1.9065,
		1.9226,
		1.9147,
		1.9284,
		1.9225,
		1.9235,
		1.926,
		1.9131,
		1.913,
		1.9269,
		1.9248,
		1.9221,
		1.8954,
		1.8853,
		1.8703,
		1.8681,
		1.8704,
		1.8663,
		1.875,
		1.8792,
		1.8873,
		1.8822,
		1.8765,
		1.8795,
		1.879,
		1.8694,
		1.8857,
		1.8916,
		1.889,
		1.8937,
		1.8808,
		1.8928,
		1.9005,
		1.9167,
		1.9188,
		1.9078,
		1.9139,
		1.9111,
		1.9052,
		1.9048,
		1.9077,
		1.9086,
		1.8933,
		1.8928,
		1.9005,
		1.9002,
		1.8912,
		1.8816,
		1.8841,
		1.8721,
		1.8627,
		1.8506,
		1.837,
		1.8321,
		1.8389,
		1.8342,
		1.8283,
		1.829,
		1.8257,
		1.8321,
		1.8204,
		1.8241,
		1.8227,
		1.8176,
		1.8103,
		1.8153,
		1.8155,
		1.8227,
		1.8344,
		1.8237,
		1.8213,
		1.8123,
		1.8031,
		1.8058,
		1.821,
		1.8203,
		1.8309,
		1.8205,
		1.8289,
		1.8213,
		1.8156,
		1.8247,
		1.828,
		1.8164,
		1.8067,
		1.7899,
		1.7674,
		1.7609,
		1.7573,
		1.7532,
		1.7422,
		1.7379,
		1.7549,
		1.7745,
		1.7604,
		1.757,
		1.7519,
		1.7483,
		1.7368,
		1.7403,
		1.7534,
		1.7388,
		1.7434,
		1.7372,
		1.7434,
		1.7542,
		1.7577,
		1.7684,
		1.7711,
		1.7778,
		1.78,
		1.778,
		1.7853,
		1.7864,
		1.7963,
		1.8103,
		1.815,
		1.8087,
		1.8093,
		1.804,
		1.7929,
		1.7948,
		1.8013,
		1.8008,
		1.7983,
		1.7995,
		1.8006,
		1.7953,
		1.7851,
		1.8034,
		1.8317,
		1.8422,
		1.8405,
		1.8407,
		1.8367,
		1.8378,
		1.8395,
		1.8218,
		1.8231,
		1.8218,
		1.8065,
		1.8085,
		1.803,
		1.7968,
		1.8125,
		1.7905,
		1.7754,
		1.7777,
		1.7662,
		1.768,
		1.7583,
		1.7653,
		1.7548,
		1.7585,
		1.7677,
		1.7763,
		1.7607,
		1.7541,
		1.7431,
		1.7526,
		1.754,
		1.7686,
		1.7517,
		1.7467,
		1.7639,
		1.7759,
		1.7684,
		1.7664,
		1.783,
		1.7757,
		1.7835,
		1.7741,
		1.7684,
		1.7646,
		1.7758,
		1.7708,
		1.7496,
		1.7426,
		1.7422,
		1.7429,
		1.739,
		1.7413,
		1.7367,
		1.7359,
		1.7181,
		1.7193,
		1.7174,
		1.7154,
		1.7209,
		1.7216,
		1.7218,
		1.7143,
		1.7277,
		1.7164,
		1.7297,
		1.7324,
		1.7323,
		1.7413,
		1.7421,
		1.7348,
		1.7518,
		1.7551,
		1.7747,
		1.7697,
		1.7711,
		1.765,
		1.7727,
		1.7625,
		1.7562,
		1.7443,
		1.737,
		1.7334,
		1.731,
		1.729,
		1.719,
		1.7705,
		1.7641,
		1.7641,
		1.7661,
		1.7604,
		1.7769,
		1.7659,
		1.7668,
		1.7615,
		1.7563,
		1.7704,
		1.7866,
		1.7829,
		1.7834,
		1.7807,
		1.767,
		1.7664,
		1.7784,
		1.7744,
		1.7779,
		1.7628,
		1.7464,
		1.7463,
		1.7408,
		1.7444,
		1.743,
		1.7354,
		1.7397,
		1.7388,
		1.7421,
		1.7443,
		1.7451,
		1.7436,
		1.7515,
		1.7454,
		1.7393,
		1.7537,
		1.7495,
		1.7528,
		1.7555,
		1.7492,
		1.7356,
		1.7359,
		1.7352,
		1.7259,
		1.7354,
		1.7463,
		1.7471,
		1.7561,
		1.7561,
		1.7549,
		1.7485,
		1.7461,
		1.7336,
	};

	public FXTradingNEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		super(net, dataSet);
	}
	
	public double evaluate(Chromosome genoType) {
		int i;
		NetworkInputSet ipSet = this.evaluationData().inputSet();
		NetworkOutputSet opSet;
		NetworkInput ip = null;
		ModifiableNetworkInput modified = null;
		double[] op;
		double sterlingAmount = STERLING_AMOUNT;
		Trade trade = null;
		int numAmounts = 1;
		int winners = 0;
		int losers = 0;
		int holds = 0;
		double profit;
		int buys = 0;
		int sells = 0;
		int holdTrades = 0;
		double totalProfit = 0;
		double winnerVal = 0;
		double returnVal;
		double[] ops = new double[ipSet.size()];
		

		// need to create a net based on this chromo
		this.createNetFromChromo(genoType);
		// execute net over data set
		for (i = 0; i < ipSet.size(); i++) {
			// close previous trade
			profit = this.closeTrade(trade, FX_RATES[i]);
			sterlingAmount += profit;
			if (profit > 0) {
				winners++;
				winnerVal = 1;
			} else if (profit < 0){
				losers++;
				winnerVal = 0;
			} else {
				holds++;
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
				ops[i] = op[0];
				if (op[0] > TWO_THIRDS) {
					// buy
					buys++;
					trade = new Trade(TRADE_AMOUNT, FX_RATES[i], Trade.BUY);
				} else if (op[0] > ONE_THIRD) {
					// sell
					sells++;
					trade = new Trade(TRADE_AMOUNT, FX_RATES[i], Trade.SELL);
				} else {
					// hold
					holdTrades++;
				}
			}
			
			if (sterlingAmount - (TRADE_AMOUNT * FX_RATES[i] * MARGIN) < CALL_AMOUNT) {
				sterlingAmount += STERLING_AMOUNT;
				numAmounts++;
			}
		}

		totalProfit = sterlingAmount - (numAmounts * STERLING_AMOUNT);
		if (totalProfit == 0) {
			returnVal = 0;
		} else {
			returnVal = 100000 + totalProfit;
			//returnVal = Math.abs(totalProfit) / (winners + losers);
		}
//		cat.debug("winners:" + winners + ":losers:" + losers + ":totalProfit:" + totalProfit + ":return:" + returnVal);
//		
		return (returnVal);
//		return (100000 + totalProfit);
	}
	
	private double closeTrade(Trade trade, double currentRate) {
		double profit = 0;
		
		if (trade != null) {
			if (trade.type() == Trade.BUY) {
				profit = (int)((currentRate - trade.rate() - PIP_COMMISSION) * trade.unitsTraded());
			} else if (trade.type() == Trade.SELL) {
				profit = (int)((trade.rate() - currentRate - PIP_COMMISSION) * trade.unitsTraded());
			}
		}
		
		return (profit);
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