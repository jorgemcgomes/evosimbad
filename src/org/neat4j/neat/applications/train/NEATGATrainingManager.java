package org.neat4j.neat.applications.train;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.core.fitness.InvalidFitnessFunction;
import org.neat4j.neat.core.mutators.InvalidMutatorFunction;
import org.neat4j.neat.core.pselectors.InvalidParentSelectorFunction;
import org.neat4j.neat.core.xover.InvalidCrossoverFunction;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.CrossOver;
import org.neat4j.neat.ga.core.FitnessFunction;
import org.neat4j.neat.ga.core.GADescriptor;
import org.neat4j.neat.ga.core.GeneticAlgorithm;
import org.neat4j.neat.ga.core.Mutator;
import org.neat4j.neat.ga.core.NeuralFitnessFunction;
import org.neat4j.neat.ga.core.ParentSelector;
import org.neat4j.neat.nn.core.LearningEnvironment;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 * Training control for a NEAT network based on given configuration.
 * @author MSimmerson
 *
 */
public class NEATGATrainingManager {
	private static final Category cat = Category.getInstance(NEATGATrainingManager.class);
	private GeneticAlgorithm ga;
	private AIConfig config;
	
	public GeneticAlgorithm ga() {
		return (this.ga);
	}
	/**
	 * @see org.neat4j.ailibrary.core.AIController#initialise(org.neat4j.ailibrary.AIConfig)
	 */
	public void initialise(AIConfig config) throws InitialisationFailedException {
		GADescriptor gaDescriptor = this.createDescriptor(config);
		this.assigGA(this.createGeneticAlgorithm(gaDescriptor));
		try {
			this.assignConfig(config);
			this.ga.pluginFitnessFunction(this.createFunction(config));
			this.ga.pluginCrossOver(this.createCrossOver(config));
			this.ga.pluginMutator(this.createMutator(config));
			this.ga.pluginParentSelector(this.createParentSelector(config));
			this.ga.createPopulation();
		} catch (InvalidFitnessFunction e) {
			throw new InitialisationFailedException(e.getMessage());
		} catch (InvalidCrossoverFunction e) {
			throw new InitialisationFailedException(e.getMessage());
		} catch (InvalidMutatorFunction e) {
			throw new InitialisationFailedException(e.getMessage());
		} catch (InvalidParentSelectorFunction e) {
			throw new InitialisationFailedException(e.getMessage());
		} catch (Exception e) {
			throw new InitialisationFailedException(e.getMessage());
		}
	}

	public void assigGA(GeneticAlgorithm ga) {
		this.ga = ga;
	}
	
	public void assignConfig(AIConfig config) {
		this.config = config;
	}

	/**
	 * Initiates an evaluation and evolution cycle.
	 *
	 */
	public void evolve() {
		int epochs = Integer.parseInt(config.configElement("NUMBER.EPOCHS"));
		double terminateVal = ((NEATGADescriptor)this.ga.descriptor()).getTerminationValue();
		boolean nOrder = ((NEATGADescriptor)this.ga.descriptor()).isNaturalOrder();
		boolean terminate = false;
		int i = 0;
		
		while (i < epochs && !terminate) {
			cat.info("Running Epoch[" + i + "]\r");
			this.ga.runEpoch();
			this.saveBest();
			if ((this.ga.discoverdBestMember().fitness() >= terminateVal && !nOrder) || (this.ga.discoverdBestMember().fitness() <= terminateVal && nOrder)) {
				terminate = true;
			}
			i++;
		}
		cat.debug("Innovation Database Stats - Hits:" + InnovationDatabase.database().hits + " - misses:" + InnovationDatabase.database().misses);
	}
	
	/**
	 * Saves the best candidate of the generation
	 *
	 */
	public void saveBest() {
		this.save(config.configElement("SAVE.LOCATION"), this.ga.discoverdBestMember());
	}
	
