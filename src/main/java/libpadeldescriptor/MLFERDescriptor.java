/*
 *  $RCSfile$
 *  $Author: yapchunwei $
 *  $Date: 2008-07-07 14:50:01 +0800 (Tue, 07 Jul 2008) $
 *  $Revision: 1 $
 *
 *  Copyright (C) 2004-2007  Yap Chun Wei <yapchunwei@users.sourceforge.net>
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package libpadeldescriptor;


import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleArrayResultType;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 * Molecular linear free energy relation.
 * <p/>
 * The code currently computes the descriptors for molecular linear free
 * energy relation. 
 * Platts JA, Butina D, Abraham MH, Hersey A. Estimation of molecular free energy relation descriptors using a group contribution approach. J Chem Inf Comput Sci. 1999 30/8/01;39(5):835-45.
 * <p/>
 * The order of the values returned is
 * <ol>
 * <li>MLFER_A
 * <li>MLFER_B
 * <li>MLFER_S
 * <li>MLFER_E
 * <li>MLFER_L
 * </ol>
 * <p/>
 *
 * @author Yap Chun Wei
 * @cdk.created 2008-07-07
 * @cdk.module qsarmolecular
 * @cdk.svnrev $Revision: 1 $
 * @cdk.set qsar-descriptors
 * @cdk.dictref qsar-descriptors:MLFER
 * @cdk.keyword molecular type MLFER descriptor
 * @cdk.keyword descriptor
 */
public class MLFERDescriptor implements IMolecularDescriptor {
    
    public static final String[] names = {
                                            "MLFER_A",
                                            "MLFER_BH",
                                            "MLFER_BO",
                                            "MLFER_S",
                                            "MLFER_E",
                                            "MLFER_L"
                                         };
    
    private static String[] AFragments = {
                                            "[C][OX2H]",	// -OH (connected to aliphatic)
                                            "[c][OX2H]",	// -OH (phenol)
                                            "[C][NX3;H2]",	// -NH2 (connected to aliphatic)
                                            "[c][NX3;H2;!$(NC=O)]",	// -NH2 (aniline)
                                            "[C][NX3;H1;!R][C]",	// >NH (connected to aliphatic, noncyclic)
                                            "[C][NX3;H1;R][C]",	// >NH (connected to aliphatic, cyclic)
                                            "[c][NX3;H1;!$(NC=O)][C]",	// >NH (aniline)
                                            "[c][nX3;H1][c]",	// >NH (pyrrole)
                                            "[CX3](=O)[OX1H0-,OX2H1]",	// -CO2H (carboxylic acid)
                                            "[CX3](=[OX1])[NX3;H2]",	// -CONH2 (primary amide)
                                            "[CX3](=[OX1])[NX3;H1][C]",	// -CONH- (secondary amide, aliphatic)
                                            "[CX3](=[OX1])[NX3;H1][c]",	// -CONH- (aromatic amide)
                                            "[$([SX4](=[OX1])(=[OX1])([!O])[NH,NH2,NH3+]),$([SX4+2]([OX1-])([OX1-])([!O])[NH,NH2,NH3+])]",	// -SO2NH (primary or secondary)
                                            "[NX3;H1]C(=[OX1])[NX3;H1]",	// -NHCONH- (urea)
                                            "[NX3;H0]C(=[OX1])[NX3;H1]",	// >NCONH- (urea)
                                            "[NX3;H1]C(=[OX1])O",	// -NHCOO- (carbamate)
                                            "[NX3;H1]C(=N)[NX3;H0]",	// -NHC(=N)N< (guanidine)
                                            "[C]#[CH]",	// #CH (alkyne)
                                            "P[OH,O-]",	// -P(OH)- (phosphoric acid)
                                            "[CH][F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)]",	// >CHX (X defined as halogen, NO2, C#N or CF3)
                                            "[CH]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)]",	// -CHX2 (X defined as halogen, NO2, C#N or CF3)
                                            "[CX4]([CX3](=O)[OX1H0-,OX2H1])[CX4][CX3](=O)[OX1H0-,OX2H1]",	// >C(CO2H)C(CO2H)< (1,2-diacid)
                                            "[CX4]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[CX3](=O)[OX1H0-,OX2H1]",	// >C(X)CO2H (X defined as halogen, NO2, C#N or CF3)
                                            "[CX4]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[OH]",	// >C(X)OH (X defined as halogen, NO2, C#N or CF3)
                                            "[CX4]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[CX4][OH]",	// >CX-C(OH)< (X defined as halogen, NO2, C#N or CF3)
                                            "[nX3;H1]:n",	// nH:x (pyrazole type)
                                            "[nX3;H1]:c:n",	// nH:c:x (imidazole type)
                                            "[OX2;H1]CC[O,N]",	// H-bond 1
                                            "[OX2;H1]C[C,N]=[O,S]",	// H-bond 2
                                            "[OX2;H1]c1ccccc1[O,NX3]",	// H-bond 3
                                            "[OX2;H1]c1ccccc1C=[O,S]",	// H-bond 4
                                            "[OX2;H1]c1ccccc1[$([NX3](=O)=O),$([NX3+](=O)[O-])]",	// H-bond 5
                                            "[NH,NH2,NH3+]CC[O,N]",	// H-bond 6
                                            "[NH,NH2,NH3+]c1ccccc1[O,N]",	// H-bond 7
                                            "[NH,NH2,NH3+]c1ccccc1[C,N]=[O,S]",	// H-bond 8
                                            "[OX2H]c1ccccc1[Cl,Br,I]",	// H-bond 9
                                            "[OX1]=[C,c]~[C,c]C[OH]",	// H-bond 10
                                            "[OH]c1cccc2cccnc12",	// 8-OH quinoline (peri interaction)
                                            "[OH]c1cc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])ccc1",	// 3-X phenol (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[OH]c1ccc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])cc1",	// 4-X phenol (para interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[NH,NH2,NH3+]c1cc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])ccc1",	// 3-X aniline (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[NH,NH2,NH3+]c1ccc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])cc1",	// 4-X aniline (para interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[CX3](=O)([OX1H0-,OX2H1])c1cc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])ccc1",	// 3-X benzoic acid (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[CX3](=O)([OX1H0-,OX2H1])c1ccc([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])cc1",	// 4-X benzoic acid (para interaction, X defined as halogen, NO2, C#N or CF3)
                                            "[OH]c1c([CX4])cccc1[CX4]",	// 2,6-dialkyl phenol
                                            "[NH,NH2,NH3+]c1c([CX4])cccc1[CX4]",	// 2,6-dialkyl aniline
                                            "[OH]c1c(C[F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])cccc1",	// 2-CX phenol
                                            "[OH]c1cc([CX3](=O)[OX1H0-,OX2H1])ccc1",	// 3-CO2H phenol
                                            "[OH]c1ccc([CX3](=O)[OX1H0-,OX2H1])cc1",	// 4-CO2H phenol
                                            "[OH]c1cc([$([CH](=O)),$(C(=O)C)])ccc1",	// 3-C=O phenol
                                            "[OH]c1ccc([$([CH](=O)),$(C(=O)C)])cc1"	// 4-C=O phenol
                                         };  
  
