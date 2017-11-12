package edu.stanford.bmir.protege.examples.defind;

/*-
 * #%L
 * Protege Proof-Based Explanation
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
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


import org.liveontologies.protege.explanation.proof.ProofServiceManager;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.Proof;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import uk.ac.manchester.cs.owl.owlapi.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Calc {
    public static String url = "XXX";

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Load your ontology.
        Set<OWLNamedObject> delta = new HashSet<>();

        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\steve\\Dropbox\\Projects\\git\\protege_workspace\\protege-master\\omit_cyclic_inferences.owl"));
        Calc.url = ont.getOntologyID().getDefaultDocumentIRI().get().toString();//"http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#";
//        delta.add(new OWLObjectPropertyImpl(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#r")));
//        delta.add(new OWLObjectPropertyImpl(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#s")));
//        delta.add(new OWLClassImpl(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#D1")));
//        delta.add(new OWLClassImpl(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#D2")));
        delta.add(new OWLClassImpl(IRI.create(Calc.url + "#", "C")));
        delta.add(new OWLClassImpl(IRI.create(Calc.url + "#", "D")));
        OWLClassImpl D1 = new OWLClassImpl(IRI.create(url + "#", "A"));
        System.out.println("D=" + delta.toString().replaceAll(url, ""));
        solve(manager, ont, delta, D1, null);
        // System.exit(0);

        // Create an instance of ELK
//        ElkProverFactory proverFactory = new ElkProverFactory();
        //OWLProver prover = proverFactory.createReasoner(ont);

/*        prover.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        OWLAxiom entailment = getEntailment();
        Proof inferences = prover.getProof(entailment);*/
        // System.out.println(null);
    }

    static OWLAxiom addClassAsterix(OWLOntologyManager manager, OWLClass c, Set<OWLNamedObject> delta) throws OWLOntologyCreationException {
        OWLOntology ont = manager.createOntology();
        OWLDataFactory fucktory = manager.getOWLDataFactory();
        OWLSubClassOfAxiom axiom = fucktory.getOWLSubClassOfAxiom(c, c);
        manager.addAxiom(ont, axiom);
        performRename(manager, ont, delta);
        OWLAxiom cls = ont.getAxioms().iterator().next();
        OWLClass c_ = (OWLClass) ((OWLSubClassOfAxiomImpl) cls).getSubClass();
        return fucktory.getOWLSubClassOfAxiom(c, c_);
    }

    static void printOntology(OWLOntology ont) {
        int i = 0;
        for (OWLAxiom a : ont.getAxioms()) {
            System.out.println(i + ":" + a.toString().replaceAll(url, ""));
            i++;
        }
    }

    static OWLClassExpression solve(OWLOntologyManager manager, OWLOntology ont, Set<OWLNamedObject> delta, OWLClass c, ProofServiceManager proofServiceManager) throws OWLOntologyCreationException {
        OWLOntology ont1 = cloneWithAsterisk(manager, ont, delta);
        manager.addAxioms(ont, ont1.getAxioms());
        saveOnt(ont);
        printOntology(ont);
        OWLAxiom cIsLessC_ = addClassAsterix(manager, c, delta);
        System.out.println("cIsLessC_=" + cIsLessC_.toString().replaceAll(url, ""));
        ProofService proofService = proofServiceManager.getProofServices().iterator().next();
        Proof inferences = proofService.getProof(cIsLessC_);
        OWLClassExpression res = handle(cIsLessC_, inferences, delta, null);
        OWLClassExpression res1 = DNFConverter.toDNF(res);
        System.out.println("res=" + res.toString().replace(url, ""));
        System.out.println("res1=" + res1.toString().replace(url, ""));
        return res1;
    }

    private static void saveOnt(OWLOntology ont) {
        try {
            FileOutputStream outputStream = new FileOutputStream("ont.xml");
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
            if (delta.contains(oop)) continue;
            IRI iri = oop.getIRI();
            IRI iri1 = IRI.create(iri.getNamespace(), (iri.getRemainder()).get() + "_");
            map.put(iri, iri1);
        }
        for (OWLClass cls : ont1.getClassesInSignature()) {
            if (delta.contains(cls)) continue;
            IRI iri = cls.getIRI();
            IRI iri1 = IRI.create(iri.getNamespace(), iri.getRemainder().get() + "_");
            map.put(iri, iri1);
        }
        for (IRI iri : map.keySet()) {
            List<OWLOntologyChange> owlOntologyChanges = new OWLEntityRenamer(manager, s).changeIRI(iri, map.get(iri));
            //ChangeApplied changeApplied =
            manager.applyChanges(owlOntologyChanges);
        }
    }

    static boolean isEmpty(OWLClassExpression e) {
        if (e instanceof OWLObjectUnionOfImpl) {
            if (((OWLObjectUnionOfImpl) e).getOperands().size() == 0) {
                return true;
            }
        }
        return false;
    }

    static OWLAxiom getFirst(Set<OWLAxiom> premises, Map<OWLAxiom, OWLClassExpression> circles) {
        for (OWLAxiom premise : premises) {
            if (!circles.containsKey(premise)) {
                premises.remove(premise);
                return premise;
            }
        }
        return null;
    }

    static OWLClassExpression handle(OWLAxiom root, Proof proof, Set<OWLNamedObject> delta, Map<OWLAxiom, OWLClassExpression> circles) {
        if (circles == null) {
            circles = new HashMap<>();
            return handle(root, proof, delta, circles);
        }
        circles.put(root, null);
        Collection<? extends Inference<OWLAxiom>> inferences = proof.getInferences(root);
        Set<OWLClassExpression> union = new HashSet();
        if (inferences.size() == 0) {
            if (root instanceof OWLSubClassOfAxiomImpl) {
                OWLClassExpression superClass = ((OWLSubClassOfAxiomImpl) root).getSuperClass();
                Set<OWLEntity> signature = superClass.getSignature();
                if (delta.containsAll(signature)) {
                    union.add(superClass);
                } else {
                    union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                }
                System.out.println("premises.size() == 0");
            } else if (root instanceof OWLEquivalentClassesAxiomImpl) {
                OWLEquivalentClassesAxiomImpl eq = (OWLEquivalentClassesAxiomImpl) root;
                for (OWLClassExpression el : eq.getClassExpressionsAsList()) {
                    Set<OWLEntity> signature = el.getSignature();
                    if (delta.containsAll(signature)) {
                        union.add(el);
                    }
                }
            }
        }
        for (Inference<OWLAxiom> inf : inferences) {
            Set<OWLClassExpression> pUnion = new HashSet<>();
            Set<OWLAxiom> axioms = new HashSet<>();
            axioms.addAll(inf.getPremises());
            while (true) {
                OWLAxiom premise = getFirst(axioms, circles);
                OWLClassExpression res;
                if (premise == null) {
                    for (OWLAxiom ax : axioms) {
                        res = circles.get(ax);
                        if (isEmpty(res) || res == null) continue;
                        pUnion.add(res);
                    }
                    break;
                } else {
                    res = handle(premise, proof, delta, circles);
                }
                if (isEmpty(res)) continue;
                pUnion.add(res);
            }
            switch (inf.getName()) {
                case "Equivalent Classes Decomposition":
                    union.addAll(pUnion);
                    break;
                case "Class Hierarchy":
                    if (pUnion.size() == 1) {
                        union.add(pUnion.iterator().next());
                    } else {
                        union.add(new OWLObjectUnionOfImpl(pUnion));
                    }
                    break;
                case "Intersection Decomposition":
                    if (root instanceof OWLSubClassOfAxiom) {
                        OWLClassExpression superClass = ((OWLSubClassOfAxiom) root).getSuperClass();
                        Set<OWLEntity> signature = superClass.getSignature();
                        if (delta.containsAll(signature)) {
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
                    if (delta.containsAll(rSet)) {
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
                    if (!haveEmpty) {
                        if (pUnion.size() == 1 && pUnion.iterator().next() instanceof OWLObjectIntersectionOf) {
                            union.add(pUnion.iterator().next());
                        } else {
                            union.add(new OWLObjectIntersectionOfImpl(pUnion));
                        }
                    } else {
                        union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    }
                    break;
                default:
                    union.add(new OWLObjectUnionOfImpl(new HashSet<>()));
                    break;
            }
        }
        System.out.println();
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
        System.out.println("return " + ret.toString().replaceAll(url, "") + " for " + root.toString().replaceAll(url, ""));
        circles.put(root, ret);
        return ret;
    }

    private static org.semanticweb.owlapi.model.OWLAxiom getEntailment() {
        // Let's pick some class subsumption we want to explain
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        OWLClass subsumee = factory.getOWLClass(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#A2"));
        OWLClass subsumer = factory.getOWLClass(IRI.create("http://www.semanticweb.org/denis/ontologies/2017/6/untitled-ontology-239#B2"));
        return factory.getOWLSubClassOfAxiom(subsumee, subsumer);
    }

}
/*
OSVF(r,OUO(A1..An)) => OUO(OSVF(r,A1),OSVF(r,A2),OSVF(r,An))
OIO(OUO(A1,A2),OUO(B1,B2)) => OUO ( OIO(A_i,B_j) )
OUO должен быть только снаружи

* */