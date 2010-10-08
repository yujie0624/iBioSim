package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
//import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import biomodelsim.BioSim;

public class AssignmentPanel extends JPanel implements ActionListener {

	private String selected = "", transition, id, oldName = null;

	private PropertyList assignmentList, continuousList, rateList, integerList, booleanList;

	private ArrayList<String> boolList, contList, intList;

	private String[] varList;

	private String[] options = { "Ok", "Cancel" };

	private LhpnFile lhpn;

	// private JComboBox typeBox, varBox;
	private JComboBox varBox;

	private JCheckBox rateBox;

	// private static final String[] types = { "boolean", "continuous", "rate"
	// };

	private HashMap<String, PropertyField> fields = null;

	// private boolean rate = false;

	public AssignmentPanel(String transition, String selected, PropertyList assignmentList,
			PropertyList continuousList, PropertyList rateList, PropertyList booleanList,
			PropertyList integerList, LhpnFile lhpn) {
		super(new GridLayout(3, 1));
		this.selected = selected;
		//this.transition = transition;
		this.assignmentList = assignmentList;
		this.continuousList = continuousList;
		this.rateList = rateList;
		this.booleanList = booleanList;
		this.integerList = integerList;

		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();

		contList = new ArrayList<String>();
		boolList = new ArrayList<String>();
		intList = new ArrayList<String>();
		String[] tempArray = lhpn.getContVars();
		String[] tempList = assignmentList.getItems();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!(selected.startsWith(tempArray[i] + ":=") || selected.startsWith(tempArray[i]
						+ "':="))) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")
								|| tempList[j].startsWith(tempArray[i] + "':=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")
							|| tempList[j].startsWith(tempArray[i] + "':=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				contList.add(tempArray[i]);
			}
		}
		tempArray = lhpn.getBooleanVars();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!selected.startsWith(tempArray[i] + ":=")) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				boolList.add(tempArray[i]);
			}
		}
		tempArray = lhpn.getIntVars();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!selected.startsWith(tempArray[i] + ":=")) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				intList.add(tempArray[i]);
			}
		}
		varList = new String[contList.size() + boolList.size() + intList.size()];
		for (int i = 0; i < boolList.size(); i++) {
			varList[i] = boolList.get(i);
		}
		for (int i = 0; i < contList.size(); i++) {
			varList[i + boolList.size()] = contList.get(i);
		}
		for (int i = 0; i < intList.size(); i++) {
			varList[i + boolList.size() + contList.size()] = intList.get(i);
		}

		// Variable field
		JPanel tempPanel = new JPanel();
		JLabel varLabel = new JLabel("Variable");
		varBox = new JComboBox(varList);
		varBox.setSelectedItem(varList[0]);
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);

		// Assignment lower bound
		PropertyField field = new PropertyField("Assignment", "", null, null,
				Utility.VALstring);
		fields.put("Assignment", field);
		add(field);

		// Rate Assignment Box
		rateBox = new JCheckBox("Rate Assignment");
		add(rateBox);
		if (!lhpn.isContinuous(varBox.getSelectedItem().toString())) {
			rateBox.setEnabled(false);
		}

		if (selected != null) {
			oldName = selected;
			tempArray = new String[2];
			if (oldName.matches("[\\S^']+':=[\\S]+")) {
				rateBox.setSelected(true);
				tempArray = oldName.split("':=");
			}
			else {
				tempArray = oldName.split(":=");
			}
			for (int i = 0; i < varList.length; i++) {
				if (varList[i].equals(tempArray[0])) {
					varBox.setSelectedItem(varList[i]);
					break;
				}
			}
			if (lhpn.isContinuous(varBox.getSelectedItem().toString())) {
				rateBox.setEnabled(true);
			}
			else {
				rateBox.setEnabled(false);
			}
			fields.get("Assignment").setValue(tempArray[1]);
			// loadProperties(prop);
		}

		// setType(types[0]);
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Variable Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				HashMap<String, String> prop;
				if (lhpn.getTransition(selected) == null) {
					prop = null;
				}
				else {
					prop = lhpn.getTransition(selected).getContAssignments();
				}
				if (prop != null) {
					if (prop.containsKey(varBox.getSelectedItem())) {
						Utility.createErrorMessage("Error", "Assignment id already exists.");
						return false;
					}
				}
			}
			if (!save()) {
				return false;
			}
			// else if
			// (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
			// if
			// (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue()))
			// {
			// Utility.createErrorMessage("Error", "Assignment id already
			// exists.");
			// return false;
			// }
			// }
			String assign = "";
			assign = fields.get("Assignment").getValue();
			if (rateBox.isSelected()) {
				id = varBox.getSelectedItem().toString() + "':=" + assign;
			}
			else {
				id = varBox.getSelectedItem().toString() + ":=" + assign;
			}

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				// System.out.println(f.getKey());
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put("Variable", varBox.getSelectedItem().toString());

			// if (selected != null && !oldName.equals(id)) {
			// lhpn.removeContAssign(selected, oldName);
			// }
			// else {
			// System.out.println(transition + " " + id + " " +
			// property.getProperty("Assignment value"));
			// lhpn.addContAssign(transition, id,
			// property.getProperty("Value"));
			// }
			assignmentList.removeItem(oldName);
			// System.out.println(id);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			String var = varBox.getSelectedItem().toString();
			if (rateBox.isSelected()) {
				if (lhpn.isContinuous(var)) {
					rateList.removeItem(oldName);
					rateList.addItem(id);
				}
				else {
					Utility.createErrorMessage("Error",
							"Rate assignments must be for continuous variables.");
					return false;
				}
			}
			else if (lhpn.isInput(var) || lhpn.isOutput(var)) {
				booleanList.removeItem(oldName);
				booleanList.addItem(id);
			}
			else if (lhpn.isContinuous(var)) {
				continuousList.removeItem(oldName);
				continuousList.addItem(id);
			}
			else if (lhpn.isInteger(var)) {
				integerList.removeItem(oldName);
				integerList.addItem(id);
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setID(varBox.getSelectedItem().toString());
			if (lhpn.isContinuous(varBox.getSelectedItem().toString())) {
				rateBox.setEnabled(true);
			}
			else {
				rateBox.setEnabled(false);
			}
		}
	}

	public boolean save() {
		String variable = varBox.getSelectedItem().toString();
		String value = "";
		ExprTree[] expr = new ExprTree[2];
		expr[0] = new ExprTree(lhpn);
		expr[1] = new ExprTree(lhpn);
			value = fields.get("Assignment").getValue();
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value))
					return false;
			}
		Properties property = new Properties();
		for (PropertyField f : fields.values()) {
			if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
				property.put(f.getKey(), f.getValue());
			}
		}
		property.put("Variable", variable);
		return true;
	}

}
