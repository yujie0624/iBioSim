package biomodel.gui.textualeditor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;
import main.util.MutableBoolean;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.UnitDefinition;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.ModelEditor;
import biomodel.gui.SBOLField;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


public class ModelPanel extends JButton implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTextField modelID; // the model's ID

	private JTextField modelName; // the model's Name

	private JButton modelEditor;

	private JComboBox substanceUnits, timeUnits, volumeUnits, areaUnits, lengthUnits, extentUnits, conversionFactor;

	private SBOLField sbolField;
	
	private BioModel bioModel;
	
	private ModelEditor gcmEditor;

	private MutableBoolean dirty;

	public ModelPanel(BioModel gcm, ModelEditor gcmEditor) {
		super();
		this.bioModel = gcm;
		this.gcmEditor = gcmEditor;
		this.dirty = gcmEditor.getDirty();
		this.setText("Edit Model Attributes");
		this.addActionListener((ActionListener) this);
		if (gcmEditor.isParamsOnly()) {
			this.setEnabled(false);
		}
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void modelEditor(String option) {
		JPanel modelEditorPanel;
		modelEditorPanel = new JPanel(new GridLayout(10, 2));
		Model model = bioModel.getSBMLDocument().getModel();
		modelName = new JTextField(model.getName(), 50);
		modelID = new JTextField(model.getId(), 16);
		modelName = new JTextField(model.getName(), 40);
		JLabel modelIDLabel = new JLabel("Model ID:");
		JLabel modelNameLabel = new JLabel("Model Name:");
		modelID.setEditable(false);
		modelEditorPanel.add(modelIDLabel);
		modelEditorPanel.add(modelID);
		modelEditorPanel.add(modelNameLabel);
		modelEditorPanel.add(modelName);
		if (bioModel.getSBMLDocument().getLevel() > 2) {
			JLabel substanceUnitsLabel = new JLabel("Substance Units:");
			JLabel timeUnitsLabel = new JLabel("Time Units:");
			JLabel volumeUnitsLabel = new JLabel("Volume Units:");
			JLabel areaUnitsLabel = new JLabel("Area Units:");
			JLabel lengthUnitsLabel = new JLabel("Length Units:");
			JLabel extentUnitsLabel = new JLabel("Extent Units:");
			JLabel conversionFactorLabel = new JLabel("Conversion Factor:");
			substanceUnits = new JComboBox();
			substanceUnits.addItem("( none )");
			timeUnits = new JComboBox();
			timeUnits.addItem("( none )");
			volumeUnits = new JComboBox();
			volumeUnits.addItem("( none )");
			areaUnits = new JComboBox();
			areaUnits.addItem("( none )");
			lengthUnits = new JComboBox();
			lengthUnits.addItem("( none )");
			extentUnits = new JComboBox();
			extentUnits.addItem("( none )");
			conversionFactor = new JComboBox();
			conversionFactor.addItem("( none )");
			ListOf listOfUnits = bioModel.getSBMLDocument().getModel().getListOfUnitDefinitions();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
						&& (unit.getUnit(0).getExponentAsDouble() == 1)) {
					substanceUnits.addItem(unit.getId());
					extentUnits.addItem(unit.getId());
				}
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isSecond()) && (unit.getUnit(0).getExponentAsDouble() == 1)) {
					timeUnits.addItem(unit.getId());
				}
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponentAsDouble() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 3)) {
					volumeUnits.addItem(unit.getId());
				}
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 2)) {
					areaUnits.addItem(unit.getId());
				}
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 1)) {
					lengthUnits.addItem(unit.getId());
				}
			}
			substanceUnits.addItem("dimensionless");
			substanceUnits.addItem("gram");
			substanceUnits.addItem("item");
			substanceUnits.addItem("kilogram");
			substanceUnits.addItem("mole");
			timeUnits.addItem("dimensionless");
			timeUnits.addItem("second");
			volumeUnits.addItem("dimensionless");
			volumeUnits.addItem("litre");
			areaUnits.addItem("dimensionless");
			lengthUnits.addItem("dimensionless");
			lengthUnits.addItem("metre");
			extentUnits.addItem("dimensionless");
			extentUnits.addItem("gram");
			extentUnits.addItem("item");
			extentUnits.addItem("kilogram");
			extentUnits.addItem("mole");
			ListOf listOfParameters = bioModel.getSBMLDocument().getModel().getListOfParameters();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumParameters(); i++) {
				Parameter param = (Parameter) listOfParameters.get(i);
				if (param.getConstant() && !bioModel.IsDefaultParameter(param.getId())) {
					conversionFactor.addItem(param.getId());
				}
			}
			if (option.equals("OK")) {
				substanceUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getSubstanceUnits());
				timeUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getTimeUnits());
				volumeUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getVolumeUnits());
				areaUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getAreaUnits());
				lengthUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getLengthUnits());
				extentUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getExtentUnits());
				conversionFactor.setSelectedItem(bioModel.getSBMLDocument().getModel().getConversionFactor());
			}
			sbolField = new SBOLField(GlobalConstants.SBOL_DNA_COMPONENT, gcmEditor, 1);
			LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(bioModel.getSBMLDocument().getModel());
			if (sbolURIs.size() > 0)
				sbolField.setSBOLURIs(sbolURIs);
			modelEditorPanel.add(substanceUnitsLabel);
			modelEditorPanel.add(substanceUnits);
			modelEditorPanel.add(timeUnitsLabel);
			modelEditorPanel.add(timeUnits);
			modelEditorPanel.add(volumeUnitsLabel);
			modelEditorPanel.add(volumeUnits);
			modelEditorPanel.add(areaUnitsLabel);
			modelEditorPanel.add(areaUnits);
			modelEditorPanel.add(lengthUnitsLabel);
			modelEditorPanel.add(lengthUnits);
			modelEditorPanel.add(extentUnitsLabel);
			modelEditorPanel.add(extentUnits);
			modelEditorPanel.add(conversionFactorLabel);
			modelEditorPanel.add(conversionFactor);
			modelEditorPanel.add(new JLabel("SBOL DNA Component: "));
			modelEditorPanel.add(sbolField);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (bioModel.getSBMLDocument().getLevel() > 2) {
				if (substanceUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetSubstanceUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setSubstanceUnits((String) substanceUnits.getSelectedItem());
				}
				if (timeUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetTimeUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setTimeUnits((String) timeUnits.getSelectedItem());
				}
				if (volumeUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetVolumeUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setVolumeUnits((String) volumeUnits.getSelectedItem());
				}
				if (areaUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetAreaUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setAreaUnits((String) areaUnits.getSelectedItem());
				}
				if (lengthUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetLengthUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setLengthUnits((String) lengthUnits.getSelectedItem());
				}
				if (extentUnits.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetExtentUnits();
				}
				else {
					bioModel.getSBMLDocument().getModel().setExtentUnits((String) extentUnits.getSelectedItem());
				}
				if (conversionFactor.getSelectedItem().equals("( none )")) {
					bioModel.getSBMLDocument().getModel().unsetConversionFactor();
				}
				else {
					bioModel.getSBMLDocument().getModel().setConversionFactor((String) conversionFactor.getSelectedItem());
				}
			}
			bioModel.getSBMLDocument().getModel().setName(modelName.getText());
			dirty.setValue(true);
			bioModel.makeUndoPoint();
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Units Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		// Add SBOL annotation to promoter
		if (value == JOptionPane.YES_OPTION) {
			LinkedList<URI> sbolURIs = sbolField.getSBOLURIs();
			if (sbolURIs.size() > 0) {
				SBOLAnnotation sbolAnnot = new SBOLAnnotation(bioModel.getSBMLDocument().getModel().getMetaId(), sbolURIs);
				AnnotationUtility.setSBOLAnnotation(bioModel.getSBMLDocument().getModel(), sbolAnnot);
			} else
				AnnotationUtility.removeSBOLAnnotation(bioModel.getSBMLDocument().getModel());
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	public void actionPerformed(ActionEvent e) {
		// if the add unit button is clicked
		// if the add function button is clicked
		if (e.getSource() == this) {
			modelEditor("OK");
		}

	}

	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
