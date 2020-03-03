/**********************************************************************
 * This file is part of "Object Teams Runtime Environment"-Software
 * 
 * Copyright 2012 GK Software AG
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
 * 
 * This file is based on class org.apache.bcel.generic.BranchHandle
 * originating from the Apache BCEL project which was provided under the 
 * Apache 2.0 license. Original Copyright from BCEL:
 * 
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package org.apache.bcel.generic;

/**
 * BranchHandle is returned by specialized InstructionList.append() whenever a
 * BranchInstruction is appended. This is useful when the target of this
 * instruction is not known at time of creation and must be set later
 * via setTarget().
 *
 * @see InstructionHandle
 * @see Instruction
 * @see InstructionList
 * @version $Id: BranchHandle.java 386056 2006-03-15 11:31:56Z tcurdt $
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public final class BranchHandle extends InstructionHandle {

    private BranchInstruction bi; // An alias in fact, but saves lots of casts


    private BranchHandle(BranchInstruction i) {
        super(i);
        bi = i;
    }

    /** Factory methods.
     */

    static final BranchHandle getBranchHandle( BranchInstruction i ) {
        return new BranchHandle(i);
    }


    /* Override InstructionHandle methods: delegate to branch instruction.
     * Through this overriding all access to the private i_position field should
     * be prevented.
     */
    public int getPosition() {
        return bi.position;
    }


    void setPosition( int pos ) {
        i_position = bi.position = pos;
    }


    protected int updatePosition( int offset, int max_offset ) {
        int x = bi.updatePosition(offset, max_offset);
        i_position = bi.position;
        return x;
    }


    /**
     * Pass new target to instruction.
     */
    public void setTarget( InstructionHandle ih ) {
        bi.setTarget(ih);
    }


    /**
     * Update target of instruction.
     */
    public void updateTarget( InstructionHandle old_ih, InstructionHandle new_ih ) {
        bi.updateTarget(old_ih, new_ih);
    }


    /**
     * @return target of instruction.
     */
    public InstructionHandle getTarget() {
        return bi.getTarget();
    }


    /** 
     * Set new contents. Old instruction is disposed and may not be used anymore.
     */
    public void setInstruction( Instruction i ) {
        super.setInstruction(i);
        if (!(i instanceof BranchInstruction)) {
            throw new ClassGenException("Assigning " + i
                    + " to branch handle which is not a branch instruction");
        }
        bi = (BranchInstruction) i;
    }
}
