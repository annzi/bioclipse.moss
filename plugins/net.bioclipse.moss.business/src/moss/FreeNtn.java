/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*----------------------------------------------------------------------
  File    : FreeNtn.java
  Contents: abstract class for free graph notations
  Author  : Christian Borgelt
  History : 2007.06.22 file created
----------------------------------------------------------------------*/
package moss;

/*--------------------------------------------------------------------*/
/** Class for free graph notations (with dynamic type managers).
 *  @author Christian Borgelt
 *  @since  2007.06.22 */
/*--------------------------------------------------------------------*/
public abstract class FreeNtn extends Notation {

  /*------------------------------------------------------------------*/
  /*  instance variables                                              */
  /*------------------------------------------------------------------*/
  /** the manager for the node types */
  protected TypeMgr nodemgr;
  /** the manager for the edge types */
  protected TypeMgr edgemgr;

  /*------------------------------------------------------------------*/
  /** Whether this notation has a fixed set of (node and edge) types.
   *  @return <code>false</code>, because any type managers can be used
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public boolean hasFixedTypes ()
  { return false; }

  /*------------------------------------------------------------------*/
  /** Get the node type manager.
   *  @return the node type manager
   *  @since  2007.06.22 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public TypeMgr getNodeMgr ()
  { return this.nodemgr; }

  /*------------------------------------------------------------------*/
  /** Set the node type manager.
   *  @param  nodemgr the new node type manager
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public void setNodeMgr (TypeMgr nodemgr)
  { this.nodemgr = nodemgr; }

  /*------------------------------------------------------------------*/
  /** Get the edge type manager.
   *  @return the edge type manager
   *  @since  2007.06.22 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public TypeMgr getEdgeMgr ()
  { return this.edgemgr; }

  /*------------------------------------------------------------------*/
  /** Set the edge type manager.
   *  @param  edgemgr the new edge type manager
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public void setEdgeMgr (TypeMgr edgemgr)
  { this.edgemgr = edgemgr; }

}  /* class FreeNtn */
