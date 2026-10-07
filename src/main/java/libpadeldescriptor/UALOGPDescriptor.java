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


import java.util.LinkedHashMap;
import java.util.Map;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleArrayResultType;
import org.openscience.cdk.qsar.result.IDescriptorResult;

/**
 * UALOGP LogP and MR.
 * <p/>
 * The code currently UALOGP LogP and MR estimates for a molecule. 
 * 
 * Viswanadhan, V. N., Rajesh, H., and Balaji, V. N. (2011). Atom Type Preferences, Structural Diversity, and Property Profiles of Known Drugs, Leads, and Nondrugs: A Comparative Assessment. ACS Combinatorial Science 13, 327-336.
 * <p/>
 * The order of the values returned is
 * <ol>
 * <li>UALOGPLogP
 * <li>UALOGPMR
 * </ol>
 * <p/>
 *
 * @author Yap Chun Wei
 * @cdk.created 2008-07-07
 * @cdk.module qsarmolecular
 * @cdk.svnrev $Revision: 1 $
 * @cdk.set qsar-descriptors
 * @cdk.dictref qsar-descriptors:UALOGP
 * @cdk.keyword molecular type UALOGP descriptor
 * @cdk.keyword descriptor
 */
public class UALOGPDescriptor implements IMolecularDescriptor {
    
    public static final String[] names = {
                                            "UALOGPLogP",
                                            "UALOGPMR"
                                         };
    