    private static String[] BSELFragments = {
                                            "[CX4H3]",	// -CH3 (sp3)
                                            "[CX4H2]",	// >CH2 (sp3)
                                            "[CX4H1]",	// >CH- (sp3)
                                            "[CX4H0]",	// >C< (sp3)
                                            "*=[CX3H2]",	// =CH2 (sp2)
                                            "[$(*=[CX3H1]),$([cX3H1](a)a)]",	// =CH- (sp2 or aromatic)
                                            "[$(*=[CX3H0]),$([cX3H0](a)(a)A)]",	// =C< (sp2 or nonfused aromatic)
                                            "c(a)(a)a",	// C (fused aromatic)
                                            "*#C",	// #C (sp)
                                            "[C][NX3;H2]",	// -NH2 (sp3, connected to aliphatic)
                                            "[c][NX3;H2]",	// -NH2 (sp3, connected to aromatic)
                                            "[C][NX3;H1][C]",	// >NH (sp3, connected to aliphatic)
                                            "[c][NX3;H1]",	// >NH (sp3, connected to aromatic)
                                            "[c][nX3;H1][c]",	// >NH (pyrrole)
                                            "[C][NX3;H0](C)[C]",	// >N- (sp3, connected to aliphatic)
                                            "[c][NX3;H0](C)[C]",	// >N- (sp3, connected to aromatic)
                                            "[c][nX3;H0][c]",	// >N- (pyrrole)                                            
                                            "*=[Nv3;!R]",	// =N (sp2, noncyclic)
                                            "*=[Nv3;R]",	// =N (sp2, cyclic)
                                            "[nX2H0,nX3H1+](a)a",	// =N- (pyridine)
                                            "N#C[A;!#1]",	// N#C- (sp, connected to aliphatic)
                                            "N#C[a;!#1]",	// N#C- (sp, connected to aromatic)
                                            "[$([A;!#1][NX3](=O)=O),$([A;!#1][NX3+](=O)[O-])]",	// -NO2 (connected to aliphatic)
                                            "[$([a;!#1][NX3](=O)=O),$([a;!#1][NX3+](=O)[O-])]",	// -NO2 (connected to aromatic)
                                            "[$([NX3](=[OX1])(=[OX1])O),$([NX3+]([OX1-])(=[OX1])O)]",	// -ONO2 (nitrate)
                                            "[OH]",	// -OH (any)
                                            "[OX2;H0;!R]",	// -O- (sp3, noncyclic)
                                            "[OX2;H0;R]",	// -O- (sp3, cyclic)
                                            "[oX2](a)a",	// -O- (aromatic)
                                            "*=O",	// =O (sp2)
                                            "[SX2](*)*",	// -S- (sp3)
                                            "[sX2](a)a",	// -S- (aromatic)
                                            "*=[SX1]",	// =S (sp2)
                                            "*=[SX3]",	// >S= (sp2)
                                            "[$([#16X4](=[OX1])(=[OX1])([!#8])[OX2H0]),$([#16X4+2]([OX1-])([OX1-])([!#8])[OX2H0])]",	// -OS(O2)- (sulfonate)
                                            "[S,s]",    // S (other, minus sum of 31 to 35)
                                            "[P,p]",	// P (any)
                                            "FA",	// -F (connected to aliphatic)
                                            "Fa",	// -F (connected to aromatic)
                                            "Cl",	// Cl (any)
                                            "Br",	// Br (any)
                                            "I",	// I (any)
                                            "[CX3;!R](=[OX1])[OX2H0]",	// -OC(O)- (noncyclic ester)
                                            "[CX3;R](=[OX1])[OX2H0;R]",	// -OC(O)- (lactone)
                                            "P(=[OX1])(O)(O)O",	// O=P(OR) (phosphate)
                                            "[CX3](=[OX1])([OX2H0])[OX2H0]",	// -OC(O)O- (carbonate)
                                            "[CX3](=O)[OX1H0-,OX2H1]",	// -C(O)OH (carboxylic acid)
                                            "nC=[OX1]",	// -NC(O)- (aromatic amide)
                                            "[N;!R]C=[OX1]",	// -NC(O)- (noncyclic aliphatic amide)
                                            "[N;R][C;R]=[OX1]",	// -NC(O)- (lactam)
                                            "[$([SX4](=[OX1])(=[OX1])([!O])[NX3]),$([SX4+2]([OX1-])([OX1-])([!O])[NX3])]",	// -S(O)(O)N- (sulfonamide)
                                            "NC(=[OX1])N",	// -NC(O)N- (urea)
                                            "[NX3,NX4+][CX3](=[OX1])[OX2,OX1-]",	// -C(O)O- (carbamate)
                                            "[CX3](=[OX1])[NX3][CX3](=[OX1])",	// -C(O)NC(O)- (imide)
                                            "C1(=[OX1])C=CC(=[OX1])C=C1",	// -C(O)C=CC(O)- (quinone)
                                            "[$([CX4]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])]",	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                            "[CX4]([F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)])[CX4][F,Cl,Br,I,$([NX3](=O)=O),$([NX3+](=O)[O-]),$(C#N),$([CX4](F)(F)F)]",	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                            "*1~*2~*(~*3~*(~*~*~*~*3)~*1)~*~*~*1~*2~*~*~*1",	// steriod (fused ring system)
                                            "[OX2H]CC[O,N]",	// H-bond 1
                                            "[OX2H]C[C,N]=[O,S]",	// H-bond 2
                                            "[OX2H]c1ccccc1[O,Nv3]",	// H-bond 3
                                            "[OX2H]c1ccccc1C=[O,S]",	// H-bond 4
                                            "[OX2H]c1ccccc1[$([NX3](=O)=O),$([NX3+](=O)[O-])]",	// H-bond 5
                                            "[NH,NH2,NH3+]CC[O,N]",	// H-bond 6
                                            "[NH,NH2,NH3+]c1ccccc1[O,N]",	// H-bond 7
                                            "[NH,NH2,NH3+]c1ccccc1[C,N]=[O,S]",	// H-bond 8
                                            "[OX2H]c1ccccc1[Cl,Br,I]",	// H-bond 9
                                            "[CX4]([OH])[CX4][OH]",	// >C(OH)C(OH)< (1,2-diol)
                                            "n:n",	// n:n (1,2 aromatic interaction, pyridazine type)
                                            "o:n",	// x:x (1,2 aromatic interaction, isoxazole type)
                                            "n:c:n",	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                            "o:c:n",	// x:c:x (1,3 aromatic interaction, oxazole type)
                                            "n:c:c:n",	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                            "[F,Cl,Br,I,N,O,S]-c:c-[F,Cl,Br,I,N,O,S]",	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                            "[F,Cl,Br,I,N,O,S]-c:c:c-[F,Cl,Br,I,N,O,S]",	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                            "[F,Cl,Br,I,N,O,S]-c:c:c:c-[F,Cl,Br,I,N,O,S]",	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                            "P(=[OX1])N",	// O=PN (phosphamide)
                                            "Nc:n",	// N-c:n (2-aminopyridine)
                                            "[$(cC[OH]);!$(c[CX3](=O)[OX1H0-,OX2H1])]",	// OH-C-c (benzyl alcohol)
                                            "[$([#7+][OX1-]),$([#7v5]=[OX1]);!$([#7](~[O])~[O]);!$([#7]=[#7])]",	// O=N (N-oxide)
                                            "[OX2]-c:c-[OX2]"	// -O-c:c-O- (1,2-dimethoxy)
                                            };
    
