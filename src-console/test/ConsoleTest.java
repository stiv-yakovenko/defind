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
import java.util.Set;
import java.util.regex.Pattern;

public class ConsoleTest {

    static OWLClassExpression loadAndSolve(String fileName, String[] deltaNames,String cName ) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(fileName));
        ElkProverFactory proverFactory = new ElkProverFactory();
        HashSet<OWLNamedObject> delta = new HashSet<>();
        String url = ont.getOntologyID().getDefaultDocumentIRI().get().toString();
        System.out.println("ont="+ont.toString().replaceAll(Pattern.quote(url),""));
        Set<String> nameSet = new HashSet<String>(Arrays.asList(deltaNames));
        for (OWLObjectProperty oop : ont.getObjectPropertiesInSignature()) {
            String nm = oop.getIRI().getRemainder().get();
            if (nameSet.contains(nm)){
                delta.add(oop);
            }
        }
        for (OWLClass cls : ont.getClassesInSignature()) {
            String nm = cls.getIRI().getRemainder().get();
            if (nameSet.contains(nm)){
                delta.add(cls);
            }
        }
        OWLClassImpl a = new OWLClassImpl(IRI.create(url + "#", cName));
        Calc calc = new Calc(){
            @Override
            public Object invoke(OWLModelManager modelManager, OWLEditorKit owlEditorKit, OWLAxiom cIsLessC_) {
                OWLProver prover = proverFactory.createReasoner(ont);
                DynamicProof<? extends Inference<OWLAxiom>> proof = prover.getProof(cIsLessC_);
                System.out.println(proof);
                return proof;
            }
        };
        return (OWLClassExpression) calc.solve(ont,delta,a,null,manager,null);
    }
    static void testEq( OWLClassExpression res, String s) {
        String url = "http://www.semanticweb.org/denis/ontologies/2017/10/untitled-ontology-";
        String s1 = res.toString().replaceAll(url, "");
        if (!s1.equals(s)) throw new RuntimeException("wrong:"+s1 +" expected "  +s);
    }
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLClassExpression res;
        res = loadAndSolve("./test/concept_simplification.owl", new String[]{"A"}, "C");
        testEq(res,"<259#A>");
        res = loadAndSolve("./test/omit_cyclic_inferences.owl", new String[]{"C","D"}, "A");
        testEq(res,"ObjectIntersectionOf(<257#C> <257#D>)");
        res = loadAndSolve("./test/combofDeltaconcepts.owl", new String[]{"D1","D2"}, "A");
        testEq(res,"ObjectUnionOf(<260#D1> <260#D2> ObjectIntersectionOf(<260#D1> <260#D2>))");
        res = loadAndSolve("./test/ExpLongDefinition.owl", new String[]{"r","s"}, "A2");
        testEq(res,"ObjectIntersectionOf(ObjectSomeValuesFrom(<283#r> ObjectIntersectionOf(ObjectSomeValuesFrom(<283#r> owl:Thing) ObjectSomeValuesFrom(<283#s> owl:Thing))) ObjectSomeValuesFrom(<283#s> ObjectIntersectionOf(ObjectSomeValuesFrom(<283#r> owl:Thing) ObjectSomeValuesFrom(<283#s> owl:Thing))))");
        res = loadAndSolve("./test/EquivalentClassesDecompositionTest.owl", new String[]{"B","C"}, "A");
        testEq(res,"ObjectUnionOf(<287#B> <287#C> owl:Nothing)");
        res = loadAndSolve("./test/D1D2EquivalentClassesTest.owl", new String[]{"D1","D2"}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"ObjectUnionOf(<297#D1> <297#D2>)");
        res = loadAndSolve("./test/AEquivtoThing.owl", new String[]{}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"owl:Thing");
        res = loadAndSolve("./test/ASubclassOfThing.owl", new String[]{}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"owl:Thing");
        res = loadAndSolve("./test/ASubclassOfD.owl", new String[]{"D"}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"<293#D>");
        res = loadAndSolve("./test/ASubclassOfBAndC.owl", new String[]{"B"}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"<296#B>");
        res = loadAndSolve("./test/ExpLongDefinitionSimple.owl", new String[]{"r","s"}, "A1");
        System.out.println("res="+res.toString());
        testEq(res,"ObjectIntersectionOf(ObjectSomeValuesFrom(<283#r> owl:Thing) ObjectSomeValuesFrom(<283#s> owl:Thing))");
        res = loadAndSolve("./test/ExpManyConjunctions.owl", new String[]{"B11","B12","B21","B22"}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"ObjectUnionOf(ObjectIntersectionOf(<282#B11> <282#B21>) ObjectIntersectionOf(<282#B11> <282#B22>) ObjectIntersectionOf(<282#B12> <282#B21>) ObjectIntersectionOf(<282#B12> <282#B22>))");
        res = loadAndSolve("./test/ExpLongDefinitionExpManyConjunctions.owl", new String[]{"r1","s1","r2","s2","u1","v1","u2","v2"}, "A");
        System.out.println("res="+res.toString());
        testEq(res,"ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing))))) ObjectSomeValuesFrom(<282#s1> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#s1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing)))) ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing))))) ObjectSomeValuesFrom(<282#r2> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#r2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing)))) ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing))) ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing)))) ObjectIntersectionOf(ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing))))) ObjectSomeValuesFrom(<282#s1> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#s1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing)))) ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing))))) ObjectSomeValuesFrom(<282#u2> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#u2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing)))) ObjectSomeValuesFrom(<282#r1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#r1> owl:Thing)) ObjectSomeValuesFrom(<282#s1> owl:Thing))) ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing)))) ObjectIntersectionOf(ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing))))) ObjectSomeValuesFrom(<282#r2> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#r2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing)))) ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing))))) ObjectSomeValuesFrom(<282#v1> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#v1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing)))) ObjectSomeValuesFrom(<282#s2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#s2> owl:Thing)) ObjectSomeValuesFrom(<282#r2> owl:Thing))) ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing)))) ObjectIntersectionOf(ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing))))) ObjectSomeValuesFrom(<282#v1> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#v1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing)))) ObjectUnionOf(ObjectUnionOf(ObjectIntersectionOf(ObjectUnionOf(ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf()) ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing))))) ObjectSomeValuesFrom(<282#u2> ObjectIntersectionOf())) ObjectSomeValuesFrom(<282#u2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing)))) ObjectSomeValuesFrom(<282#u1> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#v1> owl:Thing)) ObjectSomeValuesFrom(<282#u1> owl:Thing))) ObjectSomeValuesFrom(<282#v2> ObjectIntersectionOf(ObjectUnionOf(ObjectIntersectionOf() ObjectSomeValuesFrom(<282#u2> owl:Thing)) ObjectSomeValuesFrom(<282#v2> owl:Thing)))))");
        System.out.println("SUCCESS");
    }
}
