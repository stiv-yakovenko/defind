package defind;


import org.liveontologies.protege.explanation.proof.MyProofServiceManager;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.Proof;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import uk.ac.manchester.cs.owl.owlapi.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Calc {

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Load your ontology.
        Set<OWLNamedObject> delta = new HashSet<>();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\steve\\Dropbox\\Projects\\git\\protege_workspace\\protege-master\\omit_cyclic_inferences.owl"));
        String url = ont.getOntologyID().getDefaultDocumentIRI().get().toString();//"http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#";
        delta.add(new OWLClassImpl(IRI.create(url + "#", "C")));
        delta.add(new OWLClassImpl(IRI.create(url + "#", "D")));
        System.out.println("D=" + delta.toString().replaceAll(url + "X", ""));
    }

    public static OWLAxiom addClassAsterix(OWLOntologyManager manager, OWLClassExpression c, Set<OWLNamedObject> delta, OWLOntology[] ont) throws OWLOntologyCreationException {
        ont[0] = manager.createOntology();
        OWLDataFactory fucktory = manager.getOWLDataFactory();
        OWLSubClassOfAxiom axiom = fucktory.getOWLSubClassOfAxiom(c, c);
        manager.addAxiom(ont[0], axiom);
        performRename(manager, ont[0], delta);
        OWLAxiom cls = ont[0].getAxioms().iterator().next();
        OWLClassExpression c_ = ((OWLSubClassOfAxiomImpl) cls).getSubClass();
        System.out.println("c=" + c.toString());
        System.out.println("c_=" + c_.toString());
        return fucktory.getOWLSubClassOfAxiom(c, c_);
    }

    static void printOntology(OWLOntology ont) {
        int i = 0;
        for (OWLAxiom a : ont.getAxioms()) {
            System.out.println(i + ":" + a.toString());
            i++;
        }
    }

    public static void launchReasoner(OWLModelManager modelManager) {
        OWLReasonerManager owlReasonerManager = modelManager.getOWLReasonerManager();
        owlReasonerManager.classifyAsynchronously(owlReasonerManager.getReasonerPreferences().getPrecomputedInferences());
        Thread parent = Thread.currentThread();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (owlReasonerManager.getReasonerStatus() == ReasonerStatus.INITIALIZED) {
                    synchronized (parent) {
                        parent.notify();
                    }
                    break;
                }
            }
        }).start();
        try {
            synchronized (parent) {
                parent.wait();
            }
        } catch (InterruptedException e) {
        }
    }

    public Object invoke(OWLModelManager modelManager, OWLEditorKit owlEditorKit, OWLAxiom cIsLessC_) {
        launchReasoner(modelManager);
        MyProofServiceManager proofServiceManager;
        try {
            proofServiceManager = MyProofServiceManager.get(owlEditorKit);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Collection<ProofService> proofServices = proofServiceManager.getProofServices();
        if (proofServices == null || proofServices.size() == 0) {
            System.out.println("No proof service");
        }
        ProofService proofService = proofServices.iterator().next();
        if (proofService == null) {
            return null;
        }

        try {
            proofService.initialise();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DynamicProof<Inference<? extends OWLAxiom>> proof = proofService.getProof(cIsLessC_);
        OWLReasonerManager reasonerManager = owlEditorKit.getOWLModelManager().getOWLReasonerManager();
        reasonerManager.getCurrentReasoner().flush();
        //launchReasoner(modelManager);
        try {
            proofService.dispose();
        } catch (Exception e) {
        }
        return proof;
    }

    public Object solve(OWLOntology srcOnt, Set<OWLNamedObject> delta, OWLClassExpression c,
                        OWLEditorKit owlEditorKit, OWLOntologyManager manager, OWLModelManager modelManager) throws OWLOntologyCreationException {
        Set<OWLAxiom> srcAxioms = srcOnt.getAxioms();
        System.out.println("srcAxioms = " + srcAxioms.size());
        OWLOntology ont = manager.createOntology();
        manager.addAxioms(ont, srcOnt.getAxioms());
        OWLOntology ont1 = cloneWithAsterisk(manager, ont, delta);
        manager.addAxioms(ont, ont1.getAxioms());
        System.out.println("ont = " + ont.toString().replaceAll(Pattern.quote("http://www.semanticweb.org/denis/ontologies/2017/10/untitled-ontology-293"), ""));
        saveOnt(ont);
        printOntology(ont);
        OWLOntology ont2[] = new OWLOntology[1];
        OWLAxiom cIsLessC_ = addClassAsterix(manager, c, delta, ont2);
        System.out.println("cIsLessC_=" + cIsLessC_.toString());
        manager.removeAxioms(srcOnt, srcAxioms);
        manager.addAxioms(srcOnt, ont.getAxioms());
        Proof inferences = (Proof) invoke(modelManager, owlEditorKit, cIsLessC_);
        if (inferences == null) return null;
        System.out.println("inferences = " + inferences.getInferences(cIsLessC_).size());
        OWLClassExpression res = handle(cIsLessC_, inferences, delta, null, null, null,null, srcOnt, 0);
        OWLClassExpression res1 = DNFConverter.toDNF(res);
        manager.removeAxioms(srcOnt, ont.getAxioms());
        manager.addAxioms(srcOnt, srcAxioms);
        manager.removeOntology(ont);
        manager.removeOntology(ont1);
        manager.removeOntology(ont2[0]);
        if (owlEditorKit != null) {
            OWLReasonerManager reasonerManager = owlEditorKit.getOWLModelManager().getOWLReasonerManager();
            reasonerManager.getCurrentReasoner().flush();
        }
        System.out.println("getClassesInSignature=" + srcOnt.getClassesInSignature().size());
        System.out.println("imports=" + srcOnt.getImports().size());
        return res1;
    }

    private static void saveOnt(OWLOntology ont) {
        try {
            FileOutputStream outputStream = new FileOutputStream("ont-" + new Date().getTime() + ".xml");
            ont.saveOntology(outputStream);
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (OWLOntologyStorageException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static OWLOntology cloneWithAsterisk(OWLOntologyManager manager, OWLOntology ont, Set<OWLNamedObject> delta) throws OWLOntologyCreationException {
        OWLOntology ont1 = manager.createOntology();
        manager.addAxioms(ont1, ont.getAxioms());
        Set s = new HashSet();
        performRename(manager, ont1, delta);
        return ont1;
    }

    private static void performRename(OWLOntologyManager manager, OWLOntology ont1, Set<OWLNamedObject> delta) {
        Set s = new HashSet();
        s.add(ont1);
        Map<IRI, IRI> map = new HashMap<>();
        for (OWLObjectProperty oop : ont1.getObjectPropertiesInSignature()) {
            if (oop.isBuiltIn()) continue;
            if (delta.contains(oop)) continue;
            IRI iri = oop.getIRI();
            IRI iri1 = IRI.create(iri.getNamespace(), (iri.getRemainder()).get() + "_");
            map.put(iri, iri1);
        }
        for (OWLClass cls : ont1.getClassesInSignature()) {
            if (cls.isBuiltIn()) continue;
            if (delta.contains(cls)) continue;
            IRI iri = cls.getIRI();
            IRI iri1 = IRI.create(iri.getNamespace(), iri.getRemainder().get() + "_");
            map.put(iri, iri1);
        }
        for (IRI iri : map.keySet()) {
            List<OWLOntologyChange> owlOntologyChanges = new OWLEntityRenamer(manager, s).changeIRI(iri, map.get(iri));
            manager.applyChanges(owlOntologyChanges);
        }
    }

    static boolean isEmpty(Set<OWLClassExpression> e) {
        if (e.size() == 0) return true;
        for (OWLClassExpression el : e) {
            if (!isEmpty(el)) return false;
        }
        return true;
    }

    static boolean isEmpty(OWLClassExpression e) {
        if (e instanceof OWLObjectUnionOfImpl) {
            if (((OWLObjectUnionOfImpl) e).getOperands().size() == 0) {
                return true;
            }
        }
        return false;
    }

    static OWLAxiom getFirstNonProcessed(Set<OWLAxiom> premises, Map<OWLAxiom, Set<OWLClassExpression>> circles) {
        for (OWLAxiom premise : premises) {
            if (!circles.containsKey(premise)) {
                premises.remove(premise);
                return premise;
            }
        }
        return null;
    }

    static boolean allSymbolsInDelta(Set<OWLNamedObject> delta, Set<OWLEntity> objs) {
        for (OWLEntity o : objs) {
            if (o.isBuiltIn()) {
                continue;
            }
            if (!delta.contains(o)) return false;
        }
        return true;
    }

    static OWLClassExpression merge(Set<OWLClassExpression> union) {
        Set<OWLClassExpression> union2 = new HashSet();
        for (OWLClassExpression elem : union) {
            if (!isEmpty(elem)) {
                union2.add(elem);
            }
        }
        OWLClassExpression ret = new OWLObjectUnionOfImpl(union2);
        if (union2.size() == 1) {
            ret = union2.iterator().next();
        }
        return ret;
    }

    static String indent(int rec) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < rec; i++) {
            r.append("  ");
        }
        return r.toString();
    }

    static OWLClassExpression handle(OWLAxiom root, Proof proof, Set<OWLNamedObject> delta,
                                     Set<OWLAxiom> parents, Set<OWLAxiom> cyclic, Map<OWLAxiom, OWLClassExpression> cache,
                                     Inference<OWLAxiom> prevInf,
                                     OWLOntology ont, int rec) {
        String url = ont.getOntologyID().getDefaultDocumentIRI().get().toString();
        String rootStr = root.toString().replaceAll(url, "");
        if (parents == null) {
            parents = new HashSet<>();
            HashMap<OWLAxiom, OWLClassExpression> cch = new HashMap<>();
            cyclic = new HashSet<>();
            OWLClassExpression handle = handle(root, proof, delta, parents,cyclic, cch, prevInf, ont, 0);
            System.out.println(cch.size());
            return handle;
        }
        if (parents.contains(root)) {
            cyclic.addAll(parents);
            return new OWLObjectUnionOfImpl(new HashSet<>());
        }
        if (cache.containsKey(root)&&!cyclic.contains(root)) {
            System.out.print(indent(rec));
            OWLClassExpression val = cache.get(root);
            System.out.println("CUT " + rootStr + " " + val);
            return val;
        }
        System.out.print(indent(rec));
        System.out.println("ENTER " + rootStr);
        parents.add(root);
        Collection<? extends Inference<OWLAxiom>> inferences = proof.getInferences(root);
        Set<OWLClassExpression> union = new HashSet();
        if (inferences.size() == 0) {
            System.out.print(indent(rec));
            System.out.println("inferences.size() == 0");
            if (root instanceof OWLSubClassOfAxiomImpl) {
                OWLClassExpression superClass = ((OWLSubClassOfAxiomImpl) root).getSuperClass();
                Set<OWLEntity> signature = superClass.getSignature();
                if (allSymbolsInDelta(delta, signature)) {
                    union.add(superClass);
                } else {
                    union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                }
                System.out.print(indent(rec));
                System.out.println("premises.size() == 0");
            } else if (root instanceof OWLEquivalentClassesAxiomImpl) {
                OWLEquivalentClassesAxiomImpl eq = (OWLEquivalentClassesAxiomImpl) root;
                for (OWLClassExpression el : eq.getClassExpressionsAsList()) {
                    Set<OWLEntity> signature = el.getSignature();
                    if (allSymbolsInDelta(delta, signature)) {
                        union.add(el);
                    }
                }
            }
        }
        for (Inference<OWLAxiom> inf : inferences) {
//            boolean goodInference = true;
//            for (OWLAxiom premise : inf.getPremises()) {
//                if (parents.contains(premise)) { // all axioms processed in parents
//                    goodInference = false;
//                    break;
//                }
//            }
//            if (!goodInference) continue;
            if (inf.getPremises().size() == 0) {
                if (root instanceof OWLSubClassOfAxiomImpl) {
                    OWLClassExpression superClass = ((OWLSubClassOfAxiomImpl) root).getSuperClass();
                    Set<OWLEntity> signature = superClass.getSignature();
                    if (allSymbolsInDelta(delta, signature)) {
                        union.add(superClass);
                    } else {
                        union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    }
                    System.out.print(indent(rec));
                    System.out.println("root == " + root.toString().replaceAll(url, ""));
                    for (Inference i : inferences) {
                        System.out.print(indent(rec));
                        System.out.println("  inf=" + i.toString().replaceAll(url, ""));
                    }
                    OWLClassExpression res = merge(union);
                    System.out.print(indent(rec));
                    System.out.println("return " + res.toString().replaceAll(url, ""));
                    System.out.print(indent(rec));
                    System.out.println();
                    parents.remove(root);
                    cache.put(root, res);
                    return res;
                }
            }
            System.out.print(indent(rec));
            System.out.println("reset punion");
            Set<OWLClassExpression> pUnion = new HashSet<>();
            for (OWLAxiom premise : inf.getPremises()) {
//                if (parents.contains(premise)) continue;
                OWLClassExpression res;
                res = handle(premise, proof, delta, parents,cyclic, cache, inf, ont, rec + 1);
                System.out.print(indent(rec));
                System.out.println("adding to punion: " + res);
                pUnion.add(res);
            }
            System.out.print(indent(rec));
            System.out.println("SWITCH: " + inf.getName());
            switch (inf.getName()) {
                case "Equivalent Classes Decomposition": {
                    assert inf.getPremises().size()==1;
                    OWLNaryClassAxiom eq = (OWLNaryClassAxiom) inf.getPremises().get(0);
                    for (OWLClassExpression el : eq.getClassExpressionsAsList()) {
                        Set<OWLEntity> signature = el.getSignature();
                        if (allSymbolsInDelta(delta, signature)) {
                            union.add(el);
                        }
                    }
                }
                break;
                case "Class Hierarchy":
                    if (pUnion.size() == 1) {
                        union.add(pUnion.iterator().next());
                    } else {
                        union.add(new OWLObjectUnionOfImpl(pUnion));
                    }
                    break;
                case "Asserted Conclusion":
                case "Intersection Decomposition":
                    if (root instanceof OWLSubClassOfAxiom) {
                        OWLClassExpression superClass = ((OWLSubClassOfAxiom) root).getSuperClass();
                        Set<OWLEntity> signature = superClass.getSignature();
                        if (allSymbolsInDelta(delta, signature)) {
                            union.add(superClass);
                        } else {
                            union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                        }
                    } else {
                        union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    }
                    break;
                case "Existential Filler Expansion":
                    Set<OWLObjectProperty> rSet = ((OWLSubClassOfAxiomImpl) root).getSubClass().getObjectPropertiesInSignature();
                    Set<OWLEntity> s1 = new HashSet<>();
                    s1.addAll(rSet);
                    if (allSymbolsInDelta(delta, s1)) {
                        if (pUnion.size() > 0) {
                            OWLClassExpression mark = pUnion.iterator().next();
                            if (!isEmpty(mark)) {
                                OWLObjectSomeValuesFromImpl owlObjectSomeValuesFrom = new OWLObjectSomeValuesFromImpl(rSet.iterator().next(), mark);
                                union.add(owlObjectSomeValuesFrom);
                            }
                        }
                    }
                    break;
                case "Intersection Composition":
                    boolean haveEmpty = false;
                    for (OWLClassExpression e : pUnion) {
                        if (isEmpty(e)) {
                            haveEmpty = true;
                            break;
                        }
                    }
                    if (haveEmpty || pUnion.size() == 0) {
                        union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    } else {
                        if (pUnion.size() == 1 && pUnion.iterator().next() instanceof OWLObjectIntersectionOf) {
                            union.add(pUnion.iterator().next());
                        } else {
                            union.add(new OWLObjectIntersectionOfImpl(pUnion));
                        }
                    }
                    break;

                default:
                    union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    break;
            }
        }
        OWLClassExpression ret = merge(union);
        System.out.print(indent(rec));
        System.out.println("***************************************");
        String str = root.toString().replaceAll(url, "");
        System.out.print(indent(rec));
        System.out.println("root == " + str);
        for (Inference i : inferences) {
            System.out.print(indent(rec));
            System.out.println("  inf=" + i.toString().replaceAll(url, ""));
        }
        System.out.print(indent(rec));
        System.out.println("return " + ret.toString().replaceAll(url, ""));
        parents.remove(root);
        cache.put(root, ret);
        return ret;
    }
}