    private static double[] coefAFragments = {
                                                0.345,	// -OH (connected to aliphatic)
                                                0.543,	// -OH (phenol)
                                                0.177,	// -NH2 (connected to aliphatic)
                                                0.247,	// -NH2 (aniline)
                                                0.087,	// >NH (connected to aliphatic, noncyclic)
                                                0.321,	// >NH (connected to aliphatic, cyclic)
                                                0.194,	// >NH (aniline)
                                                0.371,	// >NH (pyrrole)
                                                0.243,	// -CO2H (carboxylic acid)
                                                0.275,	// -CONH2 (primary amide)
                                                0.281,	// -CONH- (secondary amide, aliphatic)
                                                -0.091,	// -CONH- (aromatic amide)
                                                0.356,	// -SO2NH (primary or secondary)
                                                -0.165,	// -NHCONH- (urea)
                                                -0.119,	// >NCONH- (urea)
                                                -0.105,	// -NHCOO- (carbamate)
                                                0.170,	// -NHC(=N)N< (guanidine)
                                                0.082,	// #CH (alkyne)
                                                0.493,	// -P(OH)- (phosphoric acid)
                                                0.019,	// >CHX (X defined as halogen, NO2, C#N or CF3)
                                                0.050,	// -CHX2 (X defined as halogen, NO2, C#N or CF3)
                                                -0.362,	// >C(CO2H)C(CO2H)< (1,2-diacid)
                                                0.118,	// >C(X)CO2H (X defined as halogen, NO2, C#N or CF3)
                                                0.100,	// >C(X)OH (X defined as halogen, NO2, C#N or CF3)
                                                0.051,	// >CX-C(OH< (X defined as halogen, NO2, C#N or CF3)
                                                0.194,	// nH:x (pyrazole type)
                                                0.042,	// nH:c:x (imidazole type)
                                                -0.089,	// H-bond 1
                                                -0.161,	// H-bond 2
                                                -0.251,	// H-bond 3
                                                -0.418,	// H-bond 4
                                                -0.450,	// H-bond 5
                                                -0.155,	// H-bond 6
                                                0.000,	// H-bond 7
                                                -0.093,	// H-bond 8
                                                -0.110,	// H-bond 9
                                                -0.601,	// H-bond 10
                                                -0.475,	// 8-OH quinoline (peri interaction)
                                                0.119,	// 3-X phenol (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                                0.176,	// 4-X phenol (para interaction, X defined as halogen, NO2, C#N or CF3)
                                                0.080,	// 3-X aniline (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                                0.084,	// 4-X aniline (para interaction, X defined as halogen, NO2, C#N or CF3)
                                                0.085,	// 3-X benzoic acid (meta interaction, X defined as halogen, NO2, C#N or CF3)
                                                0.055,	// 4-X benzoic acid (para interaction, X defined as halogen, NO2, C#N or CF3)
                                                -0.162,	// 2,6-dialkyl phenol
                                                -0.181,	// 2,6-dialkyl aniline
                                                0.195,	// 2-CX phenol
                                                -0.203,	// 3-CO2H phenol
                                                0.096,	// 4-CO2H phenol
                                                0.185,	// 3-C=O phenol
                                                0.203,	// 4-C=O phenol
                                                0.003   // Intercept
                                             };
    
