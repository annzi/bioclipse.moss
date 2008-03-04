/*----------------------------------------------------------------------
  File    : Bonds.java
  Contents: class for a bond type manager
  Author  : Christian Borgelt
  History : 2007.06.20 file created
            2007.06.25 missing double bond added to name table (9)
----------------------------------------------------------------------*/
package moss;

/*--------------------------------------------------------------------*/
/** Class for a bond type manager.
 *  @author Christian Borgelt
 *  @since  2007.06.20 */
/*--------------------------------------------------------------------*/
public class Bonds extends TypeMgr {

  /*------------------------------------------------------------------*/
  /*  constants: bond types                                           */
  /*------------------------------------------------------------------*/
  /** bond type: unknown */
  public static final int UNKNOWN   = -1;
  /** bond type: null bond, that is,
   *  a bond that does not actually connect two atoms */
  public static final int NULL      =  0x0000;
  /** bond type: single */
  public static final int SINGLE    =  0x0001;
  /** bond type: aromatic */
  public static final int AROMATIC  =  0x0007;
  /** bond type: double */
  public static final int DOUBLE    =  0x000f;
  /** bond type: triple */
  public static final int TRIPLE    =  0x0011;

  /*------------------------------------------------------------------*/
  /*  constants: flags and masks                                      */
  /*------------------------------------------------------------------*/
  /** a mask for the type of a bond */
  public static final int BONDMASK  =  0x001f;
  /** a mask, to be anded (bitwise &) with a bond type
   *  in order to remove any distinction of bond types */
  public static final int SAMETYPE  = ~0x001e;
  /** a mask, to be anded (bitwise &) with a bond type
   *  in order to downgrade an aromatic bond to a single bond */
  public static final int DOWNGRADE = ~0x0006;
  /** a mask, to be anded (bitwise &) with a bond type
   *  in order to upgrade an aromatic bond to a double bond */
  public static final int UPGRADE   = ~0x000c;

  /*------------------------------------------------------------------*/
  /*  constants: names                                                */
  /*------------------------------------------------------------------*/
  /** the table of bond names; using the bond type as an index
   *  for this table yields a printable bond description */
  protected static String[] names = {
  /*  0    1    2    3    4    5    6    7  */
     ".", "-", "+", "=", "+", "+", "+", ":",
  /*  8    9   10   11   12   13   14   15  */
     "+", "=", "+", "+", "+", "+", "+", "=",
  /* 16   17   18   19   20   21   22   23  */
     "+", "#", "+", "+", "+", "+", "+", "+",
  /* 24   25   26   27   28   29   30   31  */
     "+", "+", "+", "+", "+", "+", "+", "+"
  };
  /* Plus marks are used for bond type codes that should not occur. */
  /* Note that code 3 denotes an upgraded aromatic bond, code 9 a   */
  /* double bond if aromatic bonds are downgraded to single bonds.  */

  /*------------------------------------------------------------------*/
  /*  class variables                                                 */
  /*------------------------------------------------------------------*/
  /** the bond type manager (only one instance is needed) */
  protected static Bonds bonds;

