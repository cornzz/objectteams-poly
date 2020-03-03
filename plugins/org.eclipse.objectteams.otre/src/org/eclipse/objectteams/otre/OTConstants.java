/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import org.apache.bcel.generic.*;


/**
 * Constants for the Object Teams Runtime Environment
 * @author Christine Hundt
 * @author Stephan Herrmann
 */
public interface OTConstants {
	// ------------------------------------------
	// ---------- Types: ------------------------
	// ------------------------------------------
	/**                            Type <tt>java.lang.Object</tt> */
    ObjectType object        = new ObjectType("java.lang.Object");
	/**                            Type <tt>java.lang.Object</tt> */
    ObjectType string        = new ObjectType("java.lang.String");
    /**                            Type <tt>java.lang.Class</tt> */
    ObjectType classType = new ObjectType("java.lang.Class");
    /** 						   Signature of java.lang.Class#getMethod(String, Class...) */
    public static final Type[] getMethodSignature = new Type[]{string, new ArrayType(classType, 1)};
    /**                            Type <tt>java.lang.reflect.Method</tt> */
    ObjectType methodType = new ObjectType("java.lang.reflect.Method");
    /**                            Type <tt>org.objectteams.Team</tt> */
    String     teamName          = "org.objectteams.ITeam";
    ObjectType teamType          = new ObjectType(teamName);
    String     teamClassName     = "org.objectteams.Team";
    ObjectType teamClassType     = new ObjectType(teamClassName);
	/**                            Type <tt>org.objectteams.LiftingVetoException</tt> */
    ObjectType liftingVeto   = new ObjectType("org.objectteams.LiftingVetoException");
	/**                            Type <tt>org.objectteams.LiftingVetoException</tt> */
    ObjectType liftingFailed = new ObjectType("org.objectteams.LiftingFailedException");
    /**                            Type <tt>org.objectteams.OTREInternalError</tt> */	
    ObjectType internalError = new ObjectType("org.objectteams.OTREInternalError");
	/**                            Type <tt>org.objectteams.ResultNotProvidedError</tt> */	
	ObjectType notProvidedError = new ObjectType("org.objectteams.ResultNotProvidedError");
	/**                            Type <tt>org.objectteams.UnsupportedFeatureException</tt> */	
	ObjectType unsupportedFeature = new ObjectType("org.objectteams.UnsupportedFeatureException");
	
	ObjectType threadType  = new ObjectType("java.lang.Thread");
	
	/**                          Type <tt>org.objectteams.Team[]</tt> */
    ArrayType  teamArray   = new ArrayType(teamType, 1);
	/**                          Type <tt>int[]</tt> */
    ArrayType  intArray    = new ArrayType(Type.INT, 1);
	/**                          Type <tt>java.lang.Object[]</tt> */
    ArrayType  objectArray = new ArrayType(object, 1);

	ObjectType roleSetType = new ObjectType("java.util.HashSet");
	
	ObjectType nullPointerException = new ObjectType("java.lang.NullPointerException");

	String STRING_BUFFER_NAME    = "java.lang.StringBuffer";
	
	String INIT	= "<init>";
	
	// ============ VERSION: ==============
	public static final int    OT_VERSION_MAJOR = 1;
    public static final int    OT_VERSION_MINOR = 6;
    
    // required compiler revision in the 0.9 stream:
    public static final int    OT09_REVISION = 26;

    // required compiler revision in the 1.0 stream:
    public static final int    OT10_REVISION = 0;
    
    // required compiler revision in the 1.1 stream:
    public static final int    OT11_REVISION = 0;

    // required compiler revision in the 1.2 stream:
    public static final int    OT12_REVISION = 0;

    // required compiler revision in the 1.3 stream:
    public static final int    OT13_REVISION = 0;

    // required compiler revision in the 1.4 stream:
    public static final int    OT14_REVISION = 1;

    // required compiler revision in the 1.5 (=0.7) stream:
    public static final int    OT15_REVISION = 0;

    // required compiler revision in the 1.6 (=0.8/2.0) stream:
    public static final int    OT16_REVISION = 0;

