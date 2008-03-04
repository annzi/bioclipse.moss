/*----------------------------------------------------------------------
  File    : SMILES.java
  Contents: class for the Simplified Molecular Input Line Entry System
  Authors : Christian Borgelt
  History : 2006.08.16 file created from file SmilesTokenizer.java
            2006.10.23 parameter graph added to function parse
            2006.10.25 comments in javadoc style added
            2006.10.27 output of special atom types modified
            2006.10.31 adapted to refactored classes
            2006.11.05 map from element names to element codes added
            2006.11.12 shorthand hydrogen reading and writing added
            2007.02.21 '\' bonds added, forward references added
            2007.03.02 ensured lowercase output of aromatic atoms
            2007.03.04 function isLine() added
            2007.03.19 bug in label output fixed (missing '%')
            2007.06.21 adapted to new classes Atoms and Bonds
            2007.06.22 rewritten to use readers and writers
            2007.06.26 charge parsing improved
            2007.07.02 buffers for creating descriptions added
            2007.07.05 loop check added (it must be src != dst)
----------------------------------------------------------------------*/
package moss;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/*--------------------------------------------------------------------*/
/** Class for the Simplified Molecular Input Line Entry System
 *  (SMILES, Daylight, Inc.).
 *  @author Christian Borgelt
 *  @since  2006.08.12 */
/*--------------------------------------------------------------------*/
public class SMILES extends MoleculeNtn {
  
  /*------------------------------------------------------------------*/
  /*  constants                                                       */
  /*------------------------------------------------------------------*/
  /** the elements that need no brackets */
  private static final int[] NO_BRACKETS = {
    Atoms.BORON    /* B  */,  Atoms.CARBON     /* C  */,
    Atoms.NITROGEN /* N  */,  Atoms.OXYGEN     /* O  */,
    Atoms.FLOURINE /* F  */,  Atoms.PHOSPHORUS /* P  */,
    Atoms.SULFUR   /* S  */,  Atoms.CHLORINE   /* Cl */,
    Atoms.BROMINE  /* Br */,  Atoms.IODINE     /* I  */,
    Atoms.UNKNOWN
  };
  /** the elements that become lowercase in aromatic rings */
  private static final int[] LOWER_AROM = {
    Atoms.BORON      /* B */, Atoms.CARBON /* C */,
    Atoms.NITROGEN   /* N */, Atoms.OXYGEN /* O */,
    Atoms.PHOSPHORUS /* P */, Atoms.SULFUR /* S */
  };

  /*------------------------------------------------------------------*/
  /*  instance variables                                              */
  /*------------------------------------------------------------------*/
  /** the parsed molecule */
  private Graph        mol;
  /** the label to node index map */
  private int[]        labels;
  /** the bond types of forward references */
  private int[]        bonds;
  /** the atom type recoder for a molecule to describe */
  private Recoder      coder;
  /** the buffer for creating atom descriptions */
  private StringBuffer buf;