  /*------------------------------------------------------------------*/
  /** Class initialization.
   *  @since  2006.11.05 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  static {                      /* --- class initialization */
    Bonds.bonds = new Bonds();  /* create the only instance */
  }  /* <clinit> */

  /*------------------------------------------------------------------*/
  /** Create a bond type manager.
   *  @since  2007.06.21 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public Bonds ()
  { }

  /*------------------------------------------------------------------*/
  /** Add a bond type.
   *  <p>The set of bond types is fixed and cannot be extended.
   *  Therefore this function behaves exactly like the function
   *  <code>getCode()</code> and returns -1 for an unknown name.</p>
   *  @param  name the name of the bond
   *  @return the code of the bond or -1 if the name is no bond
   *  @see    #getCode(String)
   *  @since  2007.06.21 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public int add (String name)
  { return this.getCode(name); }

  /*------------------------------------------------------------------*/
  /** Map a bond name to the corresponding code.
   *  @param  name the name of the bond
   *  @return the code of the bond or
   *          -1 if the name is not a bond description
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public int getCode (String name)
  {                             /* --- get a bond code */
    if (name.length() != 1) return -1;
    switch (name.charAt(0)) {   /* check for exactly one character, */
      case  '.': return NULL;   /* then evaluate this character */
      case  '-': return SINGLE;
      case  '/': return SINGLE;
      case '\\': return SINGLE;
      case  ':': return AROMATIC;
      case  '=': return DOUBLE;
      case  '#': return TRIPLE;
      default  : return -1;
    }
  }  /* getCode() */

  /*------------------------------------------------------------------*/
  /** Map a code to the corresponding bond name.
   *  @param  code the code of the bond
   *  @return the name of the bond or
   *          -1 if the name is not a bond description
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  @Override
public String getName (int code)
  { return Bonds.names[code & BONDMASK]; }

  /*------------------------------------------------------------------*/
  /** Get the only/default instance.
   *  @return the only/default instance of this class
   *  @since  2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static Bonds getBonds ()
  { return Bonds.bonds; }

  /*------------------------------------------------------------------*/
  /** Extract the raw bond type from a bond type.
   *  <p>The raw bond type is only part of the type of a bond.
   *  The full type of a bond also includes a ring flag.</p>
   *  @param  type the type from which to extract the bond type
   *  @return the bond type without a possible ring flag
   *  @since  2006.10.31/2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static int getBond (int type)
  { return type & BONDMASK; }

  /*------------------------------------------------------------------*/
  /** Get the name of the bond type.
   *  @param  type the bond type for which to get the name
   *  @return the name of the bond type
   *  @since  2006.30.31/2007.06.20 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static String getBondName (int type)
  { return Bonds.names[type & BONDMASK]; }

  /*------------------------------------------------------------------*/
  /** Convert Kekul&eacute; representations to true aromatic rings.
   *  <p>In a Kekul&eacute; representation an aromatic ring with 6
   *  edges is coded with alternating single and double edges. In
   *  this function such Kekul&eacute; representations are found and
   *  turned into true aromatic rings (actual aromatic bonds).</p>
   *  @param  mol the molecule in which to convert aromatic rings
   *  @return the number of converted rings
   *  @since  2003.07.11 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static int aromatize (Graph mol)
  {                             /* --- convert Kekul\'e to aromatic */
    int    i, k, n, cnt = 0;    /* loop variables, counter */
    long   all, cur;            /* buffer for ring flags */
    Node   atom;                /* to traverse the atoms */
    Edge   bond, b = null;      /* to traverse the bonds */
    Edge[] ring = new Edge[6];  /* bonds of an aromatic ring */

    mol.markRings(6, 6);        /* mark rings of size 6 */
    for (i = mol.edgecnt; --i >= 0; )
      mol.edges[i].mark = 1;    /* mark all bonds as unprocessed */
    for (i = mol.edgecnt; --i >= 0; ) {
      bond = mol.edges[i];      /* traverse the unprocessed bonds */
      if (bond.mark <= 0) continue;
      if (Bonds.getBond(bond.type) != Bonds.SINGLE)
        continue;               /* process only single bonds */
      all = bond.getRings();    /* get the ring flags and */
      ring[0] = bond;           /* note the first bond of the ring */
      for (cur = 1; all != 0; cur <<= 1) {
        if ((all & cur) == 0)   /* traverse the rings */
          continue;             /* the bond is part of */
        all &= ~cur;            /* remove processed ring flag */
        bond = ring[n = 0]; atom = bond.dst;
        do {                    /* collect the ring bonds */
          for (k = atom.deg; --k >= 0; ) {
            b = atom.edges[k];  /* traverse the bonds of the atom */
            if ((b != bond) && ((b.flags & cur) != 0)) break;
          }                     /* find the next ring bond */
          ring[++n] = bond = b; bond.mark = 0;
          atom = (bond.src != atom) ? bond.src : bond.dst;
        } while (atom != ring[0].src);
        if ((Bonds.getBond(ring[1].type) != Bonds.DOUBLE)
        ||   (ring[0].type != ring[2].type)
        ||   (ring[2].type != ring[4].type)
        ||   (ring[1].type != ring[3].type)
        ||   (ring[3].type != ring[5].type))
          continue;             /* check for Kekul\'e representation */
        cnt++;                  /* count the aromatic ring */
        for (k = 6; --k >= 0; ) /* traverse the ring found and */
          ring[k].mark = -1;    /* specially mark all bonds */
      }                         /* (actual conversion is done later */
    }                           /* in order to avoid interference) */
    for (i = mol.edgecnt; --i >= 0; ) {
      bond = mol.edges[i];      /* traverse the marked bonds */
      if (bond.mark >= 0) continue;
      bond.type      = Bonds.AROMATIC;
      bond.src.type |= Atoms.AROMATIC;
      bond.dst.type |= Atoms.AROMATIC;
    }                           /* set the bond types to aromatic */
    mol.markRings(0, 0);        /* unmark the rings again */
    return cnt;                 /* return the number of rings */
  }  /* aromatize() */

  /*------------------------------------------------------------------*/
  /** Main function for testing some basic functionality.
   *  <p>It is tried to parse the first command line argument
   *  as a bond description and the resulting code is reported.</p>
   *  @param  args the command line arguments
   *  @since  2007.06.21 (Christian Borgelt) */
  /*------------------------------------------------------------------*/

  public static void main (String args[])
  {                             /* --- main function for testing */
    if (args.length != 1) {     /* if wrong number of arguments */
      System.err.println("usage: java moss.Bonds <string>");
      return;                   /* print a usage message */
    }                           /* and abort the program */
    int c = Bonds.getBonds().getCode(args[0]);
    System.out.print("code(\"" +args[0] +"\") = ");
    System.out.println(c);      /* convert from name to code */
    if (c < 0) return;          /* check for a valid code */
    String s = Bonds.getBonds().getName(c);
    System.out.print("name(" +c +") = ");
    System.out.println(s);      /* convert from code to name */
  }  /* main() */

}  /* class Bonds */
