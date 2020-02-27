/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
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
 *		Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * @author resix
 */
public class UnsupportedFeatureException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final String _bugmsg = "\nThe program encountered an unsupported situation! ";

	/**
	 *
	 */
	public UnsupportedFeatureException() {
		super(_bugmsg);
	}

	/**
	 * @param message
	 */
	public UnsupportedFeatureException(String message) {
		super(_bugmsg + "\n" + message);
		StackTraceElement[] ste = new StackTraceElement[0];
		setStackTrace(ste);
	}

	/**
	 * @param cause
	 */
	public UnsupportedFeatureException(Throwable cause) {
		super(_bugmsg + cause.toString());
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedFeatureException(String message, Throwable cause) {
		super(_bugmsg + message/*+cause.toString()*/);
	}
}
