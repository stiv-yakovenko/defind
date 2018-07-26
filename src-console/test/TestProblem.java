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
        ConsoleTest.loadAndSolve("C:\\Users\\steve\\Project\\defind\\test\\pizza_simplified.owl", new String[]{"Pizza","SpicyPizza"}, "C");
    }
}