    private static double[] coefBHFragments = {
                                                0.007,	// -CH3 (sp3)
                                                0.000,	// >CH2 (sp3)
                                                0.011,	// >CH- (sp3)
                                                0.037,	// >C< (sp3)
                                                0.019,	// =CH2 (sp2)
                                                0.011,	// =CH- (sp2 or aromatic)
                                                0.000,	// =C< (sp2 or nonfused aromatic)
                                                0.019,	// C (fused aromatic)
                                                0.028,	// #C (sp)
                                                0.481,	// -NH2 (sp3, connected to aliphatic)
                                                0.275,	// -NH2 (sp3, connected to aromatic)
                                                0.541,	// >NH (sp3, connected to aliphatic)
                                                0.415,	// >NH (sp3, connected to aromatic)
                                                0.316,	// >NH (pyrrole)
                                                0.653,	// >N- (sp3, connected to aliphatic)
                                                0.321,	// >N- (sp3, connected to aromatic)
                                                0.392,	// >N- (pyrrole)
                                                0.200,	// =N (sp2, noncyclic)
                                                0.596,	// =N (sp2, cyclic)
                                                0.321,	// =N- (pyridine)
                                                0.242,	// N#C- (sp, connected to aliphatic)
                                                0.103,	// N#C- (sp, connected to aromatic)
                                                -0.476,	// -NO2 (connected to aliphatic)
                                                -0.525,	// -NO2 (connected to aromatic)
                                                -0.204,	// -ONO2 (nitrate)
                                                0.307,	// -OH (any)
                                                0.211,	// -O- (sp3, noncyclic)
                                                0.331,	// -O- (sp3, cyclic)
                                                0.047,	// -O- (aromatic)
                                                0.334,	// =O (sp2)
                                                0.168,	// -S- (sp3)
                                                0.043,	// -S- (aromatic)
                                                0.071,	// =S (sp2)
                                                0.448,	// >S= (sp2)
                                                -0.188,	// -OS(O2)- (sulfonate)
                                                0.000,	// S (other)
                                                1.183,	// P (any)
                                                -0.036,	// -F (connected to aliphatic)
                                                0.000,	// -F (connected to aromatic)
                                                0.000,	// Cl (any)
                                                -0.011,	// Br (any)
                                                0.000,	// I (any)
                                                -0.206,	// -OC(O)- (noncyclic ester)
                                                -0.214,	// -OC(O)- (lactone)
                                                -0.394,	// O=P(OR) (phosphate)
                                                -0.267,	// -OC(O)O- (carbonate)
                                                -0.308,	// -C(O)OH (carboxylic acid)
                                                -0.095,	// -NC(O)- (aromatic amide)
                                                -0.287,	// -NC(O)- (noncyclic aliphatic amide)
                                                -0.231,	// -NC(O)- (lactam)
                                                -0.446,	// -S(O)(O)N- (sulfonamide)
                                                -0.076,	// -NC(O)N- (urea)
                                                -0.252,	// -C(O)O- (carbamate)
                                                -0.148,	// -C(O)NC(O)- (imide)
                                                -0.051,	// -C(O)C=CC(O)- (quinone)
                                                -0.014,	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                                0.013,	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                                0.267,	// steriod (fused ring system)
                                                0.000,	// H-bond 1
                                                -0.068,	// H-bond 2
                                                -0.079,	// H-bond 3
                                                -0.387,	// H-bond 4
                                                -0.126,	// H-bond 5
                                                0.000,	// H-bond 6
                                                -0.059,	// H-bond 7
                                                -0.045,	// H-bond 8
                                                -0.130,	// H-bond 9
                                                0.000,	// >C(OH)C(OH)< (1,2-diol)
                                                -0.132,	// n:n (1,2 aromatic interaction, pyridazine type)
                                                -0.157,	// x:x (1,2 aromatic interaction, isoxazole type)
                                                -0.098,	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                                -0.170,	// x:c:x (1,3 aromatic interaction, oxazole type)
                                                -0.089,	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                                0.031,	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.035,	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.023,	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.668,	// O=PN (phosphamide)
                                                -0.042,	// N-c:n (2-aminopyridine)
                                                0.131,	// OH-C-c (benzyl alcohol)
                                                -0.408,	// O=N (N-oxide)
                                                -0.216,	// -O-c:c-O- (1,2-dimethoxy)
                                                0.071   // Intercept 
                                             };
    