    private static final String UALOGP_1a = "UALOGP_1a";
    private static final String UALOGP_1b = "UALOGP_1b";
    private static final String UALOGP_1c = "UALOGP_1c";
    private static final String UALOGP_1d = "UALOGP_1d";
    private static final String UALOGP_1e = "UALOGP_1e";
    private static final String UALOGP_2a = "UALOGP_2a";
    private static final String UALOGP_2b = "UALOGP_2b";
    private static final String UALOGP_2c = "UALOGP_2c";
    private static final String UALOGP_2d = "UALOGP_2d";
    private static final String UALOGP_2e = "UALOGP_2e";
    private static final String UALOGP_2f = "UALOGP_2f";
    private static final String UALOGP_3a = "UALOGP_3a";
    private static final String UALOGP_3b = "UALOGP_3b";
    private static final String UALOGP_3c = "UALOGP_3c";
    private static final String UALOGP_3d = "UALOGP_3d";
    private static final String UALOGP_3e = "UALOGP_3e";
    private static final String UALOGP_3f = "UALOGP_3f";
    private static final String UALOGP_4 = "UALOGP_4";
    private static final String UALOGP_5 = "UALOGP_5";
    private static final String UALOGP_6a = "UALOGP_6a";
    private static final String UALOGP_6b = "UALOGP_6b";
    private static final String UALOGP_7 = "UALOGP_7";
    private static final String UALOGP_8a = "UALOGP_8a";
    private static final String UALOGP_8b = "UALOGP_8b";
    private static final String UALOGP_8c = "UALOGP_8c";
    private static final String UALOGP_8d = "UALOGP_8d";
    private static final String UALOGP_8e = "UALOGP_8e";
    private static final String UALOGP_8f = "UALOGP_8f";
    private static final String UALOGP_9a = "UALOGP_9a";
    private static final String UALOGP_9b = "UALOGP_9b";
    private static final String UALOGP_10 = "UALOGP_10";
    private static final String UALOGP_11 = "UALOGP_11";
    private static final String UALOGP_12 = "UALOGP_12";
    private static final String UALOGP_13 = "UALOGP_13";
    private static final String UALOGP_14 = "UALOGP_14";
    private static final String UALOGP_15a = "UALOGP_15a";
    private static final String UALOGP_15b = "UALOGP_15b";
    private static final String UALOGP_16a = "UALOGP_16a";
    private static final String UALOGP_16b = "UALOGP_16b";
    private static final String UALOGP_16c = "UALOGP_16c";
    private static final String UALOGP_16d = "UALOGP_16d";
    private static final String UALOGP_16e = "UALOGP_16e";
    private static final String UALOGP_17 = "UALOGP_17";
    private static final String UALOGP_18a = "UALOGP_18a";
    private static final String UALOGP_18b = "UALOGP_18b";
    private static final String UALOGP_19 = "UALOGP_19";
    private static final String UALOGP_20 = "UALOGP_20";
    private static final String UALOGP_21a = "UALOGP_21a";
    private static final String UALOGP_21b = "UALOGP_21b";
    private static final String UALOGP_22 = "UALOGP_22";
    private static final String UALOGP_23 = "UALOGP_23";
    private static final String UALOGP_24a = "UALOGP_24a";
    private static final String UALOGP_24b = "UALOGP_24b";
    private static final String UALOGP_25 = "UALOGP_25";
    private static final String UALOGP_26 = "UALOGP_26";
    private static final String UALOGP_27a = "UALOGP_27a";
    private static final String UALOGP_27b = "UALOGP_27b";
    private static final String UALOGP_27c = "UALOGP_27c";
    private static final String UALOGP_28 = "UALOGP_28";
    private static final String UALOGP_29 = "UALOGP_29";
    private static final String UALOGP_30 = "UALOGP_30";
    private static final String UALOGP_31 = "UALOGP_31";
    private static final String UALOGP_32 = "UALOGP_32";
    private static final String UALOGP_33a = "UALOGP_33a";
    private static final String UALOGP_33b = "UALOGP_33b";
    private static final String UALOGP_34 = "UALOGP_34";
    private static final String UALOGP_35 = "UALOGP_35";
    private static final String UALOGP_36a = "UALOGP_36a";
    private static final String UALOGP_36b = "UALOGP_36b";
    private static final String UALOGP_36c = "UALOGP_36c";
    private static final String UALOGP_37a = "UALOGP_37a";
    private static final String UALOGP_37b = "UALOGP_37b";
    private static final String UALOGP_38 = "UALOGP_38";
    private static final String UALOGP_39 = "UALOGP_39";
    private static final String UALOGP_40 = "UALOGP_40";
    private static final String UALOGP_41 = "UALOGP_41";
    private static final String UALOGP_42 = "UALOGP_42";
    private static final String UALOGP_43 = "UALOGP_43";
    private static final String UALOGP_44 = "UALOGP_44";
    private static final String UALOGP_45 = "UALOGP_45";
    private static final String UALOGP_46 = "UALOGP_46";
    private static final String UALOGP_47 = "UALOGP_47";
    private static final String UALOGP_48 = "UALOGP_48";
    private static final String UALOGP_49 = "UALOGP_49";
    private static final String UALOGP_50 = "UALOGP_50";
    private static final String UALOGP_51 = "UALOGP_51";
    private static final String UALOGP_52 = "UALOGP_52";
    private static final String UALOGP_53 = "UALOGP_53";
    private static final String UALOGP_54 = "UALOGP_54";
    private static final String UALOGP_55 = "UALOGP_55";
    private static final String UALOGP_56 = "UALOGP_56";
    private static final String UALOGP_57 = "UALOGP_57";
    private static final String UALOGP_58 = "UALOGP_58";
    private static final String UALOGP_59 = "UALOGP_59";
    private static final String UALOGP_60 = "UALOGP_60";
    private static final String UALOGP_61 = "UALOGP_61";
    private static final String UALOGP_62 = "UALOGP_62";
    private static final String UALOGP_63 = "UALOGP_63";
    private static final String UALOGP_64 = "UALOGP_64";
    private static final String UALOGP_65 = "UALOGP_65";
    private static final String UALOGP_66 = "UALOGP_66";
    private static final String UALOGP_67 = "UALOGP_67";
    private static final String UALOGP_68 = "UALOGP_68";
    private static final String UALOGP_69 = "UALOGP_69";
    private static final String UALOGP_70 = "UALOGP_70";
    private static final String UALOGP_71 = "UALOGP_71";
    private static final String UALOGP_72 = "UALOGP_72";
    private static final String UALOGP_73 = "UALOGP_73";
    private static final String UALOGP_74 = "UALOGP_74";
    private static final String UALOGP_75 = "UALOGP_75";
    private static final String UALOGP_76 = "UALOGP_76";
    private static final String UALOGP_77 = "UALOGP_77";
    private static final String UALOGP_78 = "UALOGP_78";
    private static final String UALOGP_79 = "UALOGP_79";
    private static final String UALOGP_80 = "UALOGP_80";
    private static final String UALOGP_81 = "UALOGP_81";
    private static final String UALOGP_82 = "UALOGP_82";
    private static final String UALOGP_83 = "UALOGP_83";
    private static final String UALOGP_84 = "UALOGP_84";
    private static final String UALOGP_85 = "UALOGP_85";
    private static final String UALOGP_86 = "UALOGP_86";
    private static final String UALOGP_87 = "UALOGP_87";
    private static final String UALOGP_88 = "UALOGP_88";
    private static final String UALOGP_89 = "UALOGP_89";
    private static final String UALOGP_90 = "UALOGP_90";
    private static final String UALOGP_91 = "UALOGP_91";
    private static final String UALOGP_92 = "UALOGP_92";
    private static final String UALOGP_93 = "UALOGP_93";
    private static final String UALOGP_94 = "UALOGP_94";
    private static final String UALOGP_95 = "UALOGP_95";
    private static final String UALOGP_96 = "UALOGP_96";
    private static final String UALOGP_97 = "UALOGP_97";
    private static final String UALOGP_98 = "UALOGP_98";
    private static final String UALOGP_99 = "UALOGP_99";
    private static final String UALOGP_100 = "UALOGP_100";
    private static final String UALOGP_101 = "UALOGP_101";
    private static final String UALOGP_102 = "UALOGP_102";
    private static final String UALOGP_103 = "UALOGP_103";
    private static final String UALOGP_104 = "UALOGP_104";
    private static final String UALOGP_105 = "UALOGP_105";
    private static final String UALOGP_106 = "UALOGP_106";
    private static final String UALOGP_107 = "UALOGP_107";
    private static final String UALOGP_108 = "UALOGP_108";
    private static final String UALOGP_109 = "UALOGP_109";
    private static final String UALOGP_110 = "UALOGP_110";
    private static final String UALOGP_111 = "UALOGP_111";
    private static final String UALOGP_112 = "UALOGP_112";
    private static final String UALOGP_113 = "UALOGP_113";
    private static final String UALOGP_114 = "UALOGP_114";
    private static final String UALOGP_115 = "UALOGP_115";
    private static final String UALOGP_116 = "UALOGP_116";
    private static final String UALOGP_117 = "UALOGP_117";
    private static final String UALOGP_118 = "UALOGP_118";
    private static final String UALOGP_119 = "UALOGP_119";
    private static final String UALOGP_120 = "UALOGP_120";
    
