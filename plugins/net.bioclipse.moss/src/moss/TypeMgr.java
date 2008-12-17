/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*----------------------------------------------------------------------
  File    : TypeMgr.java
  Contents: class for a node or edge type manager
  Author  : Christian Borgelt
  History : 2007.06.20 file created
----------------------------------------------------------------------*/
package moss;
/*--------------------------------------------------------------------*/
/** Class for a node or edge type manager.
 *  @author Christian Borgelt
 *  @since  2007.06.20 */
/*--------------------------------------------------------------------*/
public abstract class TypeMgr {
  /*------------------------------------------------------------------*/
  /** Add a type to the type map.
   *  <p>If the name is already present, no new mapping is added,
   *  but the code already associated with the name is returned,
   *  thus automatically avoiding duplicate entries.</p>
   *  <p>If the type manager does not allow for adding types and
   *  the name is not present, this function should return -1.</p>
   *  @param  name the name of the type
   *  @return the code of the type or -1 if the name does not exist
   *          in this type manager and adding is not possible
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/
  public abstract int add (String name);
  /*------------------------------------------------------------------*/
  /** Map a type name to the corresponding type code.
   *  @param  name the name of the type
   *  @return the code of the type
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/
  public abstract int getCode (String name);
  /*------------------------------------------------------------------*/
  /** Map a type code to the corresponding type name.
   *  @param  code the code of the type
   *  @return the name of the type
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/
  public abstract String getName (int code);
}  /* class TypeMgr */
