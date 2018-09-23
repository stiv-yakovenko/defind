package defind;

import net.miginfocom.swing.MigLayout;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

public class MainForm extends JFrame {
    public MainForm(String name) {
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

    static void updateList(JPanel panel, Collection<OWLClassExpression> list, AbstractOWLViewComponent aoc, OWLClassExpression cls) {
        panel.removeAll();
        OWLEditorKit owlEditorKit = aoc.getOWLEditorKit();
        Map<String, Object> objNames = new HashMap<>();
        for (OWLClassExpression p : list) {
            String html = RenderHTML.render(p, objNames);
            html += "&nbsp;<a class='q' href='_src_'>&nbsp;?&nbsp;</a>".replaceAll("_src_", "EXPL:" + p.toString());
            JEditorPane jep = new JEditorPane("text/html", html);
            HTMLEditorKit kit = new HTMLEditorKit();
            StyleSheet styleSheet = kit.getStyleSheet();
            styleSheet.addRule(".q {background-color:#AAAAAA;text-decoration: none; border-radius:10px; color:white}");
            jep.addHyperlinkListener(e -> {
                if (e.getEventType() != ACTIVATED) return;
                if (e.getDescription().startsWith("EXPL")) {
                    Calc.launchReasoner(owlEditorKit.getOWLModelManager());
                    String key = e.getDescription().substring(5);
                    OWLClassExpression obj = (OWLClassExpression) objNames.get(key);
                    ExplanationManager explanationManager = owlEditorKit.getOWLModelManager().getExplanationManager();
                    OWLModelManager modelManager = owlEditorKit.getModelManager();
                    OWLDataFactory fucktory = modelManager.getOWLDataFactory();
                    OWLEquivalentClassesAxiom axiom = fucktory.getOWLEquivalentClassesAxiom(cls, obj);
                    JFrame frame = ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace());
                    explanationManager.handleExplain(frame, axiom);
                    return;
                }
                objNames.forEach((exp, obj) -> {
                    if (exp.toString().equals(e.getDescription())) {
                        owlEditorKit.getWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity) obj);
                        owlEditorKit.getWorkspace().displayOWLEntity((OWLEntity) obj);
                    }
                });
            });
            jep.setEditable(false);
            jep.setOpaque(false);
            panel.add(jep);
        }
        panel.add(Box.createVerticalStrut(400));
    }

    static OWLNamedObject transToClass(Transferable transferable) {
        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (DataFlavor df : transferDataFlavors) {
            Object transferData;
            try {
                transferData = transferable.getTransferData(df);
                if (transferData instanceof List) {
                    OWLNamedObject cls = (OWLNamedObject) ((List) transferData).get(0);
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

    static void removeElem(int[] idx, Set<OWLNamedObject> delta, JList deltaList) {
        for (int i : idx) {
            Object dlt = deltaList.getModel().getElementAt(i);
            for (OWLNamedObject cls : delta) {
                if (cls.getIRI().getShortForm() != dlt) continue;
                delta.remove(cls);
                break;
            }
        }
        updateList(deltaList, delta);
    }

    static public JPanel addComponentsToPane(AbstractOWLViewComponent aoc) {
        JPanel mainPanel = new JPanel();
        JPanel resPanel = new JPanel();
        OWLOntology ont = aoc.getOWLModelManager().getActiveOntology();
        Set<OWLNamedObject> allClasses = new HashSet<>();
        allClasses.addAll(ont.getClassesInSignature());
        allClasses.addAll(ont.getObjectPropertiesInSignature());
        Set<OWLNamedObject> delta = new LinkedHashSet<>();
        Map<Integer, OWLNamedObject> idxToObject = new HashMap<>();
        JList deltaList = new JList(new String[]{});
        deltaList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 127) {
                    JList list = (JList) e.getSource();
                    int[] idxes = list.getSelectedIndices();
                    removeElem(idxes, delta, deltaList);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        deltaList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int[] idxes = list.getSelectedIndices();
                    removeElem(idxes, delta, deltaList);
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
        JButton calcButton = new JButton("Compute definitions");
        calcButton.setPreferredSize(new Dimension(100, 40));
        JPanel res = new JPanel();
        JCheckBox invert = new JCheckBox("doesn't include symbols");
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
                Object object = owlDescriptionEditor.createObject();
                OWLClassExpression cls = (OWLClassExpression) object;
                OWLOntologyManager mgr = aoc.getOWLEditorKit().getOWLModelManager().getOWLOntologyManager();
                Set<OWLNamedObject> inverted = new HashSet<>();
                if (invert.isSelected()){
                    for(OWLObjectProperty oop: ontology.getObjectPropertiesInSignature()){
                        if (!delta.contains(oop)) inverted.add(oop);
                    }
                    for(OWLClass oop: ontology.getClassesInSignature()){
                        if (!delta.contains(oop)) inverted.add(oop);
                    }
                }
                OWLClassExpression sol = (OWLClassExpression) calc.solve(ontology, invert.isSelected()?inverted:delta, cls, owlEditorKit, mgr, manager);
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
                updateList(resPanel, rs, aoc, cls);
                if (rs.size() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "No definitions found in the target signature");
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        List<String> strs = Arrays.asList(new String[]{});
        updateList(res, strs);
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.Y_AXIS));
        resPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new MigLayout("", "[][][grow][]", "[][][][][grow]"));
        mainPanel.add(new JLabel("Class expression"), "wrap");
        mainPanel.add(owlDescriptionEditor, "growx,span 3");
        mainPanel.add(calcButton, "wrap");
        JButton addAllObjectProperties = new JButton("Add all Object Properties");
        mainPanel.add(addAllObjectProperties,"span 2,wrap");
        addAllObjectProperties.addActionListener(e -> {
            aoc.getOWLEditorKit().getModelManager();
            OWLModelManager manager = aoc.getOWLModelManager();
            OWLOntology ontology = manager.getActiveOntology();
            delta.addAll(ontology.getObjectPropertiesInSignature());
            updateList(deltaList, delta);
        });
        mainPanel.add(new JLabel("Target signature"), "");
        mainPanel.add(invert, "");
        mainPanel.add(new JLabel("Definitions found"), "span 2,wrap");
        JScrollPane jsp = new JScrollPane(resPanel);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(new JScrollPane(deltaList), "growx,growy,span 2");
        mainPanel.add(jsp, "growy, growx, span 2");
        return mainPanel;
    }

    private static void updateList(JPanel panel, List<String> results) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String res : results) {
            panel.add(new JLabel(res));
        }
    }
}