    private Map<String,Integer> UALOGP_Count = new LinkedHashMap<String,Integer>();
    private Map<String,Double> UALOGP_LogP = new LinkedHashMap<String,Double>();
    private Map<String,Double> UALOGP_MR = new LinkedHashMap<String,Double>();

    public UALOGPDescriptor() {
        UALOGP_LogP.put(UALOGP_1a, 0.642);
        UALOGP_LogP.put(UALOGP_1b, 0.0099);
        UALOGP_LogP.put(UALOGP_1c, 0.4377);
        UALOGP_LogP.put(UALOGP_1d, 0.0513);
        UALOGP_LogP.put(UALOGP_1e, 0.3411);
        UALOGP_LogP.put(UALOGP_2a, 0.4562);
        UALOGP_LogP.put(UALOGP_2b, 0.0348);
        UALOGP_LogP.put(UALOGP_2c, 0.32);
        UALOGP_LogP.put(UALOGP_2d, 0.0624);
        UALOGP_LogP.put(UALOGP_2e, 2.556);
        UALOGP_LogP.put(UALOGP_2f, -0.288);
        UALOGP_LogP.put(UALOGP_3a, 0.066);
        UALOGP_LogP.put(UALOGP_3b, -0.1447);
        UALOGP_LogP.put(UALOGP_3c, -0.0021);
        UALOGP_LogP.put(UALOGP_3d, -0.1309);
        UALOGP_LogP.put(UALOGP_3e, -0.0343);
        UALOGP_LogP.put(UALOGP_3f, -0.3061);
        UALOGP_LogP.put(UALOGP_5, 0.1023);
        UALOGP_LogP.put(UALOGP_6a, 0.0116);
        UALOGP_LogP.put(UALOGP_6b, -0.2018);
        UALOGP_LogP.put(UALOGP_7, 0.0055);
        UALOGP_LogP.put(UALOGP_8a, -0.0504);
        UALOGP_LogP.put(UALOGP_8b, -0.1571);
        UALOGP_LogP.put(UALOGP_8c, -0.0139);
        UALOGP_LogP.put(UALOGP_8d, -0.1433);
        UALOGP_LogP.put(UALOGP_8e, -0.0467);
        UALOGP_LogP.put(UALOGP_8f, -0.3185);
        UALOGP_LogP.put(UALOGP_9a, 0.1322);
        UALOGP_LogP.put(UALOGP_9b, 0.1576);
        UALOGP_LogP.put(UALOGP_10, 0.7184);
        UALOGP_LogP.put(UALOGP_15a, 0.4736);
        UALOGP_LogP.put(UALOGP_15b, -0.8237);
        UALOGP_LogP.put(UALOGP_16a, 0.2339);
        UALOGP_LogP.put(UALOGP_16b, 0.1272);
        UALOGP_LogP.put(UALOGP_16c, 0.2698);
        UALOGP_LogP.put(UALOGP_16d, 0.141);
        UALOGP_LogP.put(UALOGP_16e, 0.2376);
        UALOGP_LogP.put(UALOGP_18a, -0.2871);
        UALOGP_LogP.put(UALOGP_18b, -0.8422);
        UALOGP_LogP.put(UALOGP_21a, 0.9877);
        UALOGP_LogP.put(UALOGP_21b, 0.4326);
        UALOGP_LogP.put(UALOGP_24a, 0.305);
        UALOGP_LogP.put(UALOGP_24b, 0.1983);
        UALOGP_LogP.put(UALOGP_27a, 0.5785);
        UALOGP_LogP.put(UALOGP_27b, -0.0366);
        UALOGP_LogP.put(UALOGP_27c, 0.5239);
        UALOGP_LogP.put(UALOGP_30, 0.1443);
        UALOGP_LogP.put(UALOGP_33a, 1.0322);
        UALOGP_LogP.put(UALOGP_33b, 0.1511);
        UALOGP_LogP.put(UALOGP_36a, 0.386);
        UALOGP_LogP.put(UALOGP_36b, 0.1691);
        UALOGP_LogP.put(UALOGP_36c, 0.3914);
        UALOGP_LogP.put(UALOGP_37a, -0.0615);
        UALOGP_LogP.put(UALOGP_37b, 0.499);
        UALOGP_LogP.put(UALOGP_42, -0.2518);
        UALOGP_LogP.put(UALOGP_56, -0.4603);
        UALOGP_LogP.put(UALOGP_57, -0.1163);
        UALOGP_LogP.put(UALOGP_64, 0.5565);
        UALOGP_LogP.put(UALOGP_66, -0.7499);
        UALOGP_LogP.put(UALOGP_67, -0.4204);
        UALOGP_LogP.put(UALOGP_69, -0.5955);
        UALOGP_LogP.put(UALOGP_70, -0.1425);
        UALOGP_LogP.put(UALOGP_72, -0.6149);
        UALOGP_LogP.put(UALOGP_73, 0.0223);
        UALOGP_LogP.put(UALOGP_74, 0.0313);
        UALOGP_LogP.put(UALOGP_79, -1.5475);
        UALOGP_LogP.put(UALOGP_106, 0.511);
        UALOGP_LogP.put(UALOGP_118, -0.9002);
        UALOGP_LogP.put(UALOGP_119, 0.5669);

        UALOGP_MR.put(UALOGP_1a, 5.5021);
        UALOGP_MR.put(UALOGP_1b, 5.4244);
        UALOGP_MR.put(UALOGP_1c, 5.7325);
        UALOGP_MR.put(UALOGP_1d, 5.8987);
        UALOGP_MR.put(UALOGP_1e, 5.2783);
        UALOGP_MR.put(UALOGP_2a, 4.601);
        UALOGP_MR.put(UALOGP_2b, 4.5492);
        UALOGP_MR.put(UALOGP_2c, 4.7546);
        UALOGP_MR.put(UALOGP_2d, 4.8654);
        UALOGP_MR.put(UALOGP_2e, 4.4518);
        UALOGP_MR.put(UALOGP_2f, 0.0);
        UALOGP_MR.put(UALOGP_3a, 3.6475);
        UALOGP_MR.put(UALOGP_3b, 3.6216);
        UALOGP_MR.put(UALOGP_3c, 3.7243);
        UALOGP_MR.put(UALOGP_3d, 3.7797);
        UALOGP_MR.put(UALOGP_3e, 3.5729);
        UALOGP_MR.put(UALOGP_3f, 0.0);
        UALOGP_MR.put(UALOGP_5, 5.6967);
        UALOGP_MR.put(UALOGP_6a, 4.7122);
        UALOGP_MR.put(UALOGP_6b, 4.562);
        UALOGP_MR.put(UALOGP_7, 4.2339);
        UALOGP_MR.put(UALOGP_8a, 3.3979);
        UALOGP_MR.put(UALOGP_8b, 3.3228);
        UALOGP_MR.put(UALOGP_8c, 3.4255);
        UALOGP_MR.put(UALOGP_8d, 3.4809);
        UALOGP_MR.put(UALOGP_8e, 3.2741);
        UALOGP_MR.put(UALOGP_8f, 0.0);
        UALOGP_MR.put(UALOGP_9a, 3.1775);
        UALOGP_MR.put(UALOGP_9b, 3.1958);
        UALOGP_MR.put(UALOGP_10, 3.3879);
        UALOGP_MR.put(UALOGP_15a, 4.9879);
        UALOGP_MR.put(UALOGP_15b, 4.8641);
        UALOGP_MR.put(UALOGP_16a, 5.1593);
        UALOGP_MR.put(UALOGP_16b, 5.0842);
        UALOGP_MR.put(UALOGP_16c, 5.1869);
        UALOGP_MR.put(UALOGP_16d, 5.2423);
        UALOGP_MR.put(UALOGP_16e, 5.0355);
        UALOGP_MR.put(UALOGP_18a, 4.401);
        UALOGP_MR.put(UALOGP_18b, 4.4325);
        UALOGP_MR.put(UALOGP_21a, 4.283);
        UALOGP_MR.put(UALOGP_21b, 4.3145);
        UALOGP_MR.put(UALOGP_24a, 4.3433);
        UALOGP_MR.put(UALOGP_24b, 4.2682);
        UALOGP_MR.put(UALOGP_27a, 3.3014);
        UALOGP_MR.put(UALOGP_27b, 3.3329);
        UALOGP_MR.put(UALOGP_27c, 3.3197);
        UALOGP_MR.put(UALOGP_30, 3.3329);
        UALOGP_MR.put(UALOGP_33a, 4.2676);
        UALOGP_MR.put(UALOGP_33b, 4.2859);
        UALOGP_MR.put(UALOGP_36a, 4.8195);
        UALOGP_MR.put(UALOGP_36b, 4.851);
        UALOGP_MR.put(UALOGP_36c, 4.8378);
        UALOGP_MR.put(UALOGP_37a, 5.609);
        UALOGP_MR.put(UALOGP_37b, 5.5958);
        UALOGP_MR.put(UALOGP_42, 3.6104);
        UALOGP_MR.put(UALOGP_56, 2.2646);
        UALOGP_MR.put(UALOGP_57, 2.2778);
        UALOGP_MR.put(UALOGP_64, 11.9366);
        UALOGP_MR.put(UALOGP_66, 4.2221);
        UALOGP_MR.put(UALOGP_67, 3.3);
        UALOGP_MR.put(UALOGP_69, 5.2841);
        UALOGP_MR.put(UALOGP_70, 0.0);
        UALOGP_MR.put(UALOGP_72, 3.3);
        UALOGP_MR.put(UALOGP_73, 3.5956);
        UALOGP_MR.put(UALOGP_74, 3.5);
        UALOGP_MR.put(UALOGP_79, 0.0);
        UALOGP_MR.put(UALOGP_106, 8.6916);
        UALOGP_MR.put(UALOGP_118, 0.0);
        UALOGP_MR.put(UALOGP_119, 0.0);
    }

