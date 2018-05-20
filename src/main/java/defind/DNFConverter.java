package defind;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

import java.util.*;
import java.util.function.Consumer;

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

    static String toStr(OWLClassExpression e){
        return e.toString().replaceAll("http://www.semanticweb.org/denis/ontologies/2017/10/untitled-ontology-282","").replaceAll("ObjectIntersectionOf","I")
                .replaceAll("ObjectUnionOf","U").replaceAll("ObjectSomeValuesFrom","OSVF");
    }

    static List<List<OWLClassExpression>> regroup(List<List<OWLClassExpression>> lists) {
        List<OWLClassExpression> current = new ArrayList<>();
        List<List<OWLClassExpression>> result = new ArrayList<>();
        regr(lists, result, 0, current);
        return result;
    }
    // U(A,B) -> A, I(A) -> A
    static OWLClassExpression expandSimple(OWLClassExpression e){
        if (e instanceof  OWLObjectUnionOf) {
            OWLObjectUnionOf u = (OWLObjectUnionOf) e;
            if (u.getOperands().size()==1) {
                return expandSimple(u.getOperands().iterator().next());
            }
        } else if (e instanceof OWLObjectIntersectionOf){
            OWLObjectIntersectionOf i = (OWLObjectIntersectionOf) e;
            if (i.getOperands().size()==1) {
                return expandSimple(i.getOperands().iterator().next());
            }
        }
        return (e);
    }

    //OIO(OUO(A1,A2),OUO(B1,B2)) => OUO ( OIO(A_i,B_j) )
    static OWLClassExpression process(OWLObjectIntersectionOf oio,Map<OWLClassExpression,OWLClassExpression> cache) {
        if (cache.containsKey(oio)) return cache.get(oio);
        List<List<OWLClassExpression>> arr = new ArrayList<>();
        for (OWLClassExpression exp : oio.getOperands()) {
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
        List<List<OWLClassExpression>> planeArr = regroup(arr);
        Set<OWLClassExpression> uset = new HashSet<>();
        for (List<OWLClassExpression> aibi : planeArr) {
            Set<OWLClassExpression> iParams = new HashSet<>(aibi);
            OWLClassExpression newClass = new OWLObjectIntersectionOfImpl(iParams);
            if (iParams.size() == 1) {
                newClass = iParams.iterator().next();
            }
            //OWLClassExpression oio1 = handleRecursively(newClass,cache);
            uset.add(newClass);
        }
        OWLClassExpression ret;
        if (uset.size() == 1) {
            ret= expandSimple(uset.iterator().next());
        } else {
            ret = new OWLObjectUnionOfImpl(uset);
        }
        cache.put(oio,ret);
        return ret;
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
    static OWLClassExpression handleRecursively(OWLClassExpression exp0,Map<OWLClassExpression,OWLClassExpression> cache) {
        //System.out.println("process:"+toStr(exp0));
        if (cache.containsKey(exp0)) {
            return cache.get(exp0);
        }
        OWLClassExpression exp = expandSimple(exp0);
        OWLClassExpression res = exp;
        Set<OWLClassExpression> parts = new HashSet<>();
        //System.out.println("handleRecursively " + exp.toString().replace(Calc.url,""));
        if (exp instanceof OWLObjectIntersectionOf) {
            OWLObjectIntersectionOf oio = (OWLObjectIntersectionOf) exp;
            Set<OWLClassExpression> ops = oio.getOperands();
            Set<OWLClassExpression> newOps = new HashSet<>(),ops2=new HashSet<>();
            ops.forEach(owlClassExpression -> {
                OWLClassExpression updExp = handleRecursively(owlClassExpression,cache);
                if (updExp instanceof OWLObjectIntersectionOf) {
                    newOps.addAll(((OWLObjectIntersectionOf)updExp).getOperands());
                } else {
                    newOps.add(owlClassExpression);
                }
            });
            newOps.forEach(op->{
                ops2.add(handleRecursively(op,cache));
            });
            parts.addAll(ops2);
            if (ops2.size()==0) {
                res= new OWLObjectUnionOfImpl(ops2);
            } else {
                OWLObjectIntersectionOfImpl newObj = new OWLObjectIntersectionOfImpl(ops2);
                res = process(newObj, cache);
            }
        } else if (exp instanceof OWLObjectSomeValuesFrom) {// OSVF(r,OUO(A1..An)) => OUO(OSVF(r,A1),OSVF(r,A2) ... OSVF(r,An))
            OWLObjectSomeValuesFrom osvf = (OWLObjectSomeValuesFrom) exp;
            OWLClassExpression child = osvf.getFiller();
            OWLClassExpression updChild = handleRecursively(child,cache);
            OWLObjectSomeValuesFromImpl osvf1 = new OWLObjectSomeValuesFromImpl(osvf.getProperty(), updChild);
            res = process(osvf1);
        } else if (exp instanceof OWLObjectUnionOf) {
            OWLObjectUnionOf ouo = (OWLObjectUnionOf) exp;
            Set<OWLClassExpression> ops = ouo.getOperands();
            Set<OWLClassExpression> newOps = new HashSet<>();
            ops.forEach(owlClassExpression -> {
                OWLClassExpression updExp = handleRecursively(owlClassExpression,cache);
                if (updExp instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf u2 = (OWLObjectUnionOf) updExp;
                    newOps.addAll(u2.getOperands());
                } else {
                    newOps.add(updExp);
                }
            });
            if (newOps.size() == 1) {
                res= newOps.iterator().next();
            } else {
                res = new OWLObjectUnionOfImpl(newOps);
            }
        }
        cache.put(exp0,res);

        System.out.println("\nConverting " + toStr(exp0) + " => ");
        System.out.println("           " + toStr(res));
        for(OWLClassExpression e:parts) {
            System.out.println("              part="+toStr(e));
        }
        return res;
    }

    public static OWLClassExpression toDNF(OWLClassExpression src) {
        Map<OWLClassExpression,OWLClassExpression> cache = new HashMap<>();
        OWLClassExpression res = handleRecursively(src,cache);
        System.out.println(toStr(res));
        return res;
    }
}