    private static double[] coefBOFragments = {
                                                0.000,	// -CH3 (sp3)
                                                0.000,	// >CH2 (sp3)
                                                0.020,	// >CH- (sp3)
                                                0.047,	// >C< (sp3)
                                                0.024,	// =CH2 (sp2)
                                                0.012,	// =CH- (sp2 or aromatic)
                                                0.000,	// =C< (sp2 or nonfused aromatic)
                                                0.018,	// C (fused aromatic)
                                                0.032,	// #C (sp)
                                                0.486,	// -NH2 (sp3, connected to aliphatic)
                                                0.326,	// -NH2 (sp3, connected to aromatic)
                                                0.543,	// >NH (sp3, connected to aliphatic)
                                                0.426,	// >NH (sp3, connected to aromatic)
                                                0.267,	// >NH (pyrrole)
                                                0.655,	// >N- (sp3, connected to aliphatic)
                                                0.338,	// >N- (sp3, connected to aromatic)
                                                0.338,	// >N- (pyrrole)
                                                0.202,	// =N (sp2, noncyclic)
                                                0.589,	// =N (sp2, cyclic)
                                                0.300,	// =N- (pyridine)
                                                0.245,	// N#C- (sp, connected to aliphatic)
                                                0.093,	// N#C- (sp, connected to aromatic)
                                                -0.595,	// -NO2 (connected to aliphatic)
                                                -0.533,	// -NO2 (connected to aromatic)
                                                -0.202,	// -ONO2 (nitrate)
                                                0.311,	// -OH (any)
                                                0.226,	// -O- (sp3, noncyclic)
                                                0.330,	// -O- (sp3, cyclic)
                                                0.060,	// -O- (aromatic)
                                                0.339,	// =O (sp2)
                                                0.175,	// -S- (sp3)
                                                0.083,	// -S- (aromatic)
                                                0.069,	// =S (sp2)
                                                0.319,	// >S= (sp2)
                                                -0.190,	// -OS(O2)- (sulfonate)
                                                0.000,	// S (other)
                                                1.189,	// P (any)
                                                -0.033,	// -F (connected to aliphatic)
                                                0.000,	// -F (connected to aromatic)
                                                0.000,	// Cl (any)
                                                0.000,	// Br (any)
                                                0.000,	// I (any)
                                                -0.223,	// -OC(O)- (noncyclic ester)
                                                -0.169,	// -OC(O)- (lactone)
                                                -0.408,	// O=P(OR) (phosphate)
                                                -0.298,	// -OC(O)O- (carbonate)
                                                -0.312,	// -C(O)OH (carboxylic acid)
                                                -0.038,	// -NC(O)- (aromatic amide)
                                                -0.292,	// -NC(O)- (noncyclic aliphatic amide)
                                                -0.242,	// -NC(O)- (lactam)
                                                -0.443,	// -S(O)(O)N- (sulfonamide)
                                                -0.054,	// -NC(O)N- (urea)
                                                -0.251,	// -C(O)O- (carbamate)
                                                -0.149,	// -C(O)NC(O)- (imide)
                                                -0.050,	// -C(O)C=CC(O)- (quinone)
                                                -0.016,	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                                0.010,	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                                0.218,	// steriod (fused ring system)
                                                0.000,	// H-bond 1
                                                -0.090,	// H-bond 2
                                                -0.122,	// H-bond 3
                                                -0.403,	// H-bond 4
                                                -0.120,	// H-bond 5
                                                0.000,	// H-bond 6
                                                -0.027,	// H-bond 7
                                                -0.069,	// H-bond 8
                                                -0.130,	// H-bond 9
                                                -0.018,	// >C(OH)C(OH)< (1,2-diol)
                                                -0.094,	// n:n (1,2 aromatic interaction, pyridazine type)
                                                -0.141,	// x:x (1,2 aromatic interaction, isoxazole type)
                                                -0.113,	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                                -0.184,	// x:c:x (1,3 aromatic interaction, oxazole type)
                                                -0.073,	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                                0.025,	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.033,	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.025,	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.668,	// O=PN (phosphamide)
                                                -0.057,	// N-c:n (2-aminopyridine)
                                                0.129,	// OH-C-c (benzyl alcohol)
                                                -0.405,	// O=N (N-oxide)
                                                -0.218,	// -O-c:c-O- (1,2-dimethoxy)
                                                0.064   // Intercept
                                             };
    
