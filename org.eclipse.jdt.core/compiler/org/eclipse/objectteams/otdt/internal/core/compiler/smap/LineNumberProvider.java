/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/** This class provides linenumbers which are greater than maximal linenumber of given type.
 *  Holds also mapping form linenumber -> mappend linenumber and its origin.
 *  Used in Rolemodel.
 *
 * @author ike
 */
public class LineNumberProvider
{
    private Hashtable <ReferenceBinding, Vector<LineInfo>>_sourceToLineInfos;
    private int _sourceEndLineNumber;
    private int _currentEndLineNumber;
	private ReferenceBinding _referenceBinding;

	// ensures each foreign line number is always mapped to the same synthetic line number above max:
	HashMap<ReferenceBinding, HashMap<Integer,Integer>> remapped = new HashMap<ReferenceBinding, HashMap<Integer,Integer>>();

    public LineNumberProvider(ReferenceBinding referenceBinding, int sourceEndLineNumber)
    {
    	this._referenceBinding = referenceBinding;
        this._sourceEndLineNumber = sourceEndLineNumber;
        this._currentEndLineNumber = sourceEndLineNumber;
        this._sourceToLineInfos = new Hashtable<ReferenceBinding, Vector<LineInfo>>();
    }

    public LineInfo getRemappedLineNumber(ReferenceBinding copySrc, List<LineInfo> lineInfos, Integer inputStartLine, int repeatCount)
    {
    	HashMap<Integer, Integer> srcRemapped = this.remapped.get(copySrc);
    	if (srcRemapped != null) {
    		Integer exist = srcRemapped.get(inputStartLine);
    		if (exist != null)
	    		for (LineInfo lineInfo : lineInfos)
					if (   lineInfo.getOutputStartLine() == exist
						&& lineInfo.getRepeatCount() >= repeatCount)
						return lineInfo;
	    	// either not registered yet or with too small an extent
    	} else {
    		srcRemapped = new HashMap<Integer, Integer>();
    		this.remapped.put(copySrc, srcRemapped);
    	}
        int remappedLineNumber = this._currentEndLineNumber + 1;
        this._currentEndLineNumber = remappedLineNumber;
        if (repeatCount > 1)
        	this._currentEndLineNumber += (repeatCount - 1);
        srcRemapped.put(inputStartLine, remappedLineNumber);

        LineInfo lineInfo = new LineInfo(inputStartLine, remappedLineNumber);

        if (repeatCount > -1)
        	lineInfo.setRepeatCount(repeatCount);
        // TODO(SH): is outputLineIncrement relevant here?

        lineInfos.add(lineInfo);
        return lineInfo;
    }

    /**
     * Add a line info that maps input lines (source code) to output lines (used in byte codes LNT)
     * @param copySrc        the type being translated
     * @param inputStartLine the input line number, i.e., source position within copySrc
     * @param repeatCount    the number of lines being handled
     * @return the (perhaps mapped) line number to be used in the byte code
     */
    public LineInfo addLineInfo(ReferenceBinding copySrc, int inputStartLine, int repeatCount)
    {
    	if (!this._sourceToLineInfos.containsKey(copySrc))
    		this._sourceToLineInfos.put(copySrc, new Vector<LineInfo>());
    	
    	List <LineInfo>lineInfos = this._sourceToLineInfos.get(copySrc);

    	int outputStartLine;
    	if (TypeBinding.notEquals(copySrc, this._referenceBinding) && inputStartLine < ISMAPConstants.STEP_INTO_LINENUMBER)
    		// map "foreign" lines to numbers above the current file's max:
    		return getRemappedLineNumber(copySrc, lineInfos, inputStartLine, repeatCount);

    	// not yet mapped, create an identity mapping:
    	outputStartLine = inputStartLine;

        LineInfo lineInfo = new LineInfo(inputStartLine, outputStartLine);

        if (repeatCount > -1)
        	lineInfo.setRepeatCount(repeatCount);
        // TODO(SH): is outputLineIncrement relevant here?

        lineInfos.add(lineInfo);
        return lineInfo;
    }

    public Hashtable<ReferenceBinding, Vector<LineInfo>> getLineInfos()
    {
        return this._sourceToLineInfos;
    }

    public List<LineInfo> getLineInfosForType(Object key)
    {
        return this._sourceToLineInfos.get(key);
    }

    public int getSourceEndLineNumber()
    {
        return this._sourceEndLineNumber;
    }

    public boolean containsLineInfos()
    {
        return this._sourceToLineInfos.size() > 0;
    }

	public void setRepeatCount(LineInfo lineInfo, int count) {
		lineInfo.setRepeatCount(count);
		this._currentEndLineNumber = Math.max(this._currentEndLineNumber, lineInfo.getOutputStartLine()+count-1);
	}
}