    @Override
    public DescriptorSpecification getSpecification() {
        return new DescriptorSpecification(
                "UALOGP",
                this.getClass().getName(),
                "$Id: UALOGPDescriptor.java 1 2012-03-16 16:00:00Z yapchunwei $",
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
    
        UALOGP_Count = new LinkedHashMap<String,Integer>();
        UALOGP_Count.put(UALOGP_1a, 0);
        UALOGP_Count.put(UALOGP_1b, 0);
        UALOGP_Count.put(UALOGP_1c, 0);
        UALOGP_Count.put(UALOGP_1d, 0);
        UALOGP_Count.put(UALOGP_1e, 0);
        UALOGP_Count.put(UALOGP_2a, 0);
        UALOGP_Count.put(UALOGP_2b, 0);
        UALOGP_Count.put(UALOGP_2c, 0);
        UALOGP_Count.put(UALOGP_2d, 0);
        UALOGP_Count.put(UALOGP_2e, 0);
        UALOGP_Count.put(UALOGP_2f, 0);
        UALOGP_Count.put(UALOGP_3a, 0);
        UALOGP_Count.put(UALOGP_3b, 0);
        UALOGP_Count.put(UALOGP_3c, 0);
        UALOGP_Count.put(UALOGP_3d, 0);
        UALOGP_Count.put(UALOGP_3e, 0);
        UALOGP_Count.put(UALOGP_3f, 0);
        UALOGP_Count.put(UALOGP_5, 0);
        UALOGP_Count.put(UALOGP_6a, 0);
        UALOGP_Count.put(UALOGP_6b, 0);
        UALOGP_Count.put(UALOGP_7, 0);
        UALOGP_Count.put(UALOGP_8a, 0);
        UALOGP_Count.put(UALOGP_8b, 0);
        UALOGP_Count.put(UALOGP_8c, 0);
        UALOGP_Count.put(UALOGP_8d, 0);
        UALOGP_Count.put(UALOGP_8e, 0);
        UALOGP_Count.put(UALOGP_8f, 0);
        UALOGP_Count.put(UALOGP_9a, 0);
        UALOGP_Count.put(UALOGP_9b, 0);
        UALOGP_Count.put(UALOGP_10, 0);
        UALOGP_Count.put(UALOGP_15a, 0);
        UALOGP_Count.put(UALOGP_15b, 0);
        UALOGP_Count.put(UALOGP_16a, 0);
        UALOGP_Count.put(UALOGP_16b, 0);
        UALOGP_Count.put(UALOGP_16c, 0);
        UALOGP_Count.put(UALOGP_16d, 0);
        UALOGP_Count.put(UALOGP_16e, 0);
        UALOGP_Count.put(UALOGP_18a, 0);
        UALOGP_Count.put(UALOGP_18b, 0);
        UALOGP_Count.put(UALOGP_21a, 0);
        UALOGP_Count.put(UALOGP_21b, 0);
        UALOGP_Count.put(UALOGP_24a, 0);
        UALOGP_Count.put(UALOGP_24b, 0);
        UALOGP_Count.put(UALOGP_27a, 0);
        UALOGP_Count.put(UALOGP_27b, 0);
        UALOGP_Count.put(UALOGP_27c, 0);
        UALOGP_Count.put(UALOGP_30, 0);
        UALOGP_Count.put(UALOGP_33a, 0);
        UALOGP_Count.put(UALOGP_33b, 0);
        UALOGP_Count.put(UALOGP_36a, 0);
        UALOGP_Count.put(UALOGP_36b, 0);
        UALOGP_Count.put(UALOGP_36c, 0);
        UALOGP_Count.put(UALOGP_37a, 0);
        UALOGP_Count.put(UALOGP_37b, 0);
        UALOGP_Count.put(UALOGP_42, 0);
        UALOGP_Count.put(UALOGP_56, 0);
        UALOGP_Count.put(UALOGP_57, 0);
        UALOGP_Count.put(UALOGP_64, 0);
        UALOGP_Count.put(UALOGP_66, 0);
        UALOGP_Count.put(UALOGP_67, 0);
        UALOGP_Count.put(UALOGP_69, 0);
        UALOGP_Count.put(UALOGP_70, 0);
        UALOGP_Count.put(UALOGP_72, 0);
        UALOGP_Count.put(UALOGP_73, 0);
        UALOGP_Count.put(UALOGP_74, 0);
        UALOGP_Count.put(UALOGP_79, 0);
        UALOGP_Count.put(UALOGP_106, 0);
        UALOGP_Count.put(UALOGP_118, 0);
        UALOGP_Count.put(UALOGP_119, 0);

        int maxAtoms = container.getAtomCount();
        for (int i=0, endi=maxAtoms; i<endi; ++i)
        {
            IAtom atom = container.getAtom(i);
            if (atom.getSymbol().equals("C")) 
            {
                
            }
            else if (atom.getSymbol().equals("O")) 
            {
                
            }
            else if (atom.getSymbol().equals("Se")) 
            {
                
            }
            else if (atom.getSymbol().equals("N")) 
            {
                
            }
            else if (atom.getSymbol().equals("H")) 
            {
                
            }
            else if (atom.getSymbol().equals("P"))
            {
                
            }
        }
        
        
        double logP = 0.0;
        double MR = 0.0;
        DoubleArrayResult retval = new DoubleArrayResult();
        retval.add(logP);
        retval.add(MR);

        return new DescriptorValue(getSpecification(), getParameterNames(), getParameters(), retval, names);        
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
       
    boolean IsType1to14(IAtomContainer ac, IAtom atom, int nH, int nR, int nX)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxH = 0;
            int maxC = 0;
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("H")) ++maxH;
                else if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxC;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxX;
            }
            if (maxH==nH && maxC==nR && maxX==nX)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
      