    private static double[] coefSFragments = {
                                                -0.075,	// -CH3 (sp3)
                                                0.000,	// >CH2 (sp3)
                                                0.036,	// >CH- (sp3)
                                                0.071,	// >C< (sp3)
                                                -0.085,	// =CH2 (sp2)
                                                0.050,	// =CH- (sp2 or aromatic)
                                                0.101,	// =C< (sp2 or nonfused aromatic)
                                                0.121,	// C (fused aromatic)
                                                0.034,	// #C (sp)
                                                0.175,	// -NH2 (sp3, connected to aliphatic)
                                                0.383,	// -NH2 (sp3, connected to aromatic)
                                                0.265,	// >NH (sp3, connected to aliphatic)
                                                0.311,	// >NH (sp3, connected to aromatic)
                                                0.221,	// >NH (pyrrole)
                                                0.323,	// >N- (sp3, connected to aliphatic)
                                                0.295,	// >N- (sp3, connected to aromatic)
                                                0.265,	// >N- (pyrrole)
                                                0.125,	// =N (sp2, noncyclic)
                                                0.254,	// =N (sp2, cyclic)
                                                0.223,	// =N- (pyridine)
                                                0.694,	// N#C- (sp, connected to aliphatic)
                                                0.390,	// N#C- (sp, connected to aromatic)
                                                0.000,	// -NO2 (connected to aliphatic)
                                                -0.231,	// -NO2 (connected to aromatic)
                                                -0.476,	// -ONO2 (nitrate)
                                                0.247,	// -OH (any)
                                                0.185,	// -O- (sp3, noncyclic)
                                                0.185,	// -O- (sp3, cyclic)
                                                0.000,	// -O- (aromatic)
                                                0.370,	// =O (sp2)
                                                0.189,	// -S- (sp3)
                                                0.000,	// -S- (aromatic)
                                                0.618,	// =S (sp2)
                                                1.065,	// >S= (sp2)
                                                -0.505,	// -OS(O2)- (sulfonate)
                                                0.643,	// S (other)
                                                0.703,	// P (any)
                                                -0.042,	// -F (connected to aliphatic)
                                                0.000,	// -F (connected to aromatic)
                                                0.082,	// Cl (any)
                                                0.161,	// Br (any)
                                                0.198,	// I (any)
                                                -0.225,	// -OC(O)- (noncyclic ester)
                                                0.360,	// -OC(O)- (lactone)
                                                -0.240,	// O=P(OR) (phosphate)
                                                -0.190,	// -OC(O)O- (carbonate)
                                                -0.412,	// -C(O)OH (carboxylic acid)
                                                -0.076,	// -NC(O)- (aromatic amide)
                                                0.175,	// -NC(O)- (noncyclic aliphatic amide)
                                                -0.100,	// -NC(O)- (lactam)
                                                -0.569,	// -S(O)(O)N- (sulfonamide)
                                                -0.553,	// -NC(O)N- (urea)
                                                -0.588,	// -C(O)O- (carbamate)
                                                -0.510,	// -C(O)NC(O)- (imide)
                                                -0.411,	// -C(O)C=CC(O)- (quinone)
                                                -0.050,	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                                0.000,	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                                1.029,	// steriod (fused ring system)
                                                -0.067,	// H-bond 1
                                                -0.095,	// H-bond 2
                                                -0.237,	// H-bond 3
                                                -0.344,	// H-bond 4
                                                -0.276,	// H-bond 5
                                                -0.102,	// H-bond 6
                                                0.000,	// H-bond 7
                                                -0.140,	// H-bond 8
                                                -0.120,	// H-bond 9
                                                0.052,	// >C(OH)C(OH)< (1,2-diol)
                                                0.024,	// n:n (1,2 aromatic interaction, pyridazine type)
                                                0.047,	// x:x (1,2 aromatic interaction, isoxazole type)
                                                -0.040,	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                                0.087,	// x:c:x (1,3 aromatic interaction, oxazole type)
                                                -0.051,	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                                -0.043,	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.038,	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                0.000,	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.452,	// O=PN (phosphamide)
                                                0.098,	// N-c:n (2-aminopyridine)
                                                0.000,	// OH-C-c (benzyl alcohol)
                                                0.434,	// O=N (N-oxide)
                                                0.380,	// -O-c:c-O- (1,2-dimethoxy)
                                                0.277   // Intercept
                                             };
    
    private static double[] coefEFragments = {
                                                -0.104,	// -CH3 (sp3)
                                                0.000,	// >CH2 (sp3)
                                                0.089,	// >CH- (sp3)
                                                0.187,	// >C< (sp3)
                                                -0.045,	// =CH2 (sp2)
                                                0.068,	// =CH- (sp2 or aromatic)
                                                0.180,	// =C< (sp2 or nonfused aromatic)
                                                0.300,	// C (fused aromatic)
                                                0.040,	// #C (sp)
                                                0.085,	// -NH2 (sp3, connected to aliphatic)
                                                0.163,	// -NH2 (sp3, connected to aromatic)
                                                0.138,	// >NH (sp3, connected to aliphatic)
                                                0.192,	// >NH (sp3, connected to aromatic)
                                                -0.030,	// >NH (pyrrole)
                                                0.220,	// >N- (sp3, connected to aliphatic)
                                                0.346,	// >N- (sp3, connected to aromatic)
                                                0.083,	// >N- (pyrrole)
                                                0.117,	// =N (sp2, noncyclic)
                                                0.121,	// =N (sp2, cyclic)
                                                0.046,	// =N- (pyridine)
                                                0.000,	// N#C- (sp, connected to aliphatic)
                                                0.000,	// N#C- (sp, connected to aromatic)
                                                0.200,	// -NO2 (connected to aliphatic)
                                                0.210,	// -NO2 (connected to aromatic)
                                                0.000,	// -ONO2 (nitrate)
                                                0.061,	// -OH (any)
                                                0.014,	// -O- (sp3, noncyclic)
                                                0.013,	// -O- (sp3, cyclic)
                                                -0.125,	// -O- (aromatic)
                                                -0.041,	// =O (sp2)
                                                0.330,	// -S- (sp3)
                                                0.116,	// -S- (aromatic)
                                                0.364,	// =S (sp2)
                                                0.413,	// >S= (sp2)
                                                0.000,	// -OS(O2)- (sulfonate)
                                                0.465,	// S (other)
                                                0.295,	// P (any)
                                                -0.180,	// -F (connected to aliphatic)
                                                -0.230,	// -F (connected to aromatic)
                                                0.023,	// Cl (any)
                                                0.196,	// Br (any)
                                                0.533,	// I (any)
                                                -0.113,	// -OC(O)- (noncyclic ester)
                                                0.000,	// -OC(O)- (lactone)
                                                -0.100,	// O=P(OR) (phosphate)
                                                0.000,	// -OC(O)O- (carbonate)
                                                -0.192,	// -C(O)OH (carboxylic acid)
                                                0.221,	// -NC(O)- (aromatic amide)
                                                0.000,	// -NC(O)- (noncyclic aliphatic amide)
                                                0.061,	// -NC(O)- (lactam)
                                                -0.111,	// -S(O)(O)N- (sulfonamide)
                                                -0.110,	// -NC(O)N- (urea)
                                                0.000,	// -C(O)O- (carbamate)
                                                0.000,	// -C(O)NC(O)- (imide)
                                                0.000,	// -C(O)C=CC(O)- (quinone)
                                                -0.017,	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                                0.012,	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                                0.285,	// steriod (fused ring system)
                                                0.029,	// H-bond 1
                                                0.000,	// H-bond 2
                                                -0.069,	// H-bond 3
                                                0.000,	// H-bond 4
                                                0.000,	// H-bond 5
                                                0.000,	// H-bond 6
                                                0.000,	// H-bond 7
                                                0.000,	// H-bond 8
                                                -0.100,	// H-bond 9
                                                -0.043,	// >C(OH)C(OH)< (1,2-diol)
                                                0.092,	// n:n (1,2 aromatic interaction, pyridazine type)
                                                -0.113,	// x:x (1,2 aromatic interaction, isoxazole type)
                                                0.000,	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                                0.052,	// x:c:x (1,3 aromatic interaction, oxazole type)
                                                0.000,	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                                0.000,	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                0.000,	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                0.000,	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.080,	// O=PN (phosphamide)
                                                0.185,	// N-c:n (2-aminopyridine)
                                                0.000,	// OH-C-c (benzyl alcohol)
                                                0.000,	// O=N (N-oxide)
                                                0.000,	// -O-c:c-O- (1,2-dimethoxy)
                                                0.248   // Intercept
                                             };
    
