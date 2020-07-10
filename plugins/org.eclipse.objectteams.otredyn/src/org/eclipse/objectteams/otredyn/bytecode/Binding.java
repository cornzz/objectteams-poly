/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2016 Oliver Frank and others.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Oliver Frank - Initial API and implementation
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode;

import org.eclipse.objectteams.otredyn.runtime.IBinding;

/**
 * This class represents a callin or decapsulation binding
 * @author Oliver Frank
 */
public class Binding implements Comparable<Binding>, IBinding {
	
	/** The callin modifier 'before' */
	public static final int BEFORE = 1;
	/** The callin modifier 'replace' */
	public static final int REPLACE = 2;
	/** The callin modifier 'after' */
	public static final int AFTER = 3;
	
	AbstractBoundClass teamClass;
	private String callinLabel;
	private String boundClass;
	private String memberName;
	private String memberSignature;
	private String weavableBaseClassName;
	private String roleName;
	private String roleMethodName;
	private String roleMethodSignature;
	private int callinModifier;
	/**
	 * Locally unique id for (an element in) this binding:
	 * For CALLIN_BINDING this is the callinId,
	 * for METHOD_ACCESS or FIELD_ACCESS it is the perTeamAccessId
	 */
	private int perTeamId;
	private int baseFlags;
	private IBinding.BindingType type;
	private boolean isHandleCovariantReturn;
	private boolean requireBaseSuperCall;
	
	public IBinding.BindingType getType() {
		return type;
	}

	/**
	 * Create a callin binding.
	 */
	public Binding(AbstractBoundClass teamClass,
			String roleClassName, String roleMethodName, String roleMethodSignature, String callinLabel, String boundClassName, 
			String memberName, String memberSignature, String weavableBaseClassName,
			int callinModifier, int callinId, int baseFlags, boolean handleCovariantReturn, boolean requireBaseSuperCall) 
	{
		this.teamClass = teamClass;
		this.callinLabel = callinLabel;
		this.boundClass = boundClassName;
		this.memberName = memberName;
		this.memberSignature = memberSignature;
		this.weavableBaseClassName = weavableBaseClassName;
		this.callinModifier = callinModifier;
		this.perTeamId = callinId;
		this.baseFlags = baseFlags;
		this.type = IBinding.BindingType.CALLIN_BINDING;
		this.isHandleCovariantReturn = handleCovariantReturn;
		this.requireBaseSuperCall = requireBaseSuperCall;
		this.roleName = roleClassName;
		this.roleMethodName = roleMethodName;
		this.roleMethodSignature = roleMethodSignature;
	}

	/**
	 * Create a method or field access binding (decapsulation).
	 */
	public Binding(AbstractBoundClass teamClass,
			String boundClassName, 
			String memberName, String memberSignature, int perTeamAccessId, IBinding.BindingType type) 
	{
		this.teamClass = teamClass;
		this.boundClass = boundClassName;
		this.memberName = memberName;
		this.memberSignature = memberSignature;
		this.perTeamId = perTeamAccessId;
		this.type = type;
	}

	/** Create a binding for a role base binding to trigger generating _OT$addRemoveRole. */
	public Binding(AbstractBoundClass teamClass, String baseClassName) {
		this.teamClass = teamClass;
		this.boundClass = baseClassName;
		this.type = IBinding.BindingType.ROLE_BASE_BINDING;
	}

	public String getBoundClass() {
		return boundClass;
	}
	
	public String getMemberName() {
		return memberName;
	}

	public String getMemberSignature() {
		return memberSignature;
	}
	
	public int getBaseFlags() {
		return baseFlags;
	}

	public int getPerTeamId() {
		return perTeamId;
	}

	public boolean isHandleCovariantReturn() {
		return this.isHandleCovariantReturn;
	}
	
	@Override
	public boolean requiresBaseSuperCall() {
		return this.requireBaseSuperCall;
	}

	public String getDeclaringBaseClassName() {
		return this.weavableBaseClassName;
	}

	@Override
	public boolean equals(Object obj) {
		Binding other = (Binding) obj;
		return boundClass.equals(other.boundClass)
				&& memberName.equals(other.memberName)
				&& memberSignature.equals(other.memberSignature)
				&& type == other.type && perTeamId == other.perTeamId;
	}
	
	public int compareTo(Binding other) {
		// ordering strategy for callin bindings:
		// - first criterion: callinModifier: before/after have higher priority than replace.
		// - second criterion: precedence (only relevant among callins of the same callin modifier).
		// the set AbstractTeam.bindings is sorted low-to-high.
		// then TeamManager.handleTeamStateChange processes all bindings from low-to-high
		// inserting each at the front of the list of active teams, such that last added
		// will indeed have highest priority, which is in line with ordering by activation time.
		int compare = 0;
		if (this.callinLabel == null || other.callinLabel == null) {
			// at least one binding is a decaps binding
			if (this.callinLabel != null)
				return 1;
			else if (other.callinLabel != null)
				return -1;
		} else {
			if (this.callinModifier != other.callinModifier) {
				// replace has lower priority than before/after:
				if (this.callinModifier == REPLACE)
					return -1;
				else if (other.callinModifier == REPLACE)
					return 1;
			}
			// the following comparison respects precedence:
			compare = this.teamClass.compare(this.callinLabel, other.callinLabel);
			if (compare != 0)
				return compare;
		}
		if (this.baseFlags != other.baseFlags)
			return Integer.valueOf(this.baseFlags).compareTo(other.baseFlags);
		return (boundClass + memberName + memberSignature).compareTo(other.boundClass + other.memberName + other.memberSignature);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		switch (this.type) {
		case CALLIN_BINDING: buf.append("callin: ");break;
		case METHOD_ACCESS: buf.append("callout: ");break;
		case FIELD_ACCESS: buf.append("callout-to-field: ");break;
		case ROLE_BASE_BINDING: 
			buf.append("playedBy: ").append(this.boundClass);
			return buf.toString();
		}
		buf.append('{');
		buf.append(this.perTeamId);
		buf.append("} ");
		buf.append(this.boundClass);
		buf.append('.');
		buf.append(this.memberName);
		buf.append(this.memberSignature);
		return buf.toString();
	}

	@Override
	public String getRoleClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRoleMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRoleMethodSignature() {
		// TODO Auto-generated method stub
		return null;
	}
}
