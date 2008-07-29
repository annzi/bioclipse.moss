/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*----------------------------------------------------------------------
  File    : Notation.java
  Contents: abstract class for notations of attributed graphs
  Author  : Christian Borgelt
  History : 2006.08.12 file created from file Molecule.java
            2006.10.23 parameter graph added to function parse
            2006.10.25 comments in javadoc style added
            2007.03.02 function createNotation added
            2007.06.21 adapted to new classes Atoms and Bonds
            2007.06.22 rewritten to use readers and writers
            2007.07.01 function setTypeMgrs(Notation) added
----------------------------------------------------------------------*/
package moss;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/*--------------------------------------------------------------------*/
/** Class for notations for attributed graphs.
 *  @author Christian Borgelt
 *  @since  2006.08.12 */
/*--------------------------------------------------------------------*/
public abstract class Notation {

  /*------------------------------------------------------------------*/
  /*  instance variables                                              */
  /*------------------------------------------------------------------*/
  /** the reader from which to read the graph description */
  protected Reader reader = null;
  /** the stack  of unread/pushed back characters */
  protected int[]  stack  = null;
  /** the number of unread/pushed back characters */
  protected int    cnt    = 0;

  /*------------------------------------------------------------------*/
  /** Whether this is a line notation (single line description).
   *  <p>A notation that describes an attributed graph in a single line
   *  (like SMILES or SLN) returns <code>true</code>, a notation that
   *  needs multiple lines (like Ctab) returns <code>false</code>.</p>
   *  @return whether this is a line notation
   *  @since  2007.03.04 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract boolean isLine ();

  /*------------------------------------------------------------------*/
  /** Whether this notation has a fixed set of (node and edge) types.
   *  @return whether this notation has a fixed set of types
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract boolean hasFixedTypes ();

  /*------------------------------------------------------------------*/
  /** Get the node type manager.
   *  @return the node type manager
   *  @since  2007.06.21 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract TypeMgr getNodeMgr ();

  /*------------------------------------------------------------------*/
  /** Set the node type manager.
   *  <p>This function should only have an effect if the function
   *  <code>hasFixedTyes()</code> returns <code>false</code>.</p>
   *  @param  nodemgr the new node type manager
   *  @see    #hasFixedTypes()
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract void setNodeMgr (TypeMgr nodemgr);

  /*------------------------------------------------------------------*/
  /** Get the edge type manager.
   *  @return the edge type manager
   *  @since  2007.06.21 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract TypeMgr getEdgeMgr ();

  /*------------------------------------------------------------------*/
  /** Set the edge type manager.
   *  <p>This function should only have an effect if the function
   *  <code>hasFixedTyes()</code> returns <code>false</code>.</p>
   *  @param  edgemgr the new edge type manager
   *  @see    #hasFixedTypes()
   *  @since  2007.06.29 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract void setEdgeMgr (TypeMgr edgemgr);

  /*------------------------------------------------------------------*/
  /** Set the type managers from another notation.
   *  @param  ntn the notation of which to copy the type managers
   *  @since  2007.07.01 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public void setTypeMgrs (Notation ntn)
  {                             /* --- set the type managers */
    this.setNodeMgr(ntn.getNodeMgr());
    this.setEdgeMgr(ntn.getEdgeMgr());
  }  /* setTypeMgrs() */

  /*------------------------------------------------------------------*/
  /** Set the reader to read from.
   *  <p>Also create a small stack to handle pushed back characters.</p>
   *  @param  reader the reader to read from
   *  @since  2007.06.23 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  protected void setReader (Reader reader)
  {                             /* --- init. a molecule notation */
    this.reader = reader;       /* note the reader to read from */
    if (this.stack == null) this.stack = new int[4];
    this.cnt = 0;               /* create/reinit. character stack */
  }  /* setReader() */

  /*------------------------------------------------------------------*/
  /** Read the next character.
   *  @return the character read
   *  @since  2007.06.23 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  protected int read () throws IOException
  {                             /* --- read the next character */
    if (this.cnt > 0) return this.stack[--this.cnt];
    return this.reader.read();  /* return a pushed back character */
  }  /* read() */               /* or actually read a new character */

  /*------------------------------------------------------------------*/
  /** Unread/push back a character.
   *  @param  c the character to push back
   *  @since  2007.06.23 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  protected void unread (int c)
  { if (c >= 0) this.stack[this.cnt++] = c; }

  /*------------------------------------------------------------------*/
  /** Parse a description of an attributed graph.
   *  @param  reader the reader from which to read the description
   *  @return the parsed graph
   *  @throws IOException if a parse error or an i/o error occurs
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract Graph parse (Reader reader) throws IOException;

  /*------------------------------------------------------------------*/
  /** Get the delimiter character.
   *  <p>The delimiter character is the character at which parsing
   *  stopped, but which was not processed by the parser anymore.</p>
   *  @return the delimiter character or
   *          -1 if no delimiter character was read
   *  @since  2007.06.26 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public int getDelim ()
  { return (this.cnt > 0) ? this.stack[this.cnt-1] : -1; }

  /*------------------------------------------------------------------*/
  /** Create a string description of a graph.
   *  @param  graph the graph to describe
   *  @return the created string description
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract String describe (Graph graph);

  /*------------------------------------------------------------------*/
  /** Write a description of an attributed graph.
   *  @param  graph  the graph to write
   *  @param  writer the writer to write to
   *  @since  2007.06.22 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public abstract void write (Graph graph, Writer writer)
    throws IOException;

  /*------------------------------------------------------------------*/
  /** Mark the visits of each node.
   *  <p>In this function the marker of a node is used to determine
   *  the number of labels needed for a node: 1 means that the node
   *  has not been visited yet, 0 means that it has been visited only
   *  once (and thus needs no label) -n, n > 0, means that it has been
   *  visited n+1 times and therefore needs n labels (for n "backward
   *  connections"). The procedure is a depth-first search and assumes
   *  that all nodes have been marked with the value 1 before.</p>
   *  @param  node the current node in the depth-first search
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  protected static void mark (Node node)
  {                             /* --- mark visits of each node */
    int  i;                     /* loop variable */
    Edge edge;                  /* to traverse the edges */

    if (--node.mark < 0) return;/* skip already processed nodes */
    for (i = 0; i < node.deg; i++) {
      edge = node.edges[i];     /* traverse the unprocessed bonds */
      if (edge.mark != 0) continue;
      edge.mark = -1;           /* mark the bond as processed */
      Notation.mark((edge.src != node) ? edge.src : edge.dst);
    }                           /* follow the bond and process */
  }  /* mark() */               /* the atoms recursively */

  /*------------------------------------------------------------------*/
  /** Unmark a connected component.
   *  <p>With this function a connected component is unmarked after
   *  it has been described. The procedure is a simple depth-first
   *  search.</p>
   *  @param  node the current node in the depth-first search
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  protected static void unmark (Node node)
  {                             /* --- unmark a connected component */
    int  i;                     /* loop variable */
    Edge edge;                  /* to traverse the edges */

    if (node.mark < 0) return;  /* skip already processed nodes */
    node.mark = -1;             /* unmark the node */
    for (i = 0; i < node.deg; i++) {
      edge = node.edges[i];     /* traverse the unprocessed bonds */
      if (edge.mark != 0) continue;
      edge.mark = -1;           /* mark the bond as processed */
      Notation.unmark((edge.src != node) ? edge.src : edge.dst);
    }                           /* follow the bond and process */
  }  /* unmark() */             /* the atoms recursively */

  /*------------------------------------------------------------------*/
  /** Create a graph notation corresponding to a given name.
   *  @param  name the name of the notation
   *  @return the created notation
   *  @since  2007.03.02 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static Notation createNotation (String name)
  {                             /* --- create a notation */
    if (name.equalsIgnoreCase("smiles")) return new SMILES();
    if (name.equalsIgnoreCase("sln"))    return new SLN();
    if (name.equalsIgnoreCase("ctab"))   return new Ctab();
    if (name.equalsIgnoreCase("mdl"))    return new Ctab();
    if (name.equalsIgnoreCase("linog"))  return new LiNoG();
    if (name.equalsIgnoreCase("list"))   return new NEList();
    if (name.equalsIgnoreCase("nelist")) return new NEList();
    return null;                /* evaluate the notation name */
  }  /* createNotation() */

}  /* class Notation */
