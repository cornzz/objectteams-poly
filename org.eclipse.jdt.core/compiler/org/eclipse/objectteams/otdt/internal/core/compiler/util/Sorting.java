/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 20038 Technical University Berlin, Germany.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id: Sorting.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * Sorting utilities.
 *
 * @author stephan
 * @since 1.2.1
 */
public class Sorting {

	/**
	 * Topological sort for member types with depth first search.
	 * Guarantee: supertypes come before subtypes.
	 */
	public static void sortMemberTypes(SourceTypeBinding enclosing) {
		int len = enclosing.memberTypes.length;

		ReferenceBinding[] unsorted = new ReferenceBinding[len];
		ReferenceBinding[] sorted = new ReferenceBinding[len];
		System.arraycopy(enclosing.memberTypes, 0, unsorted, 0, len);

		int o = 0;
		for(int i=0; i<len; i++)
			o = sort(enclosing, unsorted, i, sorted, o);

		enclosing.memberTypes = sorted;
	}
	// Transfer input[i] and all its supers into output[o] ff.
	private static int sort(ReferenceBinding enclosing,
							ReferenceBinding[] input, int i,
							ReferenceBinding[] output, int o)
	{
		if (input[i] == null)
			return o;

		ReferenceBinding superclass = input[i].superclass();
		o = sortSuper(enclosing, superclass, input, output, o);

		for (ReferenceBinding superIfc : input[i].superInterfaces())
			o = sortSuper(enclosing, superIfc, input, output, o);

		// done with supers, now input[i] can safely be transferred:
		output[o++] = input[i];
		input[i] = null;

		return o;
	}
	// if superclass is within the set of member types to sort,
	// transfer it and all its supers to output[o] ff.
	private static int sortSuper(ReferenceBinding enclosing,
						  		 ReferenceBinding superclass,
						  		 ReferenceBinding[] input,
						  		 ReferenceBinding[] output, int o)
	{
		if (   superclass != null // inspecting super of Confined?
			&& superclass.id != TypeIds.T_JavaLangObject
			&& TypeBinding.equalsEquals(superclass.enclosingType(), enclosing)) // is super within scope?
		{
			// search superclass within input:
			int j = 0;
			for(j=0; j<input.length; j++)
				if (TypeBinding.equalsEquals(input[j], superclass))
					break;
			if (j < input.length)
				// depth first traversal:
				o = sort(enclosing, input, j, output, o);
			// otherwise assume super was already transferred.
		}
		return o;
	}

	// --- similar for role models:

	public static RoleModel[] sortRoles(RoleModel[] unsorted) {
		int len = unsorted.length;

		RoleModel[] sorted = new RoleModel[len];
		int o = 0;
		for(int i=0; i<len; i++)
			o = sort(unsorted, i, sorted, o);
		// also consider the base hierarchy, but for roles bound to
		// the same base keep the existing order.
		Arrays.sort(sorted, new Comparator<RoleModel>() {
			@Override
			public int compare(RoleModel o1, RoleModel o2) {
				ReferenceBinding b1 = o1.getBaseTypeBinding();
				ReferenceBinding b2 = o2.getBaseTypeBinding();
				if (TypeBinding.equalsEquals(b1, b2))
					return 0;
				if (b1 != null && b1.id != TypeIds.T_JavaLangObject) {
					if (b2 == null || b2.id == TypeIds.T_JavaLangObject)
						return 1;
					if (b1.isCompatibleWith(b2))
						return 1;
					if (b2.isCompatibleWith(b1))
						return -1;
				} else {
					if (b2 != null && b2.id != TypeIds.T_JavaLangObject)
						return -1;
				}
				return 0;
			}
		});

		return sorted;
	}

	// Transfer input[i] and all its supers into output[o] ff.
	private static int sort(RoleModel[] input, int i,
							RoleModel[] output, int o)
	{
		if (input[i] == null || input[i].getBinding() == null)
			return o;

		ReferenceBinding inBinding = input[i].getBinding();
		ReferenceBinding superclass = inBinding.superclass();
		o = sortSuper(superclass, input, output, o);

		for (ReferenceBinding superIfc : inBinding.superInterfaces())
			o = sortSuper(superIfc, input, output, o);

		// done with supers, now input[i] can safely be transferred:
		output[o++] = input[i];
		input[i] = null;

		return o;
	}
	// if superclass is within the set of member types to sort,
	// transfer it and all its supers to output[o] ff.
	private static int sortSuper(ReferenceBinding superclass,
						  		 RoleModel[] input,
						  		 RoleModel[] output, int o)
	{
		if (   superclass != null // inspecting super of Confined?
			&& superclass.id != TypeIds.T_JavaLangObject) // is super within scope?
		{
			boolean inScope = false;
			for (RoleModel rm : input)
				if (rm != null && TypeBinding.equalsEquals(rm.getBinding(), superclass)) {
					inScope = true;
					break;
				}
			if (inScope) {

				// search superclass within input:
				int j = 0;
				for(j=0; j<input.length; j++)
					if (input[j] != null && TypeBinding.equalsEquals(input[j].getBinding(), superclass))
						break;
				if (j < input.length)
					// depth first traversal:
					o = sort(input, j, output, o);
				// otherwise assume super was already transferred.
			}
		}
		return o;
	}

	/** Apply the sorting from member type bindings to their ASTs, too. */
	public static void sortMemberTypes(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.memberTypes == null) return;
		ReferenceBinding[] bindings = typeDeclaration.binding.memberTypes;
		TypeDeclaration[] unsorted = typeDeclaration.memberTypes;
		TypeDeclaration[] newMembers = new TypeDeclaration[unsorted.length];
		int l = 0;
		allMembers: for (int i=0; i<bindings.length; i++) {
			ReferenceBinding current = bindings[i];
			if (current.isBinaryBinding())
				continue; // no AST: phantom or reused rofi
			// find corresponding AST
			for (int j=0; j<unsorted.length; j++) {
				if (TypeBinding.equalsEquals(unsorted[j].binding, current)) {
					newMembers[l++] = unsorted[j];
					continue allMembers;
				}
			}
			throw new InternalCompilerError("Unmatched member type "+String.valueOf(current.readableName())); //$NON-NLS-1$
		}
		if (l<newMembers.length)
			throw new InternalCompilerError("Not all member types matched: "+l); //$NON-NLS-1$
		typeDeclaration.memberTypes = newMembers;
	}
}