    private static double[] coefLFragments = {
                                                0.321,	// -CH3 (sp3)
                                                0.499,	// >CH2 (sp3)
                                                0.449,	// >CH- (sp3)
                                                0.443,	// >C< (sp3)
                                                0.244,	// =CH2 (sp2)
                                                0.469,	// =CH- (sp2 or aromatic)
                                                0.624,	// =C< (sp2 or nonfused aromatic)
                                                0.744,	// C (fused aromatic)
                                                0.332,	// #C (sp)
                                                0.781,	// -NH2 (sp3, connected to aliphatic)
                                                0.949,	// -NH2 (sp3, connected to aromatic)
                                                0.568,	// >NH (sp3, connected to aliphatic)
                                                0.912,	// >NH (sp3, connected to aromatic)
                                                1.250,	// >NH (pyrrole)
                                                0.400,	// >N- (sp3, connected to aliphatic)
                                                0.869,	// >N- (sp3, connected to aromatic)
                                                0.794,	// >N- (pyrrole)
                                                -0.235,	// =N (sp2, noncyclic)
                                                -0.240,	// =N (sp2, cyclic)
                                                0.574,	// =N- (pyridine)
                                                0.757,	// N#C- (sp, connected to aliphatic)
                                                0.732,	// N#C- (sp, connected to aromatic)
                                                0.278,	// -NO2 (connected to aliphatic)
                                                0.347,	// -NO2 (connected to aromatic)
                                                0.000,	// -ONO2 (nitrate)
                                                0.672,	// -OH (any)
                                                0.360,	// -O- (sp3, noncyclic)
                                                0.359,	// -O- (sp3, cyclic)
                                                0.057,	// -O- (aromatic)
                                                0.495,	// =O (sp2)
                                                1.258,	// -S- (sp3)
                                                0.848,	// -S- (aromatic)
                                                0.954,	// =S (sp2)
                                                2.196,	// >S= (sp2)
                                                0.000,	// -OS(O2)- (sulfonate)
                                                0.554,	// S (other)
                                                2.051,	// P (any)
                                                -0.143,	// -F (connected to aliphatic)
                                                -0.147,	// -F (connected to aromatic)
                                                0.669,	// Cl (any)
                                                1.097,	// Br (any)
                                                1.590,	// I (any)
                                                -0.390,	// -OC(O)- (noncyclic ester)
                                                0.406,	// -OC(O)- (lactone)
                                                -0.483,	// O=P(OR) (phosphate)
                                                0.000,	// -OC(O)O- (carbonate)
                                                -0.369,	// -C(O)OH (carboxylic acid)
                                                0.000,	// -NC(O)- (aromatic amide)
                                                0.603,	// -NC(O)- (noncyclic aliphatic amide)
                                                0.583,	// -NC(O)- (lactam)
                                                0.000,	// -S(O)(O)N- (sulfonamide)
                                                0.000,	// -NC(O)N- (urea)
                                                0.000,	// -C(O)O- (carbamate)
                                                0.000,	// -C(O)NC(O)- (imide)
                                                0.000,	// -C(O)C=CC(O)- (quinone)
                                                -0.111,	// -CX2- (CX2, X defined as halogen, NO2, C#N or CF3)
                                                0.054,	// >CXCX< (XCCX, X defined as halogen, NO2, C#N or CF3)
                                                0.488,	// steriod (fused ring system)
                                                -0.072,	// H-bond 1
                                                -0.337,	// H-bond 2
                                                0.000,	// H-bond 3
                                                -0.303,	// H-bond 4
                                                -0.364,	// H-bond 5
                                                0.062,	// H-bond 6
                                                0.000,	// H-bond 7
                                                0.169,	// H-bond 8
                                                -0.400,	// H-bond 9
                                                0.100,	// >C(OH)C(OH)< (1,2-diol)
                                                -0.179,	// n:n (1,2 aromatic interaction, pyridazine type)
                                                0.000,	// x:x (1,2 aromatic interaction, isoxazole type)
                                                0.042,	// n:c:n (1,3 aromatic interaction, pyrimidine type)
                                                0.209,	// x:c:x (1,3 aromatic interaction, oxazole type)
                                                -0.058,	// x:c:c:x (1,4 aromatic interaction, pyrazine type)
                                                -0.081,	// Y-c:c-Y (ortho interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                -0.026,	// Y-c:c:c-Y (meta interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                0.000,	// Y-c:c:c:c-Y (para interaction, Y defined as any heteroatom, i.e. halogen, N, O, S)
                                                0.000,	// O=PN (phosphamide)
                                                0.149,	// N-c:n (2-aminopyridine)
                                                -0.145,	// OH-C-c (benzyl alcohol)
                                                0.000,	// O=N (N-oxide)
                                                0.000,	// -O-c:c-O- (1,2-dimethoxy)
                                                0.130   // Intercept
                                            };

