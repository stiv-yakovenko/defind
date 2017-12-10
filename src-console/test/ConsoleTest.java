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
import java.util.HashSet;

public class ConsoleTest {

    static OWLClassExpression loadAndSolve(String fileName, String[] deltaNames,String cName ) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(fileName));
        ElkProverFactory proverFactory = new ElkProverFactory();
        HashSet<OWLNamedObject> delta = new HashSet<>();
        String url = ont.getOntologyID().getDefaultDocumentIRI().get().toString();
        for (String name : deltaNames){
            delta.add(new OWLClassImpl(IRI.create(url + "#", name)));
        }
        OWLClassImpl a = new OWLClassImpl(IRI.create(url + "#", cName));
        Calc calc = new Calc(){
            @Override
            public Object invoke(OWLModelManager modelManager, OWLEditorKit owlEditorKit, OWLAxiom cIsLessC_) {
                OWLProver prover = proverFactory.createReasoner(ont);
                DynamicProof<? extends Inference<OWLAxiom>> proof = prover.getProof(cIsLessC_);
                return proof;
            }
        };
        return (OWLClassExpression) calc.solve(ont,delta,a,null,manager,null);
    }
    static void testEq( OWLClassExpression res, String s) {
        String url = "http://www.semanticweb.org/denis/ontologies/2017/10/untitled-ontology-";
        String s1 = res.toString().replaceAll(url, "");
        //System.out.println("s1="+s1);
        if (!s1.equals(s)) throw new RuntimeException("");
    }
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLClassExpression res;
        res = loadAndSolve("./test/concept_simplification.owl", new String[]{"A"}, "C");
        testEq(res,"<259#A>");
        res = loadAndSolve("./test/omit_cyclic_inferences.owl", new String[]{"C","D"}, "A");
        testEq(res,"ObjectIntersectionOf(<257#C> <257#D>)");
        res = loadAndSolve("./test/combofDeltaconcepts.owl", new String[]{"D1","D2"}, "A");
        testEq(res,"ObjectUnionOf(<260#D1> <260#D2> ObjectIntersectionOf(<260#D1> <260#D2>))");
    }
}
