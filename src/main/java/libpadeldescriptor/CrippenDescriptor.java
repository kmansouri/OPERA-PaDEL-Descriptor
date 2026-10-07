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


import java.util.List;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleArrayResultType;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 * Wildman-Crippen LogP and MR.
 * <p/>
 * The code currently Wildman-Crippen LogP and MR estimates for a molecule. 
 * 
 * Code adapted from RDKIT.
 * Wildman, S. A., and Crippen, G. M. (1999). Prediction of Physicochemical Parameters by Atomic Contributions. J Chem Inf Comput Sci 39, 868-873.
 * <p/>
 * The order of the values returned is
 * <ol>
 * <li>CrippenLogP
 * <li>CrippenMR
 * </ol>
 * <p/>
 *
 * @author Yap Chun Wei
 * @cdk.created 2008-07-07
 * @cdk.module qsarmolecular
 * @cdk.svnrev $Revision: 1 $
 * @cdk.set qsar-descriptors
 * @cdk.dictref qsar-descriptors:Crippen
 * @cdk.keyword molecular type Crippen descriptor
 * @cdk.keyword descriptor
 */
public class CrippenDescriptor implements IMolecularDescriptor {
    
    public static final String[] names = {
                                            "CrippenLogP",
                                            "CrippenMR"
                                         };
    
    private static final String[] fragments = {
                                            "[$([CH4]),$([CH3]C),$([CH2](C)C)]",
                                            "[$([CH](C)(C)C),$([C](C)(C)(C)C)]",
                                            "[$([CH3][N,O,S,F,Cl,Br,I]),$([CH2X4][N,O,S,F,Cl,Br,I])]",
                                            "[$([CH1X4][N,O,S,F,Cl,Br,I]),$([CH0X4][N,O,S,F,Cl,Br,I])]",
                                            "[C]=[!C;A;!#1]",
                                            "[C;A]=C",
                                            "[CX2]#[A;!#1]",
                                            "[CH3]c",
                                            "[CH3]a",
                                            "[CH2X4]a",
                                            "[CHX4]a",
                                            "[CH0X4]a",
                                            "[cH0]-[A;!C;!N;!O;!S;!F;!Cl;!Br;!I;!H]",
                                            "[c][#9]",
                                            "[c][#17]",
                                            "[c][#35]",
                                            "[c][#53]",
                                            "[cH]",
                                            "[c](:a)(:a):a",
                                            "[c](:a)(:a)-a",
                                            "[c](:a)(:a)-C",
                                            "[c](:a)(:a)-N",
                                            "[c](:a)(:a)-O",
                                            "[c](:a)(:a)-S",
                                            "[c](:a)(:a)=[C,N,O]",
                                            "[$([C](=C)(a)[A;!#1]),$([C](=C)(c)a),$([C](=C)a),$([C]=c)]",
                                            "[CX4][A;!C;!N;!O;!S;!F;!Cl;!Br;!I;!#1]",
                                            "[#6]",
                                            "[#1][#6,#1]",
                                            "[$([#1]O[CX4,c]),$([#1]O[!C;!N;!O;!S]),$([#1][!C;!N;!O])]",
                                            "[$([#1][#7]),$([#1]O[#7])]",
                                            "[$([#1]OC=[#6,#7,O,S]),$([#1]O[O,S])]",
                                            "[#1]",
                                            "[NH2+0][A;!#1]",
                                            "[NH+0]([A;!#1])[A;!#1]",
                                            "[NH2+0]a",
                                            "[NH1+0]([!#1;A,a])a",
                                            "[NH+0]=[!#1;A,a]",
                                            "[N+0](=[!#1;A,a])[!#1;A,a]",
                                            "[N+0]([A;!#1])([A;!#1])[A;!#1]",
                                            "[$([N+0](a)([!#1;A,a])[A;!#1]),$([N+0](a)(a)a)]",
                                            "[N+0]#[A;!#1]",
                                            "[NH3,NH2,NH;+,+2,+3]",
                                            "[n+0]",
                                            "[n;+,+2,+3]",
                                            "[$([NH0;+,+2,+3]([A;!#1])([A;!#1])([A;!#1])[A;!#1]),$([NH0;+,+2,+3](=[A;#1])([A;#1])[!#1;A,a]),$([NH0;+,+2,+3](=[#6])=[#7])]",
                                            "[$([N;+,+2,+3]#[A;!#1]),$([N;-,-2,-3]),$([N;+,+2,+3](=[N;-,-2,-3])=N)]",
                                            "[#7]",
                                            "[o]",
                                            "[OH,OH2]",
                                            "[O]([A;!#1])[A;!#1]",
                                            "[O](a)[!#1;A,a]",
                                            "[$([O]=[#7,#8]),$([OX1;-,-2,-3][#7])]",
                                            "[OX1;-,-2,-2][#16]",
                                            "[O-]C(=O)",
                                            "[OX1;-,-2,-3][!#1;!N;!S]",
                                            "[O]=c",
                                            "[$([O]=[CH]C),$([O]=C(C)([A;!#1])),$([O]=[CH][N,O]),$([O]=[CH2]),$([O]=[CX2]=O)]",
                                            "[$([O]=[CH]c),$([O]=C([C,c])[a;!#1]),$([O]=C(c)[A;!#1])]",
                                            "[O]=C([!#1;!#6])[!#1;!#6]",
                                            "[#8]",
                                            "[#9-0]",
                                            "[#17-0]",
                                            "[#35-0]",
                                            "[#53-0]",
                                            "[$([#9,#17,#35,#53;-]),$([#53;+,+2,+3]),$([+;#3,#11,#19,#37,#55])]",
                                            "[#15]",
                                            "[S;-,-2,-3,-4,+1,+2,+3,+5,+6]",
                                            "[S;A]",
                                            "[s;a]",
                                            "[#3,#11,#19,#37,#55,#4,#12,#20,#38,#56,#5,#13,#31,#49,#81,#14,#32,#50,#82,#33,#51,#83,#34,#52,#84]",
                                            "[#21,#22,#23,#24,#25,#26,#27,#28,#29,#30,#39,#40,#41,#42,#43,#44,#45,#46,#47,#48,#72,#73,#74,#75,#76,#77,#78,#79,#80]"
                                        };  
    