    public MLFERDescriptor() {

    }

    @Override
    public DescriptorSpecification getSpecification() {
        return new DescriptorSpecification(
                "MLFER",
                this.getClass().getName(),
                "$Id: MLFERDescriptor.java 1 2008-07-07 06:50:01Z yapchunwei $",
                "PaDEL");
    }

    @Override
    public String[] getParameterNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getParameterType(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParameters(Object[] params) throws CDKException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object[] getParameters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the names of descriptors
     * 
     * @return  Names of descriptors
     */
    @Override
    public String[] getDescriptorNames() {
        return names;
    }
    
    /**
     * Get dummy descriptor values when error occurs.
     * 
     * @param e Exception that prevents the proper calculation of the descriptors
     * @return  Dummy descriptor values
     */
    private DescriptorValue getDummyDescriptorValue(Exception e) {
        int ndesc = getDescriptorNames().length;
        DoubleArrayResult results = new DoubleArrayResult(ndesc);
        for (int i = 0; i < ndesc; i++) results.add(Double.NaN);
        return new DescriptorValue(getSpecification(), getParameterNames(),
                getParameters(), results, getDescriptorNames(), e);
    }
    
    /**
     * 
     * @param container AtomContainer
     * @return
     * @throws org.openscience.cdk.exception.CDKException
     */
    @Override
    public DescriptorValue calculate(IAtomContainer container) {
    
        try
        {
            SmartsPattern.prepare(container);
            int maxAFragments = AFragments.length;
            double A = 0.0;
            for (int i=0; i<maxAFragments; ++i)
            {
                int uniqueMatches = SmartsPattern.create(AFragments[i])
                        .setPrepare(false).matchAll(container).uniqueAtoms().count();
                A += uniqueMatches * coefAFragments[i];
            }

            int maxBSELFragments = BSELFragments.length;
            double BH = 0.0;
            double BO = 0.0;
            double S = 0.0;
            double E = 0.0;
            double L = 0.0;
            int sulphurCount = 0;
            for (int i=0; i<maxBSELFragments; ++i)
            {
                int uniqueMatches = SmartsPattern.create(BSELFragments[i])
                        .setPrepare(false).matchAll(container).uniqueAtoms().count();
                if (30<=i && i<=34)
                {
                    sulphurCount += uniqueMatches;
                }
                else if (i==35)
                {
                    uniqueMatches -= sulphurCount;
                }
                BH += uniqueMatches * coefBHFragments[i];
                BO += uniqueMatches * coefBOFragments[i];
                S += uniqueMatches * coefSFragments[i];
                E += uniqueMatches * coefEFragments[i];
                L += uniqueMatches * coefLFragments[i];
            }

            // Finally, add intercept.
            A += coefAFragments[coefAFragments.length-1];
            BH += coefBHFragments[coefBHFragments.length-1];
            BO += coefBOFragments[coefBOFragments.length-1];
            S += coefSFragments[coefSFragments.length-1];
            E += coefEFragments[coefEFragments.length-1];
            L += coefLFragments[coefLFragments.length-1];

            DoubleArrayResult retval = new DoubleArrayResult();
            retval.add(A);
            retval.add(BH);
            retval.add(BO);
            retval.add(S);
            retval.add(E);
            retval.add(L);

            return new DescriptorValue(getSpecification(), getParameterNames(), getParameters(), retval, names);
        }
        catch (Exception e)
        {
            return getDummyDescriptorValue(new CDKException("Error in SMARTSQueryTool: " + e.getMessage()));
        }

    }

    /**
     * Returns the specific type of the DescriptorResult object.
     * <p/>
     * The return value from this method really indicates what type of result will
     * be obtained from the {@link org.openscience.cdk.qsar.DescriptorValue} object. Note that the same result
     * can be achieved by interrogating the {@link org.openscience.cdk.qsar.DescriptorValue} object; this method
     * allows you to do the same thing, without actually calculating the descriptor.
     *
     * @return an object that implements the {@link org.openscience.cdk.qsar.result.IDescriptorResult} interface indicating
     *         the actual type of values returned by the descriptor in the {@link org.openscience.cdk.qsar.DescriptorValue} object
     */
    @Override
    public IDescriptorResult getDescriptorResultType() {
        return new DoubleArrayResultType(names.length);
    }

    @Override
    public void initialise(org.openscience.cdk.interfaces.IChemObjectBuilder builder) {
        // No descriptor-specific initialisation required.
    }

}

