package defind;

/*-
 * #%L
 * DeFind
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Some Organisation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DNFConverter {
    static void regr(List<List<OWLClassExpression>> lists, List<List<OWLClassExpression>> result, int depth, List<OWLClassExpression> current) {
        if (depth == lists.size()) {
            result.add(current);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); ++i) {
            ArrayList<OWLClassExpression> clone = new ArrayList<>(current);
            clone.add(lists.get(depth).get(i));
            regr(lists, result, depth + 1, clone);
        }
    }

    static List<List<OWLClassExpression>> regroup(List<List<OWLClassExpression>> lists) {
        List<OWLClassExpression> current = new ArrayList<>();
        List<List<OWLClassExpression>> result = new ArrayList<>();
        regr(lists, result, 0, current);
        return result;
    }

    //OIO(OUO(A1,A2),OUO(B1,B2)) => OUO ( OIO(A_i,B_j) )
    static OWLClassExpression process(OWLObjectIntersectionOf oio) {
        boolean allOperandsAreUnions = true;
        boolean noUnions = true;
        List<List<OWLClassExpression>> arr = new ArrayList<>();
        for (OWLClassExpression exp : oio.getOperands()) {
            if (exp instanceof OWLObjectUnionOf) noUnions = false;
            if (!(exp instanceof OWLObjectUnionOf || exp instanceof OWLClass)) {
                allOperandsAreUnions = false;
                break;
            } else {
                if (exp instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf oouo = (OWLObjectUnionOf) exp;
                    List<OWLClassExpression> operands = oouo.getOperandsAsList();
                    arr.add(operands);
                } else {
                    List<OWLClassExpression> ar = new ArrayList<>();
                    ar.add(exp);
                    arr.add(ar);
                }
            }
        }
        if (!allOperandsAreUnions) {
            return oio;
        } else {
            List<List<OWLClassExpression>> planeArr = regroup(arr);
            Set<OWLClassExpression> uset = new HashSet<>();
            for (List<OWLClassExpression> aibi : planeArr) {
                Set<OWLClassExpression> iParams = new HashSet<>();
                iParams.addAll(aibi);
                OWLClassExpression newClass = new OWLObjectIntersectionOfImpl(iParams);
                if (iParams.size()==1) {
                    newClass = iParams.iterator().next();
                }
                if (noUnions) {
                    uset.add(newClass);
                } else {
                    OWLClassExpression oio1 = handleRecursively(newClass);
                    uset.add(oio1);
                }
            }
            if (uset.size()==1) {
                return uset.iterator().next();
            }
            return new OWLObjectUnionOfImpl(uset);
        }
    }

    //OSVF(r,OUO(A1..An)) => OUO(OSVF(r,A1),OSVF(r,A2),OSVF(r,An))
    static OWLClassExpression process(OWLObjectSomeValuesFrom inters) {
        OWLClassExpression filler = inters.getFiller();
        if (filler instanceof OWLObjectUnionOf) {
            OWLObjectUnionOf ouo = (OWLObjectUnionOf) filler;
            HashSet<OWLClassExpression> osvfs = new HashSet<>();
            for (OWLClassExpression op : ouo.getOperands()) {
                osvfs.add(new OWLObjectSomeValuesFromImpl(inters.getProperty(), op));
            }
            OWLObjectUnionOfImpl ret = new OWLObjectUnionOfImpl(osvfs);
            return ret;
        }
        return inters;
    }

    //OIO(OUO(A1,A2),OUO(B1,B2)) => OUO ( OIO(A_i,B_j) )
    static OWLClassExpression handleRecursively(OWLClassExpression exp) {
        //System.out.println("handleRecursively " + exp.toString().replace(Calc.url,""));
        if (exp instanceof OWLObjectIntersectionOf) {
            OWLObjectIntersectionOf oio = (OWLObjectIntersectionOf) exp;
            Set<OWLClassExpression> ops = oio.getOperands();
            Set<OWLClassExpression> newOps = new HashSet<>();
            ops.forEach(owlClassExpression -> {
                OWLClassExpression updExp = handleRecursively(owlClassExpression);
                if (updExp instanceof OWLObjectIntersectionOf) {
                    newOps.addAll(((OWLObjectIntersectionOf)updExp).getOperands());
                } else {
                    newOps.add(owlClassExpression);
                }
            });
            if (newOps.size()==0) {
                return new OWLObjectUnionOfImpl(newOps);
            }
            OWLClassExpression res = process(new OWLObjectIntersectionOfImpl(newOps));
            return res;
        } else if (exp instanceof OWLObjectSomeValuesFrom) {// OSVF(r,OUO(A1..An)) => OUO(OSVF(r,A1),OSVF(r,A2) ... OSVF(r,An))
            OWLObjectSomeValuesFrom osvf = (OWLObjectSomeValuesFrom) exp;
            OWLClassExpression child = osvf.getFiller();
            OWLClassExpression updChild = handleRecursively(child);
            OWLObjectSomeValuesFromImpl osvf1 = new OWLObjectSomeValuesFromImpl(osvf.getProperty(), updChild);
            OWLClassExpression res = process(osvf1);
            return res;
        } else if (exp instanceof OWLObjectUnionOf) {
            OWLObjectUnionOf ouo = (OWLObjectUnionOf) exp;
            Set<OWLClassExpression> ops = ouo.getOperands();
            Set<OWLClassExpression> newOps = new HashSet<>();
            ops.forEach(owlClassExpression -> {
                OWLClassExpression updExp = handleRecursively(owlClassExpression);
                if (updExp instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf u2 = (OWLObjectUnionOf) updExp;
                    newOps.addAll(u2.getOperands());
                } else {
                    newOps.add(updExp);
                }
            });
            if (newOps.size()==1) {
                return newOps.iterator().next();
            }
            OWLObjectUnionOfImpl ouo1 = new OWLObjectUnionOfImpl(newOps);
            //System.out.println("for " + exp.toString().replace(Calc.url,"") + " returns " + ouo1.toString().replace(Calc.url,""));
            return ouo1;
        }
        return exp;
    }

    public static OWLClassExpression toDNF(OWLClassExpression src) {
        OWLClassExpression res = handleRecursively(src);
        return res;
    }
}