	/**
	 * @see org.neat4j.ailibrary.ga.control.GATrainingManager#createDescriptor(org.neat4j.ailibrary.AIConfig)
	 */
	public GADescriptor createDescriptor(AIConfig config) {
		int popSize = Integer.parseInt(config.configElement("POP.SIZE"));
		double pXover = Double.parseDouble(config.configElement("PROBABILITY.CROSSOVER"));
		double pAddLink = Double.parseDouble(config.configElement("PROBABILITY.ADDLINK"));
		double pAddNode = Double.parseDouble(config.configElement("PROBABILITY.ADDNODE"));
		double pToggleLink = Double.parseDouble(config.configElement("PROBABILITY.TOGGLELINK"));
		double pMutation = Double.parseDouble(config.configElement("PROBABILITY.MUTATION"));
		double pMutateBias = Double.parseDouble(config.configElement("PROBABILITY.MUTATEBIAS"));
		double pWeightReplaced = Double.parseDouble(config.configElement("PROBABILITY.WEIGHT.REPLACED"));
		double excessCoeff = Double.parseDouble(config.configElement("EXCESS.COEFFICIENT"));
		double disjointCoeff = Double.parseDouble(config.configElement("DISJOINT.COEFFICIENT"));
		double weightCoeff = Double.parseDouble(config.configElement("WEIGHT.COEFFICIENT"));
		double threshold = Double.parseDouble(config.configElement("COMPATABILITY.THRESHOLD"));
		double thresholdChange = Double.parseDouble(config.configElement("COMPATABILITY.CHANGE"));
		int inputNodes = Integer.parseInt(config.configElement("INPUT.NODES"));
		int outputNodes =Integer.parseInt(config.configElement("OUTPUT.NODES"));
		boolean naturalOrder = Boolean.valueOf((config.configElement("NATURAL.ORDER.STRATEGY"))).booleanValue();
		int maxSpecieAge = Integer.parseInt(config.configElement("SPECIE.FITNESS.MAX"));
		int specieAgeThreshold = Integer.parseInt(config.configElement("SPECIE.AGE.THRESHOLD"));
		int specieYouthThreshold = Integer.parseInt(config.configElement("SPECIE.YOUTH.THRESHOLD"));
		double agePenalty = Double.parseDouble(config.configElement("SPECIE.OLD.PENALTY"));
		double youthBoost = Double.parseDouble(config.configElement("SPECIE.YOUTH.BOOST"));
		int specieCount = Integer.parseInt(config.configElement("SPECIE.COUNT"));
		double survialThreshold = Double.parseDouble(config.configElement("SURVIVAL.THRESHOLD"));
		boolean featureSelection = Boolean.valueOf(config.configElement("FEATURE.SELECTION")).booleanValue();
		int extraAlleles = Integer.parseInt(config.configElement("EXTRA.FEATURE.COUNT"));
		boolean eleEvents = Boolean.valueOf(config.configElement("ELE.EVENTS")).booleanValue();
		double eleSurvivalCount = Double.parseDouble(config.configElement("ELE.SURVIVAL.COUNT"));
		int eleEventTime = Integer.parseInt(config.configElement("ELE.EVENT.TIME"));
		boolean recurrencyAllowed = Boolean.valueOf(config.configElement("RECURRENCY.ALLOWED")).booleanValue();
		boolean keepBestEver = Boolean.valueOf(config.configElement("KEEP.BEST.EVER")).booleanValue();
		double terminationValue = Double.parseDouble(config.configElement("TERMINATION.VALUE"));
		double maxPerturb = Double.parseDouble(config.configElement("MAX.PERTURB"));
		double maxBiasPerturb = Double.parseDouble(config.configElement("MAX.BIAS.PERTURB"));

		
		NEATGADescriptor descriptor = new NEATGADescriptor();
		descriptor.setPAddLink(pAddLink);
		descriptor.setPAddNode(pAddNode);
		descriptor.setPToggleLink(pToggleLink);
		descriptor.setPMutateBias(pMutateBias);
		descriptor.setPXover(pXover);
		descriptor.setPMutation(pMutation);
		descriptor.setInputNodes(inputNodes);
		descriptor.setOutputNodes(outputNodes);
		descriptor.setNaturalOrder(naturalOrder);
		descriptor.setPopulationSize(popSize);
		descriptor.setDisjointCoeff(disjointCoeff);
		descriptor.setExcessCoeff(excessCoeff);
		descriptor.setWeightCoeff(weightCoeff);
		descriptor.setThreshold(threshold);
		descriptor.setCompatabilityChange(thresholdChange);
		descriptor.setMaxSpecieAge(maxSpecieAge);
		descriptor.setSpecieAgeThreshold(specieAgeThreshold);
		descriptor.setSpecieYouthThreshold(specieYouthThreshold);
		descriptor.setAgePenalty(agePenalty);
		descriptor.setYouthBoost(youthBoost);
		descriptor.setSpecieCount(specieCount);
		descriptor.setPWeightReplaced(pWeightReplaced);
		descriptor.setSurvivalThreshold(survialThreshold);
		descriptor.setFeatureSelection(featureSelection);
		descriptor.setExtraFeatureCount(extraAlleles);
		descriptor.setEleEvents(eleEvents);
		descriptor.setEleSurvivalCount(eleSurvivalCount);
		descriptor.setEleEventTime(eleEventTime);
		descriptor.setRecurrencyAllowed(recurrencyAllowed);
		descriptor.setKeepBestEver(keepBestEver);
		descriptor.setTerminationValue(terminationValue);
		descriptor.setMaxPerturb(maxPerturb);
		descriptor.setMaxBiasPerturb(maxBiasPerturb);
		
		return (descriptor);
	}


