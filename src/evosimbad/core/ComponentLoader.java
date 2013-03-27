/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge
 */
public class ComponentLoader {

    public static String NULL_STRING = "null";
    private ExtraOptions extraOptions;
    private EnvironmentGenerator environmentGen;
    private AgentGenerator agentGen;
    private AgentPlacer agentPlacer;
    private Pair<Constructor, Object[]> evaluationConstructor;
    private SimulationBuilder simulationBuilder;
    private Simulator sim;
    private EvolutionMethod evolutionMethod;
    private Logger logger;
    private String config;
    private Date date;

    public ComponentLoader(String config) throws Exception {
        this.config = config;
        this.date = Calendar.getInstance().getTime();
        this.extraOptions = new ExtraOptions();
        createSingletons();
    }

    public void init() {
        logger.setMain(this);
        evolutionMethod.setupEvolution(this);
        simulationBuilder.setEnvGenerator(environmentGen);
        simulationBuilder.setAgentGenerator(agentGen);
        simulationBuilder.setAgentPlacer(agentPlacer);
        simulationBuilder.setEvaluationFunction(
                evaluationConstructor.getLeft().getDeclaringClass(),
                evaluationConstructor.getLeft().getParameterTypes(),
                evaluationConstructor.getRight());
        sim.setSimulationBuilder(simulationBuilder);
    }

    public String getConfig() {
        return config;
    }

    public Date getCreationDate() {
        return date;
    }

    private void createSingletons() throws Exception {
        String[] lines = config.split("\n");
        Pattern generalPattern = Pattern.compile("\\s*\\w+\\s*=\\s*.+\\s*");
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("#") || l.equals("")) {
                continue;
            }
            if (!generalPattern.matcher(l).find()) {
                throw new Exception("Invalid configuration line: " + i);
            }

            int index = l.indexOf("=");
            String option = l.substring(0, index).trim();
            String rest = l.substring(index + 1, l.length()).trim();

            switch (option) {
                case "simulationBuilder":
                    simulationBuilder = (SimulationBuilder) createInstance(SimulationBuilder.class, rest);
                    break;
                case "environment":
                    environmentGen = (EnvironmentGenerator) createInstance(EnvironmentGenerator.class, rest);
                    break;
                case "agents":
                    agentGen = (AgentGenerator) createInstance(AgentGenerator.class, rest);
                    break;
                case "agentPlacer":
                    agentPlacer = (AgentPlacer) createInstance(AgentPlacer.class, rest);
                    break;
                case "evolution":
                    evolutionMethod = (EvolutionMethod) createInstance(EvolutionMethod.class, rest);
                    break;
                case "simulator":
                    sim = (Simulator) createInstance(Simulator.class, rest);
                    break;
                case "evaluation":
                    evaluationConstructor = findConstructor(EvaluationFunction.class, rest);
                    evaluationConstructor.getLeft().newInstance(evaluationConstructor.getRight()); // test the creation
                    break;
                case "logger":
                    logger = (Logger) createInstance(Logger.class, rest);
                    break;
                default:
                    extraOptions.put(option, rest);
            }
        }

        if (environmentGen == null || agentGen == null || agentPlacer == null
                || evolutionMethod == null || sim == null
                || evaluationConstructor == null || logger == null
                || simulationBuilder == null) {
            throw new Exception("Missing configuration(s)");
        }
    }

    public AgentGenerator getAgentGen() {
        return agentGen;
    }

    public AgentPlacer getAgentPlacer() {
        return agentPlacer;
    }

    public EnvironmentGenerator getEnvironmentGen() {
        return environmentGen;
    }

    public EvolutionMethod getEvolutionMethod() {
        return evolutionMethod;
    }

    public Simulator getSimulator() {
        return sim;
    }

    public Logger getLogger() {
        return logger;
    }

    public SimulationBuilder getSimulationBuilder() {
        return simulationBuilder;
    }

    public ExtraOptions getExtraOptions() {
        return extraOptions;
    }

    public static Object createInstance(Class superClass, String pars) throws Exception {
        Pair<Constructor, Object[]> constr = findConstructor(superClass, pars);
        return constr.getLeft().newInstance(constr.getRight());
    }

    public static Pair<Constructor, Object[]> findConstructor(Class superClass, String pars) throws Exception {
        int index = pars.indexOf("(");
        String className = pars.substring(0, index).trim();
        String params = pars.substring(index + 1, pars.length() - 1).trim();

        Class c = Class.forName(className);
        if (!superClass.isAssignableFrom(c)) {
            throw new Exception("Invalid class: " + c.getCanonicalName());
        }

        // http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
        String[] splitParams = params.length() == 0 ? new String[]{} : params.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < splitParams.length; i++) {
            splitParams[i] = splitParams[i].replace("\"", "").trim();
        }

        Constructor[] constructors = c.getConstructors();
        for (Constructor constr : constructors) {
            Class[] parameterTypes = constr.getParameterTypes();
            if (parameterTypes.length == splitParams.length) {
                try {
                    Object[] args = parseArguments(parameterTypes, splitParams);
                    return Pair.of(constr, args);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Caso haja algum problema na conversao dos parametros
                    System.out.println("Problem with constructor: " + e.getMessage());
                }
            }
        }
        throw new Exception("No adequate construtor found");
    }

    private static Object[] parseArguments(Class[] types, String[] params) throws Exception {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class type = types[i];
            String p = params[i];
            if (p.equalsIgnoreCase(NULL_STRING)) {
                args[i] = null;
            } else if (type.equals(byte.class)) {
                args[i] = (Byte.parseByte(p));
            } else if (type.equals(short.class)) {
                args[i] = Short.parseShort(p);
            } else if (type.equals(int.class)) {
                args[i] = Integer.parseInt(p);
            } else if (type.equals(long.class)) {
                args[i] = Long.parseLong(p);
            } else if (type.equals(float.class)) {
                args[i] = Float.parseFloat(p);
            } else if (type.equals(double.class)) {
                args[i] = Double.parseDouble(p);
            } else if (type.equals(boolean.class)) {
                args[i] = Boolean.parseBoolean(p);
            } else if (type.equals(char.class)) {
                if (p.length() != 1) {
                    throw new Exception("Not assignable to char");
                }
                args[i] = p.charAt(0);
            } else if (type.equals(String.class)) {
                args[i] = p;
            } else {
                throw new Exception("Argument not a primitive type");
            }
        }
        return args;
    }
}
