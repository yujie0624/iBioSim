package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SpeciesReference;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import main.Gui;


public class InfluencePanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InfluencePanel(String selection, PropertyList list, BioModel gcm, boolean paramsOnly, BioModel refGCM, ModelEditor gcmEditor) {
		super(new GridLayout(5, 1));
		this.selection = selection;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;
		this.promoterNameChange = false;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = null; /*= new PropertyField(GlobalConstants.NAME, "", null,
				null, "(.*)", paramsOnly, "default", false);
		field.setEnabled(false);
		fields.put(GlobalConstants.NAME, field);
		add(field);
	    */
		
		// Variables used repeatedly for different parts of the UI.
		JPanel tempPanel;
		JLabel tempLabel;
		
		// Promoter field. Disabled if the influences is connected to an explicit promoter.
		tempPanel = new JPanel();
		tempLabel = new JLabel("Promoter");
		promoterButton = new JButton("Edit Promoter");
		promoterButton.setActionCommand("buttonPushed");
		promoterButton.addActionListener(this);
		promoterId = gcm.influenceHasExplicitPromoter(selection);
		if (promoterId != null) {
			promoterBox = new JComboBox(gcm.getPromoters().toArray());
			promoterBox.setSelectedItem(promoterId);
			if (selection.contains("x>")) {
				regulator = selection.substring(0,selection.indexOf(">")-1);
			} else {
				regulator = selection.substring(0,selection.indexOf("-"));
			}
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
		} else {
			//If no implicit promoters exist (e.g. there are only complex influences), 
			//creates one to fall back on if influence type is changed to activation or repression
			// commented this out since we can't change the type of complex influence anyway and 
			// the potentially hidden implicit promoter can cause problems when saving SBOL 
//			String[] impProms = gcm.getImplicitPromotersAsArray();
//			if (impProms.length == 0) {
//				gcm.createPromoter(null, 0, 0, false);
//				impProms = gcm.getImplicitPromotersAsArray();
//			}
			promoterBox = new JComboBox(gcm.getImplicitPromotersAsArray());
			if (selection.contains(",")) {
				promoterId = selection.substring(selection.indexOf(",")+1);
				if (selection.contains("x>")) {
					regulator = selection.substring(0,selection.indexOf(">")-1);
					product = selection.substring(selection.indexOf(">")+1,selection.indexOf(","));
				} else {
					regulator = selection.substring(0,selection.indexOf("-"));
					product = selection.substring(selection.indexOf("-")+2,selection.indexOf(","));
				}
				promoterBox.setSelectedItem(promoterId);
				promoterBox.setEnabled(true);
				promoterButton.setEnabled(true);
			} else {
				regulator = selection.substring(0,selection.indexOf("+"));
				product = selection.substring(selection.indexOf(">")+1,selection.length());
				promoterBox.setEnabled(false);
				promoterButton.setEnabled(false);
			}
		}
		//System.out.println("selection=" + selection);
		//System.out.println("regulator=" + regulator);
		//System.out.println("product=" + product);
		//System.out.println("promotorId=" + promoterId);
		promoterBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 3));
		tempPanel.add(tempLabel);
		tempPanel.add(promoterBox);
		tempPanel.add(promoterButton);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			promoterBox.setEnabled(false);
		}
		add(tempPanel);

		// Type field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Type");
		if(promoterId != null)
			typeBox = new JComboBox(explicitPromoterTypes);
		else
			typeBox = new JComboBox(types);
		tempPanel.setLayout(new GridLayout(1, 3));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		tempPanel.add(new JLabel());
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		add(tempPanel);
		// Production is never a selectable type
		//((DefaultComboBoxModel) (typeBox.getModel())).removeElement(types[4]);
		production = null;
		if (promoterId != null) {
			production = gcm.getProductionReaction(promoterId);
			ModifierSpeciesReference modifier = production.getModifier(regulator);
			if (BioModel.isRepressor(modifier)) {
				typeBox.setSelectedItem(GlobalConstants.REPRESSION);
			} else if (BioModel.isActivator(modifier)) {
				typeBox.setSelectedItem(GlobalConstants.ACTIVATION);
			} else if (selection.contains("x>")) {
				typeBox.setSelectedItem(GlobalConstants.NOINFLUENCE);
			}
			if (BioModel.isRegulator(modifier)) typeBox.setEnabled(false);
			else if (!paramsOnly) typeBox.setEnabled(true);
		} else {
			production = gcm.getComplexReaction(product);
			typeBox.setSelectedItem(GlobalConstants.COMPLEX);
			typeBox.setEnabled(false);
		}
		
		// coop
		String defString = "default";
		String defaultValue = gcm.getParameter(GlobalConstants.COOPERATIVITY_STRING);
		String formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter nc = null;
				if (typeBox.getSelectedItem().equals(GlobalConstants.COMPLEX)) {
					nc = refProd.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator);
				} else if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) { 
					nc = refProd.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_r");
				} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) { 
					nc = refProd.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_a");
				} 
				if (nc != null) {
					defaultValue = ""+nc.getValue();
					defString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, 
				gcm.getParameter(GlobalConstants.COOPERATIVITY_STRING),
				defString, defaultValue, formatString, paramsOnly, defString, false);
		LocalParameter nc = null;
		if (typeBox.getSelectedItem().equals(GlobalConstants.COMPLEX)) {
			nc = production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator);
		} else if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) { 
			nc = production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_r");
		} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) { 
			nc = production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_a");
		} else {
			field.setEnabled(false);
		}
		if (nc != null && nc.isSetAnnotation() && 
				nc.getAnnotationString().contains(GlobalConstants.COOPERATIVITY_STRING)) {
			String sweep = nc.getAnnotationString().replace("<annotation>"+GlobalConstants.COOPERATIVITY_STRING+"=","")
					.replace("</annotation>","");
			field.setValue(sweep);
			field.setCustom();
		} else if (nc != null && !defaultValue.equals(""+nc.getValue())) {
			field.setValue(""+nc.getValue());
			field.setCustom();
		}
		fields.put(GlobalConstants.COOPERATIVITY_STRING, field);
		add(field);

		// krep
		defString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.FORWARD_KREP_STRING) + "/" + 
				gcm.getParameter(GlobalConstants.REVERSE_KREP_STRING);
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter kr_f = null;
				LocalParameter kr_r = null;
				if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) { 
					kr_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + regulator + "_"));
					kr_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + regulator + "_"));
				} 
				if (kr_f != null) {
					defaultValue = kr_f.getValue()+"/"+kr_r.getValue();
					defString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.KREP_STRING, gcm.getParameter(GlobalConstants.KREP_STRING),
				defString, defaultValue, formatString, paramsOnly, defString, false);
		if (!typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) { 
			field.setEnabled(false);
		}
		LocalParameter kr_f = null;
		LocalParameter kr_r = null;
		if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) { 
			kr_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + regulator + "_"));
			kr_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + regulator + "_"));
		} else {
			field.setEnabled(false);
		}
		if (kr_f != null && kr_f.isSetAnnotation() && 
				kr_f.getAnnotationString().contains(GlobalConstants.FORWARD_KREP_STRING)) {
			String sweep = kr_f.getAnnotationString().replace("<annotation>"+GlobalConstants.FORWARD_KREP_STRING+"=","")
					.replace("</annotation>","");
			field.setValue(sweep);
			field.setCustom();
		} else if (kr_f != null && !defaultValue.equals(kr_f.getValue()+"/"+kr_r.getValue())) {
			field.setValue(kr_f.getValue()+"/"+kr_r.getValue());
			field.setCustom();
		}
		fields.put(GlobalConstants.KREP_STRING, field);
		add(field);

		// kact
		defString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.FORWARD_KACT_STRING) + "/" + 
				gcm.getParameter(GlobalConstants.REVERSE_KACT_STRING);
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ka_f = null;
				LocalParameter ka_r = null;
				if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) { 
					ka_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + regulator + "_"));
					ka_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + regulator + "_"));
				} 
				if (ka_f != null) {
					defaultValue = ka_f.getValue()+"/"+ka_r.getValue();
					defString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.KACT_STRING, gcm.getParameter(GlobalConstants.KACT_STRING),
				defString, defaultValue, formatString, paramsOnly, defString, false);
		if (!typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) { 
			field.setEnabled(false);
		}
		LocalParameter ka_f = null;
		LocalParameter ka_r = null;
		if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) { 
			ka_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + regulator + "_"));
			ka_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + regulator + "_"));
		} else {
			field.setEnabled(false);
		}
		if (ka_f != null && ka_f.isSetAnnotation() && 
				ka_f.getAnnotationString().contains(GlobalConstants.FORWARD_KACT_STRING)) {
			String sweep = ka_f.getAnnotationString().replace("<annotation>"+GlobalConstants.FORWARD_KACT_STRING+"=","")
					.replace("</annotation>","");
			field.setValue(sweep);
			field.setCustom();
		} else if (ka_f != null && !defaultValue.equals(ka_f.getValue()+"/"+ka_r.getValue())) {
			field.setValue(ka_f.getValue()+"/"+ka_r.getValue());
			field.setCustom();
		}
		fields.put(GlobalConstants.KACT_STRING, field);
		add(field);
		typeBox.addActionListener(this);
		
		boolean display = false;
		while (!display) {
			display = openGui();
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

	private boolean openGui() {
		int value = JOptionPane.showOptionDialog(Gui.frame, this,
				"Influence Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}

			if (promoterId==null) {
				production.getKineticLaw().removeLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator);
			} else if (selection.contains("->")){
				production.getKineticLaw().removeLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_a");
			} else if (selection.contains("-|")){
				production.getKineticLaw().removeLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + regulator + "_r");
			}
			if (selection.contains("-|")){
				production.getKineticLaw().removeLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + regulator + "_"));
				production.getKineticLaw().removeLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + regulator + "_"));
			}
			if (selection.contains("->")){
				production.getKineticLaw().removeLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + regulator + "_"));
				production.getKineticLaw().removeLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + regulator + "_"));
			}

			if (promoterId != null && !promoterBox.getSelectedItem().equals(promoterId)) {
				//production.removeModifier(regulator);
				//gcm.createProductionKineticLaw(production);
				gcm.removeInfluence(selection);
				production = gcm.getProductionReaction(""+promoterBox.getSelectedItem());
				SpeciesReference productSpecies = production.getProduct(product);
				if (productSpecies==null) {
					productSpecies = production.createProduct();
					productSpecies.setSpecies(product);
					productSpecies.setStoichiometry(1.0);
					productSpecies.setConstant(true);
				}
				ModifierSpeciesReference modifier = production.getModifier(regulator);
				if (modifier==null) {
					modifier = production.createModifier();
					modifier.setSpecies(regulator);
					if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) {
						modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
					} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) {
						modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
					} else if (typeBox.getSelectedItem().equals(GlobalConstants.NOINFLUENCE)) {
						modifier.setAnnotation(GlobalConstants.NOINFLUENCE);
					}
				} else {
					if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION) && 
							modifier.getAnnotationString().contains(GlobalConstants.ACTIVATION)) {
						modifier.setSBOTerm(GlobalConstants.SBO_REGULATION);
					} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION) && 
								BioModel.isRepressor(modifier)) {
							modifier.setSBOTerm(GlobalConstants.SBO_REGULATION);
					}
				}
			}
			
			if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION) &&
					(BioModel.isActivator(production.getModifier(regulator)) ||
							production.getModifier(regulator).getAnnotationString().contains(GlobalConstants.NOINFLUENCE))) {
				production.getModifier(regulator).setSBOTerm(GlobalConstants.SBO_REPRESSION);
			} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION) &&
					(BioModel.isRepressor(production.getModifier(regulator)) ||
					production.getModifier(regulator).getAnnotationString().contains(GlobalConstants.NOINFLUENCE))) {
				production.getModifier(regulator).setSBOTerm(GlobalConstants.SBO_ACTIVATION);
			} else if (typeBox.getSelectedItem().equals(GlobalConstants.NOINFLUENCE)) {
				production.getModifier(regulator).setAnnotation(GlobalConstants.NOINFLUENCE);
			}
			PropertyField f = fields.get(GlobalConstants.COOPERATIVITY_STRING);
			String CoopStr = null;
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) {
					LocalParameter nc = production.getKineticLaw().createLocalParameter();
					nc.setId(GlobalConstants.COOPERATIVITY_STRING +  "_" + regulator + "_r");
					if (f.getValue().startsWith("(")) {
						nc.setAnnotation(GlobalConstants.COOPERATIVITY_STRING + "=" + f.getValue());
						nc.setValue(1.0);
					} else {
						nc.setValue(Double.parseDouble(f.getValue()));
					}
				} else if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) {
					LocalParameter nc = production.getKineticLaw().createLocalParameter();
					nc.setId(GlobalConstants.COOPERATIVITY_STRING +  "_" + regulator + "_a");
					if (f.getValue().startsWith("(")) {
						nc.setAnnotation(GlobalConstants.COOPERATIVITY_STRING + "=" + f.getValue());
						nc.setValue(1.0);
					} else {
						nc.setValue(Double.parseDouble(f.getValue()));
					}
				} else if (typeBox.getSelectedItem().equals(GlobalConstants.COMPLEX)) {
					CoopStr = f.getValue();
				}
			} 
			f = fields.get(GlobalConstants.KREP_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				if (typeBox.getSelectedItem().equals(GlobalConstants.REPRESSION)) {
					double [] Kr;
					LocalParameter p = production.getKineticLaw().createLocalParameter();
					if (f.getValue().startsWith("(")) {
						Kr = Utility.getEquilibrium("1.0/1.0");
						p.setAnnotation(GlobalConstants.FORWARD_KREP_STRING+"="+f.getValue());
					} else {
						Kr = Utility.getEquilibrium(f.getValue());
					}
					p.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + regulator + "_"));
					p.setValue(Kr[0]);
					p = production.getKineticLaw().createLocalParameter();
					p.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + regulator + "_"));
					p.setValue(Kr[1]);
				} 
			} 
			f = fields.get(GlobalConstants.KACT_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				if (typeBox.getSelectedItem().equals(GlobalConstants.ACTIVATION)) {
					double [] Ka;
					LocalParameter p = production.getKineticLaw().createLocalParameter();
					if (f.getValue().startsWith("(")) {
						Ka = Utility.getEquilibrium("1.0/1.0");
						p.setAnnotation(GlobalConstants.FORWARD_KACT_STRING+"="+f.getValue());
					} else {
						Ka = Utility.getEquilibrium(f.getValue());
					}
					p.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + regulator + "_"));
					p.setValue(Ka[0]);
					p = production.getKineticLaw().createLocalParameter();
					p.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + regulator + "_"));
					p.setValue(Ka[1]);
				} 
			} 
			if (promoterId != null) {
				gcm.createProductionKineticLaw(production);
			} else {
				String KcStr = null;
				if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING)!=null &&
						production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING)!=null) {
					KcStr = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue() + "/" +
							production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue();
				}
				gcm.addReactantToComplexReaction(regulator, product, KcStr, CoopStr);
			}
			gcmEditor.setDirty(true);
		} else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.COOPERATIVITY_STRING).getState().equals(fields.get(GlobalConstants.COOPERATIVITY_STRING).getStates()[1])) {
				updates += "\"" + selection + "\"/"
						+ GlobalConstants.COOPERATIVITY_STRING + " "
						+ fields.get(GlobalConstants.COOPERATIVITY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KREP_STRING).getState().equals(fields.get(GlobalConstants.KREP_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + selection + "\"/"
				+ GlobalConstants.KREP_STRING + " "
				+ fields.get(GlobalConstants.KREP_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KACT_STRING).getState().equals(fields.get(GlobalConstants.KACT_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + selection + "\"/"
				+ GlobalConstants.KACT_STRING + " "
				+ fields.get(GlobalConstants.KACT_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += "\"" + selection + "\"/";
			}
		}
		return updates;
	}

	/**
	 * Builds the influence name
	 * @param sourceId
	 * @param targetId
	 * @param type
	 * @param isBio: has to be "yes" or "no"
	 * @param promoter
	 * @return
	 */
	public static String buildName(String sourceId, String targetId, String type, String promoter){
		String arrow = " -| ";
		if (type==types[1]) {
			arrow = " -> ";
		} else if (type==types[2]) {
			arrow = " x> ";
		} else if (type==types[3]) {
			arrow = " +> ";
		} 
		
		String out = sourceId + arrow + targetId + ", Promoter " + promoter;
		
		return out;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(typeBox)) {
			String type = typeBox.getSelectedItem().toString();
			setType(type);
			/* 
			String arrow = "-|";
			if (type==types[1]) {
				arrow = "->";
			} else if (type==types[2]) {
				arrow = "x>";
			} else if (type==types[3]) {
				arrow = "+>";
			} 
			String newName = fields.get(GlobalConstants.NAME).getValue().replaceAll(".[>|]", arrow);
			fields.get(GlobalConstants.NAME).setValue(newName);
			 */
		} else if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(promoterBox)) {
			/* 
			String oldName = fields.get(GlobalConstants.NAME).getValue();
			String newName = oldName.split("Promoter")[0] + "Promoter " + promoterBox.getSelectedItem().toString();
			fields.get(GlobalConstants.NAME).setValue(newName);
			if (promoterNameChange && oldName.equals(selection)) {
				selection = newName;
			}
			*/
			promoterNameChange = false;
		} else if (e.getActionCommand().equals("buttonPushed") && e.getSource().equals(promoterButton)) {
			PromoterPanel promPan = gcmEditor.launchPromoterPanel(promoterBox.getSelectedItem().toString());
			//Updates panel's selected promoter (and influence name) if promoter was renamed during user edit
			promoterNameChange = promPan.wasPromoterNameChanged();
			if (promoterNameChange) {
				String newPromoterName = promPan.getLastUsedPromoter();
				String oldPromoterName = promPan.getSecondToLastUsedPromoter();
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement(newPromoterName);
				//This will generate the action command associated with changing the promoter combo box
				promoterBox.setSelectedItem(newPromoterName);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement(oldPromoterName);
			}
		}
	}

	private void setType(String type) {
		if (type.equals(GlobalConstants.REPRESSION)) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(true);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			/*
			if (promoterSetToNone) {
				promoterBox.setSelectedIndex(0);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement("none");
				promoterSetToNone = false;			
			}
			if (gcm.influenceHasExplicitPromoter(selection)==null && !paramsOnly) {	
				promoterBox.setEnabled(true);
				promoterButton.setEnabled(true);
			}
			*/
		} else if (type.equals(GlobalConstants.ACTIVATION)) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(true);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			/*
			if (promoterSetToNone) {
				promoterBox.setSelectedIndex(0);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement("none");
				promoterSetToNone = false;			
			}
			if (gcm.influenceHasExplicitPromoter(selection)==null && !paramsOnly) {	
				promoterBox.setEnabled(true);
				promoterButton.setEnabled(true);
			}
			*/
		} else if (type.equals(GlobalConstants.NOINFLUENCE)) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(false);
			/*
			if (!promoterSetToNone) {
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement("none");
				promoterBox.setSelectedItem("none");
				promoterSetToNone = true;
			}
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
			*/
		} else if (type.equals(GlobalConstants.COMPLEX)) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			/*
			if (!promoterSetToNone) {
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement("none");
				promoterBox.setSelectedItem("none");
				promoterSetToNone = true;
			}
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
			*/
		} else {
			throw new IllegalStateException("Illegal state");
		}
	}

	private String[] options = { "Ok", "Cancel" };
	public static String[] types = { GlobalConstants.REPRESSION, GlobalConstants.ACTIVATION, 
		GlobalConstants.NOINFLUENCE, GlobalConstants.COMPLEX, GlobalConstants.PRODUCTION };
	 // options available for explicit promoters. Complex makes no sense, and no_influence breaks things.
	public static String[] explicitPromoterTypes = { GlobalConstants.REPRESSION, GlobalConstants.ACTIVATION, 
		GlobalConstants.NOINFLUENCE};
	public static String[] bio = { "no", "yes" };
	private HashMap<String, PropertyField> fields = null;
	private BioModel gcm = null;
	private String selection = "";
	private JComboBox promoterBox = null;
	private JComboBox typeBox = null;
	private JButton promoterButton;
	private boolean paramsOnly;
	private boolean promoterNameChange;
	private ModelEditor gcmEditor = null;
	private String promoterId = null;
	private String regulator = null;
	private String product = null;
	private Reaction production = null;
}