    boolean IsType15to20(IAtomContainer ac, IAtom atom, int nH, int nR, int nX)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            boolean hasDoubleBond = false;
            int maxH = 0;
            int maxC = 0;
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (ac.getBond(atom, neighbour).getOrder()==Order.DOUBLE) hasDoubleBond = true;
                else if (neighbour.getSymbol().equals("H")) ++maxH;
                else if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxC;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxX;
            }
            if (hasDoubleBond && maxH==nH && maxC==nR && maxX==nX)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    // For triple bonds CH, CR or CX
    boolean IsType21to23(IAtomContainer ac, IAtom atom, int nH, int nR, int nX)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            boolean hasTripleBond = false;
            int maxH = 0;
            int maxC = 0;
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (ac.getBond(atom, neighbour).getOrder()==Order.TRIPLE) hasTripleBond = true;
                else if (neighbour.getSymbol().equals("H")) ++maxH;
                else if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxC;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) ++maxX;
            }
            if (hasTripleBond && maxH==nH && maxC==nR && maxX==nX)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    // For R=C=R
    boolean IsType22(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxC = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.DOUBLE) ++maxC;
            }
            if (maxC==2)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    boolean IsType24to35_42to44(IAtomContainer ac, IAtom atom, String left, String middle, String right)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C") && atom.isAromatic())
        {
            boolean hasLeft = false;
            boolean hasMiddle = false;
            boolean hasRight = false;
            boolean leftX = left.equals("X");
            boolean middleX = middle.equals("X");
            boolean rightX = right.equals("X");
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if ((leftX ? IsX(neighbour) : neighbour.getSymbol().equals(left)) && neighbour.isAromatic()) hasLeft = true;
                else if ((middleX ? IsX(neighbour) : neighbour.getSymbol().equals(middle)) && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE) hasMiddle = true;
                else if ((rightX ? IsX(neighbour) : neighbour.getSymbol().equals(right)) && neighbour.isAromatic()) hasRight = true;
            }
            if (hasLeft && hasMiddle && hasRight)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    boolean IsType36(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            boolean hasLeft = false;
            boolean hasMiddle = false;
            boolean hasRight = false;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE && !neighbour.isAromatic()) hasLeft = true;
                else if (neighbour.getSymbol().equals("H")) hasMiddle = true;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.DOUBLE) hasRight = true;
            }
            if (hasLeft && hasMiddle && hasRight)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    boolean IsType37(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            boolean hasLeft = false;
            boolean hasMiddle = false;
            boolean hasRight = false;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C") && neighbour.isAromatic()) hasLeft = true;
                else if (neighbour.getSymbol().equals("H")) hasMiddle = true;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.DOUBLE) hasRight = true;
            }
            if (hasLeft && hasMiddle && hasRight)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    boolean IsType38(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            boolean hasLeft = false;
            boolean hasMiddle = false;
            boolean hasRight = false;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE && !neighbour.isAromatic()) hasLeft = true;
                else if (IsX(neighbour) && ac.getBond(atom, neighbour).getOrder()==Order.DOUBLE) hasRight = true;
                else if (neighbour.getSymbol().equals("C") && ac.getBond(atom, neighbour).getOrder()==Order.SINGLE && !neighbour.isAromatic()) hasRight = true;
            }
            if (hasLeft && hasMiddle && hasRight)
            {
                isAtomType = true;
            }
        }
        return isAtomType;
    }
    
    boolean IsC0sp3X0(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb)) ++maxX;
                    }                    
                }
            }
            if (maxX==0)
            {
                isAtomType = true;
            }
        }        
        return isAtomType;
    }
    
    boolean IsAlphaC(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb))
                        {
                            Order bondOrder = ac.getBond(nb, neighbour).getOrder();
                            if (bondOrder==Order.DOUBLE ||
                                bondOrder==Order.TRIPLE ||
                                (nb.isAromatic() && neighbour.isAromatic()))
                            {
                                isAtomType = true;
                                break;
                            }
                        }                           
                    }                    
                }
                if (isAtomType) break;
            }
        }        
        return isAtomType;
    }
    
    boolean IsC0sp3X1(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb)) ++maxX;
                    }                    
                }
            }
            if (maxX==1)
            {
                isAtomType = true;
            }
        }        
        return isAtomType;
    }
    
    boolean IsC0sp3X2(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb)) ++maxX;
                    }                    
                }
            }
            if (maxX==2)
            {
                isAtomType = true;
            }
        }        
        return isAtomType;
    }
    
    boolean IsC0sp3X3(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb)) ++maxX;
                    }                    
                }
            }
            if (maxX==3)
            {
                isAtomType = true;
            }
        }        
        return isAtomType;
    }
    
    boolean IsC0sp3X4(IAtomContainer ac, IAtom atom)
    {
        boolean isAtomType = false;
        if (atom.getSymbol().equals("C"))
        {
            int maxX = 0;
            for (IAtom neighbour : ac.getConnectedAtomsList(atom))
            {
                if (neighbour.getSymbol().equals("C"))
                {                    
                    for (IAtom nb : ac.getConnectedAtomsList(neighbour))
                    {
                        if (IsX(nb)) ++maxX;
                    }                    
                }
            }
            if (maxX>=4)
            {
                isAtomType = true;
            }
        }        
        return isAtomType;
    }
    
