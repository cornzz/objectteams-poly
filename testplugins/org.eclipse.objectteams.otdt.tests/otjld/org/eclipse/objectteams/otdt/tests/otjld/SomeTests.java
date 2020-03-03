/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld;

import org.eclipse.objectteams.otdt.tests.otjld.callinbinding.BaseCalls;
import org.eclipse.objectteams.otdt.tests.otjld.callinbinding.CallinMethodBinding;
import org.eclipse.objectteams.otdt.tests.otjld.callinbinding.CallinParameterMapping_LiftingAndLowering;
import org.eclipse.objectteams.otdt.tests.otjld.callinbinding.CallinWithTranslation;
import org.eclipse.objectteams.otdt.tests.otjld.calloutbinding.CalloutMethodBinding;
import org.eclipse.objectteams.otdt.tests.otjld.calloutbinding.CalloutParameterBinding_LiftingAndLowering;
import org.eclipse.objectteams.otdt.tests.otjld.calloutbinding.CalloutToField;
import org.eclipse.objectteams.otdt.tests.otjld.liftlower.DeclaredLifting;
import org.eclipse.objectteams.otdt.tests.otjld.regression.DevelopmentExamples;
import org.eclipse.objectteams.otdt.tests.otjld.regression.ReportedBugs;
import org.eclipse.objectteams.otdt.tests.otjld.roleplaying.LiftingAndLowering;
import org.eclipse.objectteams.otdt.tests.otjld.rolesandteams.Covariance;
import org.eclipse.objectteams.otdt.tests.otjld.rolesandteams.TeamNesting;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author stephan
 */
public class SomeTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All OTJLD Tests");
		
		// 1. rolesandteams
//		/*1.1*/suite.addTest(RoleObjectContainment.suite());
		/*1.1.10-*/suite.addTest(TeamNesting.suite());
//		/*1.2*/suite.addTest(InheritanceHierarchyOfTeams.suite());
//		/*1.3*/suite.addTest(AcquisitionAndInheritanceOfRoleClasses.suite());
//		/*1.4*/suite.addTest(RegularRoleInheritance.suite());
//		/*1.5*/suite.addTest(FileStructure.suite());
//		/*1.6*/suite.addTest(ExternalizedRoles.suite());
//		
//		/*6.2*/suite.addTest(OldExternalizedRoles.suite());
//		
//		/*1.7*/suite.addTest(Confinement.suite());
//		/*1.8*/suite.addTest(RelevantRole.suite());
//		/*1.9*/suite.addTest(ValueParameters.suite());
//		
//		/*0.c*/suite.addTest(ImplicitInheritance.suite());
		/*6.3*/suite.addTest(Covariance.suite());
//		
//		// 2. roleplaying
//		/*2.1*/suite.addTest(PlayedByRelation.suite());
		/*2.2*/suite.addTest(LiftingAndLowering.suite());
//		/*2.3*/suite.addTest(ExplicitRoleCreation.suite());
//		/*2.4*/suite.addTest(BaseClassVisibility.suite());
//		/*2.5*/suite.addTest(GC.suite());
//		
//		/*7.3*/suite.addTest(AllBindingAmbiguitiesTests.suite());
//		
//		// 3. calloutbinding
		/*3.1*/suite.addTest(CalloutMethodBinding.suite()); // includes a few from 7.2.1
		/*3.2*/suite.addTest(CalloutParameterBinding_LiftingAndLowering.suite());
		/*3.3*/suite.addTest(CalloutToField.suite());
		
//		/*7.4*/suite.addTest(OverridingAccessRestrictions.suite());
//		
//		// 4. callinbinding
		/*4.1*/suite.addTest(CallinMethodBinding.suite()); // includes a few from 7.2.[45]
		/*4.3*/suite.addTest(CallinParameterMapping_LiftingAndLowering.suite());
		/*4.4*/suite.addTest(CallinWithTranslation.suite());
		/*4.5*/suite.addTest(BaseCalls.suite());
//		
//		// 5. teamactivation
//		/*5.2*/suite.addTest(ExplicitTeamActivation.suite());
//		/*5.3*/suite.addTest(ImplicitTeamActivation.suite());
//		
//		// 6.[14] liftlower
		/*6.1*/suite.addTest(DeclaredLifting.suite());
//		/*6.4*/suite.addTest(AllSmartLiftingTests.suite());
//		
//		// 8. syntax
//		/*8.1*/suite.addTest(Syntax.suite());
//		
//		// api:
//		/*9.2*/suite.addTest(Reflection.suite());
//		
//		// other:
//		/*0.a*/suite.addTest(AccessModifiers.suite());
//		/*7.1*/suite.addTest(Modifiers.suite());
//		/*7.5*/suite.addTest(Exceptions.suite());
//		/*A.1*/suite.addTest(Java5.suite());
//		/*0.m*/suite.addTest(Misc.suite());
//		
//		// regression:
		/*B.1*/suite.addTest(ReportedBugs.suite());
//		/*B.2*/suite.addTest(CompilationOrder.suite());
		/*X.2*/suite.addTest(DevelopmentExamples.suite());
		return suite;
	}
}
