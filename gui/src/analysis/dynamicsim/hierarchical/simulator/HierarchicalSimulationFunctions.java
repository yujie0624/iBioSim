package analysis.dynamicsim.hierarchical.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.EventAssignment;

import analysis.dynamicsim.flattened.XORShiftRandom;
import analysis.dynamicsim.hierarchical.util.HierarchicalEventToFire;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

public abstract class HierarchicalSimulationFunctions extends HierarchicalSBasesSetup
{

	public HierarchicalSimulationFunctions(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running,
				interestingSpecies, quantityType, abstraction);

	}

	protected void setupVariableFromTSD() throws IOException
	{
		getBufferedTSDWriter().write("(" + "\"" + "time" + "\"");

		if (getInterestingSpecies().length > 0)
		{
			for (String s : getInterestingSpecies())
			{

				getBufferedTSDWriter().write(",\"" + s + "\"");

			}

			getBufferedTSDWriter().write("),\n");

			return;
		}

		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{
			getBufferedTSDWriter().write(",\"" + speciesID + "\"");
		}

		for (String noConstantParam : getTopmodel().getVariablesToPrint())
		{
			getBufferedTSDWriter().write(",\"" + noConstantParam + "\"");
		}
		/*
		 * for (String compartment : getTopmodel().compartmentIDSet) {
		 * bufferedTSDWriter.write(", \"" + compartment + "\""); }
		 */
		for (ModelState model : getSubmodels().values())
		{
			for (String speciesID : model.getSpeciesIDSet())
			{
				if (!model.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + speciesID + "\"");
				}
			}

			for (String noConstantParam : model.getVariablesToPrint())
			{
				if (!model.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + noConstantParam + "\"");
				}
			}
		}

		getBufferedTSDWriter().write("),\n");

	}

	protected void printAllToTSD(double printTime) throws IOException
	{
		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		// print the current time
		getBufferedTSDWriter().write(printTime + ",");

		// loop through the speciesIDs and print their current value to the file
		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{

			getBufferedTSDWriter().write(commaSpace + getTopmodel().getVariableToValue(getReplacements(), speciesID));
			commaSpace = ",";

		}

		for (String noConstantParam : getTopmodel().getVariablesToPrint())
		{

			getBufferedTSDWriter().write(commaSpace + getTopmodel().getVariableToValue(getReplacements(), noConstantParam));
			commaSpace = ",";

		}

		for (ModelState models : getSubmodels().values())
		{
			for (String speciesID : models.getSpeciesIDSet())
			{
				if (!models.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), speciesID));
					commaSpace = ",";
				}
			}

			for (String noConstantParam : models.getVariablesToPrint())
			{
				if (!models.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), noConstantParam));
					commaSpace = ",";
				}
			}
		}

		getBufferedTSDWriter().write(")");
		getBufferedTSDWriter().flush();
	}

	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun)
	{

		setCurrentRun(currentRun);

		setRandomNumberGenerator(new XORShiftRandom(randomSeed));

		try
		{

			String extension = ".tsd";

			setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + extension));
			setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
			getBufferedTSDWriter().write('(');

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void printInterestingToTSD(double printTime) throws IOException
	{

		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		// print the current time
		getBufferedTSDWriter().write(printTime + ",");

		double temp;
		// loop through the speciesIDs and print their current value to the file

		for (String s : getInterestingSpecies())
		{
			String id = s.replaceAll("__[\\w]+", "");
			String element = s.replace(id + "__", "");
			ModelState ms = (id.equals(s)) ? getTopmodel() : getSubmodels().get(id);
			if (getPrintConcentrationSpecies().contains(s))
			{
				temp = ms.getVariableToValue(getReplacements(), ms.getSpeciesToCompartmentNameMap().get(element));
				getBufferedTSDWriter().write(commaSpace + ms.getVariableToValue(getReplacements(), element) / temp);
				commaSpace = ",";
			}
			else
			{
				getBufferedTSDWriter().write(commaSpace + ms.getVariableToValue(getReplacements(), element));
				commaSpace = ",";
			}
		}

		getBufferedTSDWriter().write(")");
		getBufferedTSDWriter().flush();
	}

	/**
	 * Returns the total propensity of all model states.
	 */
	protected double getTotalPropensity()
	{
		double totalPropensity = 0;

		totalPropensity += getTopmodel().getPropensity();

		for (ModelState model : getSubmodels().values())
		{
			totalPropensity += model.getPropensity();
		}

		return totalPropensity;
	}

	protected boolean isEventTriggered(ModelState modelstate, double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{

		if (checkModelTriggerEvent(getTopmodel(), t, y, variableToIndexMap))
		{
			return true;
		}
		return false;
	}

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	protected void printToTSD(double printTime) throws IOException
	{
		if (getInterestingSpecies().length == 0)
		{
			printAllToTSD(printTime);
		}
		else
		{
			printInterestingToTSD(printTime);
		}

	}

	protected void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, int[] dims)
	{

		// these are sets of things that need to be re-evaluated or tested due
		// to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		// loop through the reaction's reactants and products and update their
		// amounts
		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{

			double stoichiometry = speciesAndStoichiometry.doub;

			String speciesID;
			if (dims == null)
			{
				speciesID = speciesAndStoichiometry.string;
			}
			else
			{
				speciesID = getIndexedSpeciesReference(modelstate, selectedReactionID, speciesAndStoichiometry.string, dims);
			}
			// this means the stoichiometry isn't constant, so look to the
			// variableToValue map
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(selectedReactionID))
			{

				for (HierarchicalStringPair doubleID : modelstate.getReactionToNonconstantStoichiometriesSetMap().get(selectedReactionID))
				{
					// string1 is the species ID; string2 is the
					// speciesReference ID
					if (doubleID.string1.equals(speciesID))
					{

						stoichiometry = modelstate.getVariableToValue(getReplacements(), doubleID.string2);

						// this is to get the plus/minus correct, as the
						// variableToValueMap has
						// a stoichiometry without the reactant/product
						// plus/minus data
						stoichiometry *= (int) (speciesAndStoichiometry.doub / Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			// update the species count if the species isn't a boundary
			// condition or constant
			// note that the stoichiometries are earlier modified with the
			// correct +/- sign
			boolean cond1 = modelstate.getSpeciesToIsBoundaryConditionMap().get(speciesID);
			boolean cond2 = modelstate.getVariableToIsConstantMap().get(speciesID);
			if (!cond1 && !cond2)
			{

				double val = modelstate.getVariableToValue(getReplacements(), speciesID) + stoichiometry;
				if (val >= 0)
				{
					modelstate.setvariableToValueMap(getReplacements(), speciesID, val);
				}

			}

			// if this variable that was just updated is part of an assignment
			// rule (RHS)
			// then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID) == true)
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID));
			}

			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(speciesID) == true)
			{
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(speciesID));
			}
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(modelstate, affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0)
		{
			setConstraintFlag(testConstraints(modelstate, affectedConstraintSet));
		}

	}

	protected static HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, boolean noAssignmentRulesFlag)
	{

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);

		// loop through the reaction's reactants and products
		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID));

			// if the species is involved in an assignment rule then it its
			// changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID))
			{

				// this assignment rule is going to be evaluated, so the rule's
				// variable's value will change
				for (AssignmentRule assignmentRule : modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID))
				{
					if (modelstate.getSpeciesToAffectedReactionSetMap().get(assignmentRule.getVariable()) != null)
					{
						affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(assignmentRule.getVariable()));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	// protected void updateRules()
	// {
	// HashSet<AssignmentRule> affectedAssignmentRuleSet = new
	// HashSet<AssignmentRule>();
	// HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();
	//
	// for (ModelState model : getSubmodels().values())
	// {
	// for (String element : model.getIsHierarchical())
	// {
	// if (model.isNoRuleFlag() == false
	// && model.getVariableToIsInAssignmentRuleMap().get(element) == true)
	// {
	// affectedAssignmentRuleSet.addAll(model
	// .getVariableToAffectedAssignmentRuleSetMap().get(element));
	// }
	// if (affectedAssignmentRuleSet.size() > 0)
	// {
	// performAssignmentRules(model, affectedAssignmentRuleSet);
	// }
	// if (model.isNoConstraintsFlag() == false
	// && model.getVariableToIsInConstraintMap().get(element) == true)
	// {
	// affectedConstraintSet.addAll(model.getVariableToAffectedConstraintSetMap().get(
	// element));
	// }
	// if (affectedConstraintSet.size() > 0)
	// {
	// setConstraintFlag(testConstraints(model, affectedConstraintSet));
	// }
	// }
	// }
	// }

	protected double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (getTopmodel().isNoEventsFlag() == false)
		{
			handleEvents(getTopmodel());
			if (!getTopmodel().getTriggeredEventQueue().isEmpty() && getTopmodel().getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
			{
				if (getTopmodel().getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
				{
					nextEventTime = getTopmodel().getTriggeredEventQueue().peek().getFireTime();
				}
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (models.isNoEventsFlag() == false)
			{
				handleEvents(models);
				if (!models.getTriggeredEventQueue().isEmpty() && models.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
				{
					if (models.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
					{
						nextEventTime = models.getTriggeredEventQueue().peek().getFireTime();
					}
				}
			}
		}
		return nextEventTime;
	}

	protected boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet)
	{
		for (ASTNode constraint : affectedConstraintSet)
		{
			if (HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, constraint, false, getCurrentTime(), null, null)))
			{
				return true;
			}
		}

		return false;
	}

	protected void handleEvents(ModelState modelstate)
	{

		HashSet<String> triggeredEvents = new HashSet<String>();

		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			boolean eventTrigger = HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate
					.getEventToTriggerMap().get(untriggeredEventID), false, getCurrentTime(), null, null));

			if (eventTrigger)
			{

				if (getCurrentTime() == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID) == true)
				{
					continue;
				}

				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID) == true)
				{
					continue;
				}

				triggeredEvents.add(untriggeredEventID);

				if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(untriggeredEventID))
				{
					handleEventsValueAtTrigger(modelstate, untriggeredEventID);
				}
				else
				{
					handleEventsValueAtFire(modelstate, untriggeredEventID);
				}
			}
			else
			{

				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}

		modelstate.getUntriggeredEventSet().removeAll(triggeredEvents);
	}

	private void handleEventsValueAtTrigger(ModelState modelstate, String untriggeredEventID)
	{
		HashSet<Object> evaluatedAssignments = new HashSet<Object>();

		for (Object evAssignment : modelstate.getEventToAssignmentSetMap().get(untriggeredEventID))
		{

			EventAssignment eventAssignment = (EventAssignment) evAssignment;
			evaluatedAssignments.add(new HierarchicalStringDoublePair(eventAssignment.getVariable(), evaluateExpressionRecursive(modelstate,
					eventAssignment.getMath(), false, getCurrentTime(), null, null)));
		}

		double fireTime = getCurrentTime();

		if (modelstate.getEventToHasDelayMap().get(untriggeredEventID))
		{
			fireTime += evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false, getCurrentTime(),
					null, null);
		}

		modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, evaluatedAssignments, fireTime));
	}

	private void handleEventsValueAtFire(ModelState modelstate, String untriggeredEventID)
	{
		double fireTime = getCurrentTime();

		if (modelstate.getEventToHasDelayMap().get(untriggeredEventID) == true)
		{
			fireTime += evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false, getCurrentTime(),
					null, null);
		}

		modelstate.getTriggeredEventQueue().add(
				new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, modelstate.getEventToAssignmentSetMap().get(untriggeredEventID),
						fireTime));
	}

	protected void updatePreviosTriggerEvent(ModelState modelstate, double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{

		if (!modelstate.isNoEventsFlag())
		{

			for (String event : modelstate.getUntriggeredEventSet())
			{
				double result = evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), true, t, y, variableToIndexMap);

				modelstate.getEventToPreviousTriggerValueMap().put(event, result > 0);

			}
		}
	}

	protected boolean isEventTriggered(ModelState modelstate, String event, double t, double[] y, Map<String, Integer> variableToIndexMap,
			boolean checkPrevious)
	{
		double result = evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), true, t, y, variableToIndexMap);
		if (result > 0)
		{
			if (!checkPrevious)
			{
				return true;
			}
			else if (!modelstate.getEventToPreviousTriggerValueMap().get(event))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean checkModelTriggerEvent(ModelState modelstate, double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{

		if (modelstate.isNoEventsFlag())
		{
			return false;
		}

		for (String event : modelstate.getUntriggeredEventSet())
		{

			if (isEventTriggered(modelstate, event, t, y, variableToIndexMap, true))
			{
				return true;
			}
		}

		// if (modelstate.getTriggeredEventQueue().peek() != null
		// && modelstate.getTriggeredEventQueue().peek().getFireTime() <= t)
		// {
		// return true;
		// }

		return false;
	}

	private void checkTriggeredEvents(ModelState modelstate, Set<String> untriggeredEvents)
	{
		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{
			String triggeredEventID = triggeredEvent.getEventID();

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							modelstate.getEventToTriggerMap().get(triggeredEventID), false, getCurrentTime(), null, null)) == false)
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true
					&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							modelstate.getEventToTriggerMap().get(triggeredEventID), false, getCurrentTime(), null, null)) == false)
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	private PriorityQueue<HierarchicalEventToFire> constructPriorityQueue(ModelState modelstate, Set<String> untriggeredEvents)
	{
		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int) modelstate.getNumEvents(),
				modelstate.getEventComparator());

		while (modelstate.getTriggeredEventQueue().size() > 0)
		{

			HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

			HierarchicalEventToFire eventToAdd = new HierarchicalEventToFire(modelstate.getID(), event.getEventID(), (HashSet<Object>) event
					.getEventAssignmentSet().clone(), event.getFireTime());

			if (!untriggeredEvents.contains(event.getEventID()))
			{
				newTriggeredEventQueue.add(eventToAdd);
			}
			else
			{
				modelstate.getUntriggeredEventSet().add(event.getEventID());
			}
		}

		return newTriggeredEventQueue;

	}

	private void updatePreviousTriggerValue(ModelState modelstate)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			if (modelstate.getEventToTriggerPersistenceMap().get(untriggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							modelstate.getEventToTriggerMap().get(untriggeredEventID), false, getCurrentTime(), null, null)) == false)
			{
				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}
	}

	private void fireSingleEvent(ModelState modelstate, Set<AssignmentRule> affectedAssignmentRuleSet, Set<ASTNode> affectedConstraintSet,
			Set<String> affectedReactionSet, Set<String> variableInFiredEvents, Set<String> untriggeredEvents, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag)
	{

		HierarchicalEventToFire eventToFire = modelstate.getTriggeredEventQueue().poll();
		String eventToFireID = eventToFire.getEventID();

		if (modelstate.getEventToAffectedReactionSetMap().get(eventToFireID) != null)
		{
			affectedReactionSet.addAll(modelstate.getEventToAffectedReactionSetMap().get(eventToFireID));
		}

		modelstate.getUntriggeredEventSet().add(eventToFireID);
		modelstate.addEventToPreviousTriggerValueMap(
				eventToFireID,
				HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
						modelstate.getEventToTriggerMap().get(eventToFireID), false, getCurrentTime(), null, null)));

		Map<String, Double> assignments = new HashMap<String, Double>();

		for (Object eventAssignment : eventToFire.getEventAssignmentSet())
		{

			String variable;
			double assignmentValue;

			if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(eventToFireID) == true)
			{
				variable = ((HierarchicalStringDoublePair) eventAssignment).string;
				assignmentValue = ((HierarchicalStringDoublePair) eventAssignment).doub;
			}
			else
			{
				variable = ((EventAssignment) eventAssignment).getVariable();
				assignmentValue = evaluateExpressionRecursive(modelstate, ((EventAssignment) eventAssignment).getMath(), false, getCurrentTime(),
						null, null);
			}

			variableInFiredEvents.add(variable);

			if (modelstate.getVariableToIsConstantMap().get(variable) == false)
			{
				assignments.put(variable, assignmentValue);
			}

			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(variable) == true)
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(variable));
			}
			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(variable) == true)
			{
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(variable));
			}

		}

		// Perform event assignments
		for (String id : assignments.keySet())
		{
			modelstate.setvariableToValueMap(getReplacements(), id, assignments.get(id));
		}

		untriggeredEvents.clear();

		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{

			String triggeredEventID = triggeredEvent.getEventID();

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							modelstate.getEventToTriggerMap().get(triggeredEventID), false, getCurrentTime(), null, null)) == false)
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true
					&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							modelstate.getEventToTriggerMap().get(triggeredEventID), false, getCurrentTime(), null, null)) == false)
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}

		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int) modelstate.getNumEvents(),
				modelstate.getEventComparator());

		while (modelstate.getTriggeredEventQueue().size() > 0)
		{

			HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

			HierarchicalEventToFire eventToAdd = new HierarchicalEventToFire(modelstate.getID(), event.getEventID(), (HashSet<Object>) event
					.getEventAssignmentSet().clone(), event.getFireTime());

			if (!untriggeredEvents.contains(event.getEventID()))
			{
				newTriggeredEventQueue.add(eventToAdd);
			}
			else
			{
				modelstate.getUntriggeredEventSet().add(event.getEventID());
			}
		}

		modelstate.setTriggeredEventQueue(newTriggeredEventQueue);
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(ModelState modelstate, String selector, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag)
	{

		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		checkTriggeredEvents(modelstate, untriggeredEvents);

		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = constructPriorityQueue(modelstate, untriggeredEvents);

		modelstate.setTriggeredEventQueue(newTriggeredEventQueue);

		updatePreviousTriggerValue(modelstate);

		while (modelstate.getTriggeredEventQueue().size() > 0 && modelstate.getTriggeredEventQueue().peek().getFireTime() <= getCurrentTime())
		{

			fireSingleEvent(modelstate, affectedAssignmentRuleSet, affectedConstraintSet, affectedReactionSet, variableInFiredEvents,
					untriggeredEvents, noAssignmentRulesFlag, noConstraintsFlag);
			handleEvents(modelstate);
		}

		// modelstate.getUntriggeredEventSet().addAll(firedEvents);

		if (selector.equals("variable"))
		{
			return variableInFiredEvents;
		}

		return affectedReactionSet;
	}

}