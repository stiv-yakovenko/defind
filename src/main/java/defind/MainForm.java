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
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
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

    static OWLNamedObject[] transToClasses(Transferable transferable) {
        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (DataFlavor df : transferDataFlavors) {
            Object transferData;
            try {
                transferData = transferable.getTransferData(df);
                if (transferData instanceof List) {
                    List transferDataList = (List) transferData;
                    OWLNamedObject[] classes = new OWLNamedObject[transferDataList.size()];
                    for (int classPos = 0; classPos < transferDataList.size(); ++classPos) {
                        classes[classPos] = (OWLNamedObject) transferDataList.get(classPos);
                    }
                    return classes;
                }
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static void removeElem(int[] idx, Set<OWLNamedObject> delta, Set<OWLNamedObject> autoAddedDelta, JList deltaList) {
        for (int i : idx) {
            Object dlt = deltaList.getModel().getElementAt(i);
            for (OWLNamedObject cls : delta) {
                if (cls.getIRI().getShortForm() != dlt) continue;
                delta.remove(cls);
                autoAddedDelta.remove(cls);
                break;
            }
        }
        updateList(deltaList, delta);
    }

    static public JPanel addComponentsToPane(AbstractOWLViewComponent aoc) {
        JPanel mainPanel = new JPanel();
        JPanel resField = new JPanel();
        OWLOntology ont = aoc.getOWLModelManager().getActiveOntology();
        Set<OWLNamedObject> allClasses = new HashSet<>();
        allClasses.addAll(ont.getClassesInSignature());
        allClasses.addAll(ont.getObjectPropertiesInSignature());
        Set<OWLNamedObject> delta = new LinkedHashSet<>();
        Set<OWLNamedObject> autoAddedProperties = new HashSet<>();
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
                    removeElem(idxes, delta, autoAddedProperties, deltaList);
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
                    removeElem(idxes, delta, autoAddedProperties, deltaList);
                }
            }
        });

        JButton eraseFromTargetButton = new JButton("Remove selected");
        eraseFromTargetButton.addActionListener(e -> {
            int[] idxes = deltaList.getSelectedIndices();
            removeElem(idxes, delta, autoAddedProperties, deltaList);
        });
        eraseFromTargetButton.setEnabled(false);

        deltaList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                JList list = (JList) evt.getSource();
                if (list.getSelectedIndices().length == 0) {
                    eraseFromTargetButton.setEnabled(false);
                } else {
                    eraseFromTargetButton.setEnabled(true);
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
                OWLNamedObject[] objectsToAdd = transToClasses(transferable);
                for (OWLNamedObject obj : objectsToAdd) {
                    delta.add(obj);
                    autoAddedProperties.remove(obj);
                }
                updateList(deltaList, delta);
            }
        });
        JButton calcButton = new JButton("Compute definitions");
        calcButton.setPreferredSize(new Dimension(100, 40));
        JPanel res = new JPanel();
        JCheckBox invert = new JCheckBox("doesn't include symbols");
        JCheckBox addAllObjectProperties = new JCheckBox("add all object properties");

        addAllObjectProperties.addActionListener(e -> {
            aoc.getOWLEditorKit().getModelManager();
            OWLModelManager manager = aoc.getOWLModelManager();
            OWLOntology ontology = manager.getActiveOntology();
            if (addAllObjectProperties.isSelected()) {
                for (OWLObjectProperty oop: ontology.getObjectPropertiesInSignature()) {
                    if (!delta.contains(oop)) {
                        delta.add(oop);
                        autoAddedProperties.add(oop);
                    }
                }
            } else {
                for (OWLNamedObject oop: autoAddedProperties) {
                    delta.remove(oop);
                }
                autoAddedProperties.clear();
            }
            updateList(deltaList, delta);
        });

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
                updateList(resField, rs, aoc, cls);
                if (rs.size() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "No definitions found in the target signature");
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        List<String> strs = Arrays.asList(new String[]{});
        updateList(res, strs);

        mainPanel.setLayout(new MigLayout("", "0[grow]0[]0", "0[]0[][grow]0"));
        mainPanel.add(new JLabel("Class expression"), "span 2,wrap");
        mainPanel.add(owlDescriptionEditor, "growx");
        mainPanel.add(calcButton, "wrap");

        JPanel targetPanelWithOptions = new JPanel();
        targetPanelWithOptions.setLayout(new MigLayout("", "0[grow]0", "0[][]0[]0[]0[grow]0"));
        targetPanelWithOptions.add(new JLabel("Target signature"), "wrap");
        targetPanelWithOptions.add(invert, "wrap");
        targetPanelWithOptions.add(addAllObjectProperties,"wrap");
        targetPanelWithOptions.add(eraseFromTargetButton, "growx, wrap");
        targetPanelWithOptions.add(new JScrollPane(deltaList), "growy, growx");

        resField.setLayout(new BoxLayout(resField, BoxLayout.Y_AXIS));
        resField.setBackground(Color.WHITE);
        JScrollPane scrolledResPanel = new JScrollPane(resField);
        scrolledResPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel resPanel = new JPanel();
        resPanel.setLayout(new MigLayout("", "0[grow]0", "0[]0[grow]0"));
        resPanel.add(new JLabel("Definitions found"), "gap rel, wrap");
        resPanel.add(scrolledResPanel, "growy, growx");

        JSplitPane targetAndDefinitionsPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        targetAndDefinitionsPanel.add(targetPanelWithOptions);
        targetAndDefinitionsPanel.add(resPanel);

        mainPanel.add(targetAndDefinitionsPanel, "growy, growx, span 2");
        return mainPanel;
    }

    private static void updateList(JPanel panel, List<String> results) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String res : results) {
            panel.add(new JLabel(res));
        }
    }
}