	/**
	 * Creates a GA for NEAT evolution based on the descriptor 
	 * @param gaDescriptor
	 * @return created GA
	 */
	public GeneticAlgorithm createGeneticAlgorithm(GADescriptor gaDescriptor) {
		GeneticAlgorithm ga = new NEATGeneticAlgorithm((NEATGADescriptor)gaDescriptor);		
		return (ga);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.control.GATrainingManager#createFunction(java.lang.String)
	 */
	public FitnessFunction createFunction(AIConfig config) throws InvalidFitnessFunction {
		String functionClass = config.configElement("OPERATOR.FUNCTION");
		String nnConfigFile;
		FitnessFunction function = null;
		AIConfig nnConfig;
		Class funcClass;
		NEATNetManager netManager;
		NeuralNet net = null;
		NetworkDataSet dataSet = null;
		LearningEnvironment env;
		Constructor fConstructor;
		
		if (functionClass != null) {
			try {
				funcClass = Class.forName(functionClass);
				if (NeuralFitnessFunction.class.isAssignableFrom(funcClass)) {
					nnConfigFile = config.configElement("NN.CONFIG");
					nnConfig  = new NEATLoader().loadConfig(nnConfigFile);
					nnConfig.updateConfig("INPUT_SIZE", config.configElement("INPUT.NODES"));
					nnConfig.updateConfig("OUTPUT_SIZE", config.configElement("OUTPUT.NODES"));
					netManager = new NEATNetManager();
					netManager.initialise(nnConfig);
					net = netManager.managedNet();
					env = net.netDescriptor().learnable().learningEnvironment();
					dataSet = (NetworkDataSet)env.learningParameter("TRAINING.SET");
					fConstructor = funcClass.getConstructor(new Class[]{NeuralNet.class, NetworkDataSet.class});
					function = (FitnessFunction) fConstructor.newInstance(new Object[]{net, dataSet});
				} else {
					throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName());
				}
			} catch (ClassNotFoundException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (InstantiationException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (SecurityException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new InvalidFitnessFunction("Invalid function class, " + functionClass + " must extend " + NeuralFitnessFunction.class.getName() + ":" + e.getMessage());
			} catch (InitialisationFailedException e) {
				e.printStackTrace();
				throw new InvalidFitnessFunction("Could not create Firness function, configuration was invalid:" + e.getMessage());
			} 
		} else {
			throw new InvalidFitnessFunction("Function class was null");
		}
		
		return (function);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.control.GATrainingManager#createParentSelector(org.neat4j.ailibrary.AIConfig)
	 */
	public ParentSelector createParentSelector(AIConfig config) throws InvalidParentSelectorFunction {
		String pSelectorClass = config.configElement("OPERATOR.PSELECTOR");
		ParentSelector pSelector;
		
		if (pSelectorClass != null) {
			try {
				pSelector = (ParentSelector)Class.forName(pSelectorClass).newInstance();
			} catch (InstantiationException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			} catch (Exception e) {
				throw new InvalidParentSelectorFunction("Invalid Parent Selector class, " + pSelectorClass + ":" + e.getMessage());
			}
		} else {
			throw new InvalidParentSelectorFunction("Parent Selector class was null");
		}
		
		return (pSelector);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.control.GATrainingManager#createMutator(org.neat4j.ailibrary.AIConfig)
	 */
	public Mutator createMutator(AIConfig config) throws InvalidMutatorFunction {
		String mutatorClass = config.configElement("OPERATOR.MUTATOR");
		Mutator mutator;
		
		if (mutatorClass != null) {
			try {
				mutator = (Mutator)Class.forName(mutatorClass).newInstance();
			} catch (InstantiationException e) {
				throw new InvalidMutatorFunction("Invalid Mutator class, " + mutatorClass + ":" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidMutatorFunction("Invalid Mutator class, " + mutatorClass + ":" + e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new InvalidMutatorFunction("Invalid Mutator class, " + mutatorClass + ":" + e.getMessage());
			} catch (Exception e) {
				throw new InvalidMutatorFunction("Invalid Mutator class, " + mutatorClass + ":" + e.getMessage());
			}
		} else {
			throw new InvalidMutatorFunction("Mutator class was null");
		}
		
		return (mutator);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.control.GATrainingManager#createCrossOver(org.neat4j.ailibrary.AIConfig)
	 */
	public CrossOver createCrossOver(AIConfig config) throws InvalidCrossoverFunction {
		String xOverClass = config.configElement("OPERATOR.XOVER");
		CrossOver xOver;
		
		if (xOverClass != null) {
			try {
				xOver = (CrossOver)Class.forName(xOverClass).newInstance();
			} catch (InstantiationException e) {
				throw new InvalidCrossoverFunction("Cross Over class, " + xOverClass + ":" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidCrossoverFunction("Cross Over class, " + xOverClass + ":" + e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new InvalidCrossoverFunction("Cross Over class, " + xOverClass + ":" + e.getMessage());
			} catch (Exception e) {
				throw new InvalidCrossoverFunction("Cross Over class, " + xOverClass + ":" + e.getMessage());
			}
		} else {
			throw new InvalidCrossoverFunction("Cross Over class was null");
		}
		
		return (xOver);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNet#save(java.lang.String)
	 */
	public boolean save(String fileName, Chromosome genoType) {
		boolean saveOk = false;
		ObjectOutputStream s = null;
		FileOutputStream out = null;
		try {
			if (fileName != null)
			//System.out.println("Saving Best Chromosome to " + fileName);
			out = new FileOutputStream(fileName);
			s = new ObjectOutputStream(out);
			s.writeObject(genoType);
			s.flush();
			saveOk = true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (s != null) {
					s.close();
				}
				
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		//System.out.println("Saving Best Chromosome...Done");
		return (saveOk);
	}

	public static void main(String[] args) {
		NEATGATrainingManager gam = new NEATGATrainingManager();
		try {
			if (args.length != 1) {
				System.out.println("Usage: NEATGAManager <ga ga config file");
			} else {
				AIConfig config = new NEATLoader().loadConfig(args[0]);
				gam.initialise(config);
				gam.evolve();
			}
		} catch (InitialisationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
