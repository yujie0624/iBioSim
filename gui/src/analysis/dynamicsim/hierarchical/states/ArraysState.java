package analysis.dynamicsim.hierarchical.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.ArraysObject;
import analysis.dynamicsim.hierarchical.util.IndexObject;

public abstract class ArraysState extends HierarchicalState
{

	private HashMap<String, List<ArraysObject>>	dimensionObjects;
	private HashMap<String, IndexObject>		indexObjects;

	private HashMap<String, ASTNode>			arraysIDs;
	private final HashMap<String, ASTNode>		arraysMetaIDs;

	private HashSet<String>						arrayedObjects;
	private HashSet<String>						arrayedMetaObjects;

	private HashMap<String, String>				arraySizeToSBase;

	public ArraysState(HashMap<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
		dimensionObjects = new HashMap<String, List<ArraysObject>>();
		arrayedObjects = new HashSet<String>();
		arrayedMetaObjects = new HashSet<String>();
		arraysIDs = new HashMap<String, ASTNode>();
		arraysMetaIDs = new HashMap<String, ASTNode>();
		indexObjects = new HashMap<String, IndexObject>();
		arraySizeToSBase = new HashMap<String, String>();
	}

	public ArraysState(ArraysState state)
	{
		super(state);
		dimensionObjects = new HashMap<String, List<ArraysObject>>(state.dimensionObjects);
		arraysIDs = new HashMap<String, ASTNode>(state.arraysIDs);
		arraysMetaIDs = new HashMap<String, ASTNode>(state.arraysMetaIDs);
		arrayedObjects = new HashSet<String>(state.arrayedObjects);
		arrayedMetaObjects = new HashSet<String>(state.arrayedMetaObjects);
		indexObjects = new HashMap<String, IndexObject>(indexObjects);
	}

	public void addArrayedObject(String id)
	{
		arrayedObjects.add(id);
	}

	public void addArrayedMetaObject(String id)
	{
		arrayedMetaObjects.add(id);
	}

	public void addSizeTrigger(String parameterSize, String id)
	{
		arraySizeToSBase.put(parameterSize, id);
	}

	public String addValue(String id, double value, int... indices)
	{
		ASTNode vector = arraysIDs.get(id);
		String newId = id;
		ASTNode child;
		if (vector == null)
		{
			vector = new ASTNode(ASTNode.Type.VECTOR);
			arraysIDs.put(id, vector);
		}
		child = vector;
		for (int i = indices.length - 1; i >= 0; i--)
		{
			while (child.getChildCount() <= indices[i])
			{
				child.addChild(new ASTNode(ASTNode.Type.VECTOR));
			}
			child = child.getChild(indices[i]);
			newId += "_" + indices[i];
		}
		child.setType(ASTNode.Type.NAME);
		child.setName(newId);
		return newId;
	}

	public void addSBase(String metaid, int... indices)
	{
		ASTNode vector = arraysMetaIDs.get(metaid);
		String newId = metaid;
		ASTNode child;
		if (vector == null)
		{
			vector = new ASTNode(ASTNode.Type.VECTOR);
			arraysMetaIDs.put(metaid, vector);
		}
		child = vector;
		for (int i = indices.length - 1; i >= 0; i--)
		{
			while (child.getChildCount() <= indices[i])
			{
				child.addChild(new ASTNode(ASTNode.Type.VECTOR));
			}
			child = child.getChild(indices[i]);
			newId += "_" + indices[i];
		}

		child.setName(newId);
	}

	public void addDimension(String id, String size, int arrayDim)
	{
		List<ArraysObject> list = dimensionObjects.get(id);
		if (list == null)
		{
			list = new ArrayList<ArraysObject>();
			dimensionObjects.put(id, list);
		}
		list.add(new ArraysObject(size, arrayDim));
	}

	public void addIndex(String id, String attribute, int dim, ASTNode math)
	{
		if (indexObjects.containsKey(id))
		{
			indexObjects.get(id).addDimToAttribute(attribute, dim, math);
		}
		else
		{
			IndexObject obj = new IndexObject();
			obj.addDimToAttribute(attribute, dim, math);
			indexObjects.put(id, obj);
		}
	}

	public HashMap<String, List<ArraysObject>> getDimensionObjects()
	{
		return dimensionObjects;
	}

	public int getDimensionCount(String id)
	{
		return dimensionObjects.get(id) == null ? -1 : dimensionObjects.get(id).size();
	}

	public void setDimensionObjects(HashMap<String, List<ArraysObject>> dimensionObjects)
	{
		this.dimensionObjects = dimensionObjects;
	}

	public HashSet<String> getArrayedObjects()
	{
		return arrayedObjects;
	}

	public boolean isArrayed(String id)
	{
		return arrayedObjects.contains(id) || arrayedMetaObjects.contains(id);
	}

	public void setArrayedObjects(HashSet<String> arrayedObjects)
	{
		this.arrayedObjects = arrayedObjects;
	}

	public HashMap<String, ASTNode> getValues()
	{
		return arraysIDs;
	}

	public void setValues(HashMap<String, ASTNode> values)
	{
		this.arraysIDs = values;
	}

	public HashMap<String, IndexObject> getIndexObjects()
	{
		return indexObjects;
	}

	public void setIndexObjects(HashMap<String, IndexObject> indexObjects)
	{
		this.indexObjects = indexObjects;
	}

	public HashSet<String> getArrayedMetaObjects()
	{
		return arrayedMetaObjects;
	}

	public void setArrayedMetaObjects(HashSet<String> arrayedMetaObjects)
	{
		this.arrayedMetaObjects = arrayedMetaObjects;
	}

	public HashMap<String, String> getArraySizeToSBase()
	{
		return arraySizeToSBase;
	}

	public void setArraySizeToSBase(HashMap<String, String> arraySizeToSBase)
	{
		this.arraySizeToSBase = arraySizeToSBase;
	}

}