    private static final double[] logp = {
                                        0.1441,
                                        0,
                                        -0.2035,
                                        -0.2051,
                                        -0.2783,
                                        0.1551,
                                        0.0017,
                                        0.08452,
                                        -0.1444,
                                        -0.0516,
                                        0.1193,
                                        -0.0967,
                                        -0.5443,
                                        0,
                                        0.245,
                                        0.198,
                                        0,
                                        0.1581,
                                        0.2955,
                                        0.2713,
                                        0.136,
                                        0.4619,
                                        0.5437,
                                        0.1893,
                                        -0.8186,
                                        0.264,
                                        0.2148,
                                        0.08129,
                                        0.123,
                                        -0.2677,
                                        0.2142,
                                        0.298,
                                        0.1125,
                                        -1.019,
                                        -0.7096,
                                        -1.027,
                                        -0.5188,
                                        0.08387,
                                        0.1836,
                                        -0.3187,
                                        -0.4458,
                                        0.01508,
                                        -1.95,
                                        -0.3239,
                                        -1.119,
                                        -0.3396,
                                        0.2887,
                                        -0.4806,
                                        0.1552,
                                        -0.2893,
                                        -0.0684,
                                        -0.4195,
                                        0.0335,
                                        -0.3339,
                                        -1.326,
                                        -1.189,
                                        0.1788,
                                        -0.1526,
                                        0.1129,
                                        0.4833,
                                        -0.1188,
                                        0.4202,
                                        0.6895,
                                        0.8456,
                                        0.8857,
                                        -2.996,
                                        0.8612,
                                        -0.0024,
                                        0.6482,
                                        0.6237,
                                        -0.3808,
                                        -0.0025
                                   };

    
    private static final double[] mr = {
                                    2.503,
                                    2.433,
                                    2.753,
                                    2.731,
                                    5.007,
                                    3.513,
                                    3.888,
                                    2.464,
                                    2.412,
                                    2.488,
                                    2.582,
                                    2.576,
                                    4.041,
                                    3.257,
                                    3.564,
                                    3.18,
                                    3.104,
                                    3.35,
                                    4.346,
                                    3.904,
                                    3.509,
                                    4.067,
                                    3.853,
                                    2.673,
                                    3.135,
                                    4.305,
                                    2.693,
                                    3.243,
                                    1.057,
                                    1.395,
                                    0.9627,
                                    1.805,
                                    1.112,
                                    2.262,
                                    2.173,
                                    2.827,
                                    3,
                                    1.757,
                                    2.428,
                                    1.839,
                                    2.819,
                                    1.725,
                                    0,
                                    2.202,
                                    0,
                                    0.2604,
                                    3.359,
                                    2.134,
                                    1.08,
                                    0.8238,
                                    1.085,
                                    1.182,
                                    3.367,
                                    0.7774,
                                    0,
                                    0,
                                    3.135,
                                    0,
                                    0.2215,
                                    0.389,
                                    0.6865,
                                    1.108,
                                    5.853,
                                    8.927,
                                    14.02,
                                    0,
                                    6.92,
                                    7.365,
                                    7.591,
                                    6.691,
                                    5.754,
                                    0
                                 };

    public CrippenDescriptor() {

    }

    @Override
    public DescriptorSpecification getSpecification() {
        return new DescriptorSpecification(
                "Crippen",
                this.getClass().getName(),
                "$Id: CrippenDescriptor.java 1 2012-03-16 16:00:00Z yapchunwei $",
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
            int maxFragments = fragments.length;
            double logP = 0.0;
            double MR = 0.0;
            boolean atomTypeFound[] = new boolean[container.getAtomCount()]; // Used to ensure each atom is counted only once.
            for (int i=0, endi=container.getAtomCount(); i<endi; ++i) atomTypeFound[i] = false;
            for (int i=0; i<maxFragments; ++i)
            {
                Mappings atoms = SmartsPattern.create(fragments[i])
                        .setPrepare(false).matchAll(container).uniqueAtoms();
                for (int[] match : atoms.toArray())
                {
                    boolean atomNotUsed = false;
                    for (int a : match)
                    {
                        if (!atomTypeFound[a])
                            {
                                atomTypeFound[a] = true;
                                atomNotUsed = true;
                                break;
                            }
                        }
                    if (atomNotUsed)
                    {
                        logP += logp[i];
                        MR += mr[i];
                    }
                }
                boolean isAllFound = true;
                for (int a=0, enda=container.getAtomCount(); a<enda; ++a)
                {
                    if (!atomTypeFound[a])
                    {
                        isAllFound = false;
                        break;
                    }
                }
                if (isAllFound) break;
            }

            DoubleArrayResult retval = new DoubleArrayResult();
            retval.add(logP);
            retval.add(MR);

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