//    boolean IsC0sp3X4(IAtomContainer ac, IAtom atom)
//    {
//        boolean isAtomType = false;
//        if (atom.getSymbol().equals("C"))
//        {
//            List<IAtom> neighbours = ac.getConnectedAtomsList(atom);
//            for (IAtom neighbour : neighbours)
//            {
//                if (neighbour.getSymbol().equals("C"))
//                {
//                    int maxX = 0;
//                    List<IAtom> nbs = ac.getConnectedAtomsList(neighbour);
//                    for (IAtom nb : nbs)
//                    {
//                        if (IsX(nb)) ++maxX;
//                    }
//                    if (maxX==4)
//                    {
//                        isAtomType = true;
//                    }
//                    else if (maxX>4) 
//                    {
//                        isAtomType = false;
//                        break;
//                    }
//                }
//            }
//        }        
//        return isAtomType;
//    }
    
    boolean IsX(IAtom atom)
    {
        if (atom.getSymbol().equals("O") ||
            atom.getSymbol().equals("N") ||
            atom.getSymbol().equals("S") ||
            atom.getSymbol().equals("P") ||
            atom.getSymbol().equals("Se") ||
            atom.getSymbol().equals("F") ||
            atom.getSymbol().equals("Cl") ||
            atom.getSymbol().equals("Br") ||
            atom.getSymbol().equals("I"))
        {
            return true;
        }
        else 
        {
            return false;
        }        
    }
    


    @Override
    public void initialise(org.openscience.cdk.interfaces.IChemObjectBuilder builder) {
        // No descriptor-specific initialisation required.
    }

}