  /*------------------------------------------------------------------*/
  /** Create a SMILES notation object.
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public SMILES ()
  {                             /* --- create a SMILES object */ 
    this.labels = new int[100]; /* create a label to node index map */
    this.bonds  = new int[100]; /* and a buffer for bond types */
    this.desc = this.buf = null;/* clear the description buffers */
  }  /* SMILES() */

  /*------------------------------------------------------------------*/
  /** Whether this is a line notation (single line description).
   *  @return <code>true</code>, because SMILES is a line notation
   *  @since  2007.03.04 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public boolean isLine ()
  { return true; }

  /*------------------------------------------------------------------*/
  /** Read an atom type, including shorthand hydrogens.
   *  @return the type of the next atom
   *  @throws IOException if a parse error or an i/o error occurs
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  private int readAtom () throws IOException
  {                             /* --- read an atom description */
    int k;                      /* loop variable, buffer */
    int c, n;                   /* current and next character */
    int atom = 0;               /* type code of an atom */

    /* At this point an opening bracket '[' has already been read. */
    do {                        /* skip a possible leading number */
      c = this.read();          /* (isotope index before element) */
      if (c < 0) throw new IOException("missing element after '['");
    } while ((c >= '0') && (c <= '9'));

    /* --- get the element code --- */
    if ((c >= 'a') && (c <= 'z')) {
      atom = Atoms.AROMATIC;    /* if the element name starts */
      c   += 'A' -'a';          /* with a lowercase letter, it is */
    }                           /* involved in an aromatic bond */
    if ((c < 'A') || (c > 'Z')) /* check for a letter */
      throw new IOException("invalid character '"+(char)c+"' ("+c+")");
    n = this.read();            /* get second element character */
    if ((n >= 'A') && (n <= 'Z') && (n != 'H'))
      n += 'a' -'A';            /* make letter lowercase */
    if ((n < 'a') || (n > 'z')){/* if single letter element */
      k = Atoms.map[c-'A'][26]; /* get the element code */
      if (k <= 0) throw new IOException("invalid element '"
                                        +(char)c +"'");
      this.unread(n); }         /* push back the unused character */
    else {                      /* if double letter element */
      k = Atoms.map[c-'A'][n-'a'];  /* get the element code */
      if (k <= 0) throw new IOException("invalid element '"
                                        +(char)c +(char)n +"'");
    }                           /* (both characters were used) */
    atom |= k;                  /* add element code and hydrogens */
    atom |= Atoms.codeHydros(k = this.getHydros());

    /* --- add a possible charge --- */
    c = this.read();            /* read the next character */
    if ((c == '+') || (c == '-')) {
      n = this.read();          /* if a sign follows, check */
      if (n == c) n = '2';      /* for another sign or a digit */
      else if ((n <= '0') || (n > '9')) { this.unread(n); n = '1'; }
      atom |= Atoms.codeCharge((c == '+') ? n -'0' : '0' -n);
    }                           /* add the charge to the code */

    /* --- skip additional information --- */
    while ((c != ']') && (c >= 0))
      c = this.read();          /* skip characters until ']' */
    if (c < 0) throw new IOException("missing ']'");
    return atom;                /* return the atom code */
  }  /* readAtom() */

  /*------------------------------------------------------------------*/
  /** Recursive function to parse (a branch of) a molecule.
   *  @param  src the index of the source atom for the next bond
   *  @return whether a started branch was closed
   *          (i.e. whether the last character was a ')')
   *  @throws IOException if a parse error or an i/o error occurs
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  private boolean parse (int src) throws IOException
  {                             /* --- parse a molecule description */
    int c, n;                   /* current and next character */
    int a, b, i = 0;            /* atom and bond types, buffer */
    int dst;                    /* index of destination atom */

    b = Bonds.UNKNOWN;          /* initialize the bond type */
    while (true) {              /* parse loop for a branch */
      a = dst = -1;             /* clear the atom type and index */
      c = this.read();          /* read the next character */
      if (c < 0) return false;  /* if at end, abort indicating no ')' */
      switch (c) {              /* get and evaluate next character */

        /* -- branches -- */
        case ')':               /* if at the end of a branch */
          if (b != Bonds.UNKNOWN)  /* check for a preceding bond */
            throw new IOException("unexpected ')'");
          return true;          /* abort indicating a ')' */
        case '(':               /* if at the start of a branch */
          if ((src < 0) || (b != Bonds.UNKNOWN))
            throw new IOException("unexpected '('");
          if (!this.parse(src)) /* recursively parse the branch */
            throw new IOException("missing ')'");
          break;                /* check for a closing ')' */

        /* -- labels -- */
        case '%': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
          if (c != '%')         /* if one digit label */
            i = c -'0';         /* compute the value of the digit */
          else {                /* if two digit label */
            c = this.read();    /* read the next two characters */
            n = this.read();    /* and check for a number */
            if ((c < '1') || (c > '9') || (n < '0') || (n > '9'))
              throw new IOException("invalid label %"+(char)c+(char)n);
            i = (c -'0') *10 +(n -'0');
          }                     /* compute the label value */
          dst = this.labels[i]; /* get index of destination atom */
          if ((b             != Bonds.UNKNOWN)
          &&  (this.bonds[i] != Bonds.UNKNOWN)
          &&  (this.bonds[i] != b))    /* check the bond type(s) */
            throw new IOException("duplicate bond at label " +i);
          if (src < 0)          /* check for a proper label */
            throw new IOException("unexpected label " +i);
          if (dst < 0) {        /* if the destination is not known, */
            this.labels[i] = src; /* store atom index and bond type */
            this.bonds [i] = b; b = Bonds.UNKNOWN; continue; }
          else {                /* if the destination is known, */
            this.labels[i] = -1;/* remove the stored atom index */
            if (b == Bonds.UNKNOWN) b = this.bonds[i];
            this.bonds[i] = Bonds.UNKNOWN;
          }                     /* turn a forward  reference */
          break;                /* into a backward reference */

        /* -- bonds -- */
        case  '.': b = Bonds.NULL;     break;
        case  '-': b = Bonds.SINGLE;   break;
        case  '/': b = Bonds.SINGLE;   break;
        case '\\': b = Bonds.SINGLE;   break;
        case  ':': b = Bonds.AROMATIC; break;
        case  '=': b = Bonds.DOUBLE;   break;
        case  '#': b = Bonds.TRIPLE;   break;

        /* -- atoms -- */
        case 'b': a = Atoms.BORON      | Atoms.AROMATIC; break;
        case 'B': c = this.read();
                  if (c == 'r') {    a = Atoms.BROMINE;  break; }
                  a = Atoms.BORON; this.unread(c);       break;
        case 'c': a = Atoms.CARBON     | Atoms.AROMATIC; break;
        case 'C': c = this.read();
                  if (c == 'l') {    a = Atoms.CHLORINE; break; }
                  a = Atoms.CARBON; this.unread(c);      break;
        case 'n': a = Atoms.NITROGEN   | Atoms.AROMATIC; break;
        case 'N': a = Atoms.NITROGEN;                    break;
        case 'o': a = Atoms.OXYGEN     | Atoms.AROMATIC; break;
        case 'O': a = Atoms.OXYGEN;                      break;
        case 'F': a = Atoms.FLOURINE;                    break;
        case 'p': a = Atoms.PHOSPHORUS | Atoms.AROMATIC; break;
        case 'P': a = Atoms.PHOSPHORUS;                  break;
        case 's': a = Atoms.SULFUR     | Atoms.AROMATIC; break;
        case 'S': a = Atoms.SULFUR;                      break;
        case 'I': a = Atoms.IODINE;                      break;
        case 'H': a = Atoms.HYDROGEN;                    break;
        case '[': a = this.readAtom();                   break;

        default : throw new IOException("invalid character '"
                                        +(char)c +"' (" +c +")");
      }                         /* catch all other characters */
      if ((src < 0) && (b != Bonds.UNKNOWN))
        throw new IOException("unexpected bond");
      if    (dst >= 0) a   = this.mol.nodes[dst].type;
      else if (a >= 0) dst = this.mol.addNode(Atoms.removeHydros(a));
      else continue;            /* complete the atom information */
      if (src >= 0) {           /* if there is no source, skip bond */
        if (b == Bonds.UNKNOWN){/* if the bond type is unknown */
          i = a & this.mol.nodes[src].type & Atoms.AROMATIC;
          b = (i != 0) ? Bonds.AROMATIC : Bonds.SINGLE;
        }                       /* get a default bond type */
        if (b != Bonds.NULL) {  /* if the bond is not null */
          if (src == dst)       /* and no loop (self connection) */
            throw new IOException("loop bond (source = destination)");
          this.mol.addEdge(src, dst, b);
        }                       /* add the bond to the molecule */
        if (b == Bonds.AROMATIC) {   /* if the bond is aromatic */
          this.mol.nodes[src].type |= Atoms.AROMATIC;
          this.mol.nodes[dst].type |= Atoms.AROMATIC;
        }                       /* set aromatic flag in atoms */
        b = Bonds.UNKNOWN;      /* clear the bond type */
      }                         /* for the next step */
      for (i = Atoms.getHydros(a); --i >= 0; ) {
        a = this.mol.addNode(Atoms.HYDROGEN);
        this.mol.addEdge(dst, a, Bonds.SINGLE);
      }                         /* add shorthand hydrogens */
      if (dst > src) src = dst; /* replace the source atom */
    }                           /* (for the next loop) */
  }  /* parse() */
  
  /*------------------------------------------------------------------*/
  /** Parse the description of a molecule.
   *  @param  reader the reader to read from
   *  @return the parsed molecule
   *  @throws IOException if a parse error or an i/o error occurs
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public Graph parse (Reader reader) throws IOException
  {                             /* --- parse a molecule description */
    this.setReader(reader);     /* note the reader */
    for (int i = this.labels.length; --i >= 0; ) {
      this.bonds [i] = Bonds.UNKNOWN;
      this.labels[i] = -1;      /* clear the bonds buffer */
    }                           /* and the label array */
    this.mol = new Graph(this); /* create a new molecule */
    if (this.parse(-1))         /* recursively parse the molecule */
      throw new IOException("superfluous ')'");
    for (int i = this.bonds.length; --i >= 0; ) {
      if (this.bonds[i] != Bonds.UNKNOWN)
        throw new IOException("invalid reference to label " +i);
    }                           /* check unsatisfied forward refs. */
    this.mol.opt();             /* optimize memory usage and */
    return this.mol;            /* return the created molecule */
  }  /* parse() */

  /*------------------------------------------------------------------*/
  /** Create a description of an atom.
   *  @param  type the type of the atom
   *  @return the description of the atom
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  private String describe (int type)
  {                             /* --- describe an atom */
    int    i, h;                /* loop variable, number of hydrogens */
    int    t, c;                /* element type and charge */
    String e;                   /* element name of the atom */

    if (type == Node.ANY)         return Atoms.getWildcard();
    if ((type & Node.CHAIN) != 0) return Atoms.getChainName();
    t = Atoms.getElem(type);    /* get the element type */
    e = Atoms.getElemName(t);   /* and the name of the element */
    if ((type & Atoms.AROMATIC) != 0) { /* if the atom is aromatic */
      for (i = LOWER_AROM.length; --i >= 0; )
        if (t == LOWER_AROM[i]) { e = e.toLowerCase(); break; }
    }                           /* it may have to be in lower case */
    c = Atoms.getCharge(type);  /* get the charge of the atom */
    h = Atoms.getHydros(type);  /* and the number of hydrogens */
    if ((c == 0) && (h == 0)) { /* if no charge and no hydrogens */
      for (i = NO_BRACKETS.length; --i >= 0; )
        if (t == NO_BRACKETS[i]) return e;
    }                           /* check whether brackets are nec. */
    if (this.buf == null)       /* create a description buffer */
      this.buf = new StringBuffer();
    this.buf.setLength(0);      /* clear the description buffer */
    this.buf.append('[');       /* store the element name and */
    this.buf.append(e);         /* add the shorthand hydrogens */
    if      (h > 0) { this.buf.append("H");
                      if (h >  1) this.buf.append(h); }
    if      (c > 0) { this.buf.append("+");
                      if (c > +1) this.buf.append(+c); }
    else if (c < 0) { this.buf.append("-");
                      if (c < -1) this.buf.append(-c); }
    this.buf.append("]");       /* add charge and closing bracket */
    return this.buf.toString(); /* return the created description */
  }  /* describe() */

  /*------------------------------------------------------------------*/
  /** Recursive function to create a description.
   *  @param  atom the current atom
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  private void out (Node atom)
  {                             /* --- recursive part of output */
    int  i, k, n;               /* loop variables, number of branches */
    int  p;                     /* preceding label, exchange buffer */
    Edge b;                     /* to traverse the bonds */
    Node d;                     /* destination of a bond */

    k = atom.type;              /* get and decode chemical element */
    if (this.coder != null) k = this.coder.decode(k);
    if (!Atoms.isAromatic(k)) { /* if the aromatic flag is not set */
      for (i = atom.deg; --i >= 0; )
        if (Bonds.getBond(atom.edges[i].type) == Bonds.AROMATIC) {
          k |= Atoms.AROMATIC; break; }
    }                           /* set the aromatic flag if needed */
    k |= Atoms.codeHydros(MoleculeNtn.getHydros(atom, this.coder));
    this.desc.append(this.describe(k));
                                /* append the atom description */
    for (p = k = 0, i = n = atom.mark; ++i <= 0; ) {
      while ((++k < this.labels.length) && (this.labels[k] >= 0))
        ;                       /* find the next free label */
      this.labels[k] = p;       /* note the preceding label */      
      if (k >= 10) this.desc.append('%');
      this.desc.append(p = k);  /* append the label and */
    }                           /* switch to the next one */
    atom.mark = p;              /* store the last label in the atom */
    for (i = atom.deg; --i >= 0; ) {
      b = atom.edges[i];        /* traverse unproc. backward bonds */
      if (b.mark == 0) continue;    /* that lead back to some atom */
      d = (b.src != atom) ? b.src : b.dst;
      if (d.mark <= 0) { n++; continue; }
      b.mark = 0;               /* mark the bond as processed */
      this.desc.append(Bonds.getBondName(Bonds.getBond(b.type)));
      if (d.mark >= 10) this.desc.append('%');
      this.desc.append(d.mark); /* append backward connection */
      p = this.labels[d.mark];  /* get the next label of the atom, */
      this.labels[d.mark] = -1; /* unmark the used label, */
      d.mark = p;               /* and note the next label */
    }                           /* (count down the labels) */
    /* Here n contains the number of branches leading away from the  */
    /* atom, which is the number of bonds leading to unvisited atoms */
    /* minus the number of bonds that lead back to the atom through  */
    /* labels (obtained from the original value of atom.mark).       */
    for (i = 0; i < atom.deg; i++) {
      b = atom.edges[i];        /* traverse the unprocessed bonds */
      if (b.mark == 0) continue;
      b.mark = 0;               /* mark the bond as processed */
      if (--n > 0)              /* start branch if it is not the last */
        this.desc.append("(");  /* and append the bond description */
      this.desc.append(Bonds.getBondName(Bonds.getBond(b.type)));
      this.out((b.src != atom) ? b.src : b.dst);
      if (n > 0)                /* process the branch recursively and */
        this.desc.append(")");  /* terminate it if it is not the last */
    }                           /* (last branch needs no parantheses) */
  }  /* out() */

  /*------------------------------------------------------------------*/
  /** Create a string description of a molecule.
   *  @param  mol the molecule to describe
   *  @return the created string description
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public String describe (Graph mol)
  {                             /* --- create a string description */
    int  i, n, t;               /* loop variable, counter, buffer */
    Node a, d;                  /* to traverse the atoms */
    Edge e;                     /* to access edges from hydrogens */

    if (this.desc == null)      /* create a description buffer */
      this.desc = new StringBuffer();
    this.desc.setLength(0);     /* clear the description buffer */
    for (i = this.labels.length; --i > 0; )
      this.labels[i] = -1;      /* clear all labels (mark as unused) */
    this.labels[0] = 0;         /* and init. the label counter */
    this.coder = mol.coder;     /* note the atom type recoder */
    for (i = mol.nodecnt; --i >= 0; )
      mol.nodes[i].mark = 1;    /* mark the atoms of the molecule */
    for (i = mol.edgecnt; --i >= 0; )
      mol.edges[i].mark = 0;    /* mark the bonds of the molecule */
    for (i = n = 0; i < mol.nodecnt; i++) {
      a = mol.nodes[i];         /* traverse the unprocessed atoms */
      if (a.mark < 0) continue; /* (unprocessed connected components) */
      t = a.type;               /* check the atom type */
      if (mol.coder != null) t = mol.coder.decode(t);
      if ((a.deg == 1)          /* check possible shorthand hydrogen */
      &&  (Atoms.getElem(t) == Atoms.HYDROGEN)) {
        e = a.edges[0];         /* get the connecting edge */
        d = (e.src != a) ? e.src : e.dst;
        t = d.type;             /* get the destination atom type */
        if (mol.coder != null) t = mol.coder.decode(t);
        if ((Bonds.getBond(e.type) == Bonds.SINGLE)
        &&  (Atoms.getElem(t)      != Atoms.HYDROGEN)) continue;
      }                         /* skip shorthand hydrogens */
      if (n++ > 0)              /* connect components by a null bond */
        this.desc.append(Bonds.getBondName(Bonds.NULL));
      Notation.mark(a);         /* mark the visits of each atom, */
      this.out(a);              /* output a connected component, */
      Notation.unmark(a);       /* and clear the atom markers */
    }
    return this.desc.toString();/* return the created description */
  }  /* describe() */

  /*------------------------------------------------------------------*/
  /** Main function for testing basic functionality.
   *  <p>It is tried to parse the first argument as a SMILES description
   *  of a molecule. If this is successful, the parsed molecule is
   *  printed using the function <code>describe()</code>.</p>
   *  @param  args the command line arguments
   *  @since  2006.08.12 (Christian Borgelt) */
  /*------------------------------------------------------------------*/
  
  public static void main (String args[])
  {                             /* --- main function for testing */
    if (args.length != 1) {     /* if wrong number of arguments */
      System.err.println("usage: java moss.SMILES <SMILES string>");
      return;                   /* print a usage message */
    }                           /* and abort the program */
    try {                       /* try to parse the description */
      Notation ntn = new SMILES();    /* with a SMILES notation */
      Graph    mol = ntn.parse(new StringReader(args[0]));
      System.out.println(ntn.describe(mol)); }
    catch (IOException e) {     /* catch and report parse errors */
      System.err.println(e.getMessage()); }
  }  /* main() */
  
}  /* class SMILES */