    // required compiler revision in the 1.7 (=2.3) stream:
    public static final int    OT17_REVISION = 0;

    // ------------------------------------------
	// ---------- Flags and Modifiers: ----------
	// ------------------------------------------
	/** Bytecode encoding of modifier <tt>team</tt> */
    final static int TEAM  = 1; // bit in OTClassFlags attribute

	// 'CallinFlags':
	final static int OVERRIDING =1; // this role method is inherited from the super role
	final static int WRAPPER =2; // this is a role method wrapper (in a team)

	// ------------------------------------------
	// ---------- Names: ------------------------
	// ------------------------------------------
	/**                              General prefix to mark all generated names. */
    final static String OT_PREFIX = "_OT$";
	/**                              Name of the base reference of roles. */
    final static String BASE      = "_OT$base";
	/**                              Name of the getBase method of roles (ifc and class). */
    final static String GET_BASE  = "_OT$getBase";
	/**							  Prefix for otdt. */
	final static String OTDT_PREFIX = "__OT__";
	/**							  Tsuper marker interface prefix. */
	final static String TSUPER_PREFIX = "TSuper__OT__";
	/** field for storing the class object in JVM < 5 */
	final static String SELF_CLASS 	= "_OT$self_class$";
	final static String CLASS 		= "_OT$class_literal$";

	// -----------------------------------------
	// ---------- Signature enhancement --------
	// -----------------------------------------
	
	/**                              Name of synthetic parameter. */
	final static String TEAMS     = "_OT$teams";
	/**                              Name of synthetic parameter. */
	final static String TEAMIDS   = "_OT$teamIDs";
	/**                              Name of synthetic parameter. */
	final static String IDX       = "_OT$idx";
	/**                              Name of synthetic parameter. */
	final static String BIND_IDX       = "_OT$bindIdx";
	/**                              Name of synthetic parameter. */
	final static String UNUSED    = "_OT$unusedArgs";
	/**                              Name of synthetic parameter. */
    final static String BASE_METH_TAG = "_OT$baseMethTag";

	/**                            Number of extra arguments in enhanced signatures. */
	static final int EXTRA_ARGS  = 6;
	/**                            Position of generated argument. */
	static final int TEAMS_ARG   = 1;
	/**                            Position of generated argument. */
	static final int TEAMIDS_ARG = 2;
	/**                            Position of generated argument. */
	static final int IDX_ARG     = 3;
	/**                            Position of generated argument. */
	static final int BIND_IDX_ARG = 4;
	/**                            Position of generated argument. */
    static final int BASE_METH_ARG = 5; // ## really const? also UNUSED?
	/**                            Position of generated argument. */
	static final int UNUSED_ARG  = 6;

	// ---------- Features to prevent/aid garbage collection: ----------
	String ROLE_SET              = OT_PREFIX + "roleSet";    // field  HashSet _OT$roleSet;
	String ADD_ROLE              = OT_PREFIX + "addRole";    // method void _OT$addRole(Object)
	String REMOVE_ROLE           = OT_PREFIX + "removeRole"; // method void _OT$removeRole(Object)

	String IBOUND_BASE           = "org.objectteams.IBoundBase"; // interface comprising the above methods.
	
	
	// -----------------------------------------
	// ---------- Other constants --------
	// -----------------------------------------
	/**							Marker for comment lines in the team config file. */    
	static final String COMMENT_MARKER = "#";
	/**							Constant for invalid base method tags (a method can not be relocated from a base call). */    
	static final int INVALID_BASE_METHOD_TAG = -2;
	
	// --------------------------------------------------------------
	// ---------- Separator for static replace binding keys ---------
	// --------------------------------------------------------------
	 static final String STATIC_REPLACE_BINDING_SEPARATOR = ".."; 
	 
	 // ------------------------------------------------------------------------------------
	 // ---------- Linenumbers with more information.  ----------------------
	 // ---------- (semantic linenumber) For debugging purpose.  ------
	 // ------------------------------------------------------------------------------------
	 static final int STEP_OVER_LINENUMBER = Short.MAX_VALUE *2;
	 static final int STEP_INTO_LINENUMBER = STEP_OVER_LINENUMBER - 1;
}
