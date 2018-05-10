package test;

import defind.Calc;
import org.liveontologies.owlapi.proof.OWLProver;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.elk.owlapi.ElkProverFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

class TestProblem {
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\steve\\Dropbox\\Projects\\git\\defind\\ont-1525351722713.xml"));
        System.out.println(ont.toString().replaceAll(Pattern.quote("http://www.semanticweb.org/denis/ontologies/2017/10/untitled-ontology-293"), ""));
        ElkProverFactory proverFactory = new ElkProverFactory();
        OWLProver prover = proverFactory.createReasoner(ont);
        OWLDataFactory fucktory = manager.getOWLDataFactory();
        Set<OWLClass> classes = ont.getClassesInSignature();
        Iterator<OWLClass> iterator = classes.iterator();
        OWLClass c_ = iterator.next();
        OWLClass c = iterator.next();
        OWLSubClassOfAxiom cc_ = fucktory.getOWLSubClassOfAxiom(c, c_);
        System.out.println(cc_);
        DynamicProof<? extends Inference<OWLAxiom>> proof = prover.getProof(cc_);
        System.out.println(proof.getInferences(cc_));

    }
}