package defind;

import net.miginfocom.swing.MigLayout;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.liveontologies.protege.explanation.proof.ProofServiceManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.frame.OWLEntityFrame;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class GridLayoutDemo extends JFrame {
    public GridLayoutDemo(String name) {
        super(name);
    }

    static void updateList(JList jlist, Set<OWLNamedObject> list) {
        DefaultListModel model = new DefaultListModel<String>();
        for (OWLNamedObject p : list) {
            model.addElement(p.getIRI().getShortForm());
        }
        jlist.setModel(model);
        jlist.setSelectedIndex(0);
    }

    static void updateList(JPanel panel, Collection<OWLClassExpression> list) {
        panel.removeAll();
        for (int i = 0; i < 3; i++) {
            for (OWLClassExpression p : list) {
                String name = ((OWLNamedObject) p).getIRI().getShortForm();
                JEditorPane jep = new JEditorPane("text/html", name+"QWQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
                //jep.setPreferredSize(new Dimension(200,20));
                jep.setEditable(false);
                jep.setOpaque(false);
                panel.add(jep);
            }
        }
        panel.add( Box.createVerticalStrut(400) );
    }

    static OWLClass transToClass(Transferable transferable) {
        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (DataFlavor df : transferDataFlavors) {
            Object transferData;
            try {
                transferData = transferable.getTransferData(df);
                if (transferData instanceof List) {
                    OWLClass cls = (OWLClass) ((List) transferData).get(0);
                    return cls;
                }
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static void removeElem(int idx, Set<OWLNamedObject> delta, JList deltaList) {
        Object dlt = deltaList.getModel().getElementAt(idx);
        for (OWLNamedObject cls : delta) {
            if (cls.getIRI().getShortForm() != dlt) continue;
            delta.remove(cls);
            updateList(deltaList, delta);
            return;
        }
    }

    static public JPanel addComponentsToPane(AbstractOWLViewComponent aoc) {
        JPanel mainPanel = new JPanel();
        JPanel resPanel = new JPanel();
        OWLOntology ont = aoc.getOWLModelManager().getActiveOntology();
        Set<OWLNamedObject> allClasses = new HashSet<>();
        allClasses.addAll(ont.getClassesInSignature());
        allClasses.addAll(ont.getObjectPropertiesInSignature());
        Set<OWLNamedObject> delta = new HashSet<>();
        Map<Integer, OWLNamedObject> idxToObject = new HashMap<>();
        JList deltaList = new JList(new String[]{});
        deltaList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int idx = list.locationToIndex(evt.getPoint());
                    if (idx < 0) return;
                    removeElem(idx, delta, deltaList);
                }
            }
        });
        java.util.List<String> arr = new ArrayList<>();
        int i = 0;
        for (OWLNamedObject elem : allClasses) {
            arr.add(elem.getIRI().getShortForm());
            idxToObject.put(i, elem);
            i++;
        }
        updateList(deltaList, new HashSet<>());
        final OWLExpressionChecker<OWLClassExpression> checker = aoc.getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
        ExpressionEditor owlDescriptionEditor = new ExpressionEditor<>(aoc.getOWLEditorKit(), checker);
        owlDescriptionEditor.setPreferredSize(new Dimension(100, 40));
        deltaList.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                Transferable transferable = dtde.getTransferable();
                delta.add(transToClass(transferable));
                updateList(deltaList, delta);
            }
        });
        JButton calcButton = new JButton("calculate");
        calcButton.setPreferredSize(new Dimension(100, 40));
        JPanel res = new JPanel();
        calcButton.addActionListener(e -> {
            try {
                if (!owlDescriptionEditor.isWellFormed()) {
                    JOptionPane.showMessageDialog(mainPanel, "C is null, select something");
                    return;
                }
                OWLEditorKit owlEditorKit = aoc.getOWLEditorKit();
                aoc.getOWLEditorKit().getModelManager();
                OWLModelManager manager = aoc.getOWLModelManager();
                OWLOntology ontology = manager.getActiveOntology();
                Calc calc = new Calc();
                OWLClass cls = (OWLClass) owlDescriptionEditor.createObject();
                OWLOntologyManager mgr = aoc.getOWLEditorKit().getOWLModelManager().getOWLOntologyManager();
                OWLClassExpression sol = (OWLClassExpression) calc.solve(ontology, delta, cls, owlEditorKit, mgr, manager);
                Collection<OWLClassExpression> rs = new ArrayList<>();
                if (sol instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf ouo = (OWLObjectUnionOf) sol;
                    Set<OWLClassExpression> operands = ouo.getOperands();
                    operands.forEach(owlClassExpression -> {
                        rs.add(owlClassExpression);
                    });
                } else {
                    rs.add(sol);
                }
                updateList(resPanel, rs);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        List<String> strs = Arrays.asList(new String[]{});
        updateList(res, strs);
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.Y_AXIS));
        mainPanel.setLayout(new MigLayout("", "[][grow][grow][]", "[][][][grow]"));
        mainPanel.add(new JLabel("Class expression"), "wrap");
        mainPanel.add(owlDescriptionEditor, "growx,span 3");
        mainPanel.add(calcButton, "wrap");
        mainPanel.add(new JLabel("Definitions found"), "span 2");
        mainPanel.add(new JLabel("Target signature"), "span 2,wrap");
        JScrollPane jsp = new JScrollPane(resPanel);
        mainPanel.add(jsp, "growy, growx, span 2");
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(new JScrollPane(deltaList), "growx,growy,span 2");
        return mainPanel;
    }

    private static void updateList(JPanel panel, List<String> results) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String res : results) {
            panel.add(new JLabel(res));
        }

    }

    /*private static void createAndShowGUI() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\steve\\Dropbox\\Projects\\git\\protege_workspace\\protege-master\\concept_simplification.owl"));
        GridLayoutDemo frame = new GridLayoutDemo("DeFind");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ProofServiceManager proofServiceManager = CalcConsoleTest.getProofServiceManager(ont);
        JPanel panel = frame.addComponentsToPane(manager, ont, proofServiceManager);
        frame.add(panel);
        frame.setSize(700, 600);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws OWLOntologyCreationException {
        createAndShowGUI();
    }*/


}
/*        cLabel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                Transferable transferable = dtde.getTransferable();
                c[0] = transToClass(transferable);
                cLabel.setText("C = " + c[0].getIRI().getShortForm());
            }
        });*/