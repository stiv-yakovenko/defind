package defind;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenderHTML {
    private enum TextStyle {
        AND,
        OR,
        SOME,
        PROPERTY,
        CLASS
    }

    private static String applyStyle(String text, TextStyle style) {
        String prefix = "";
        prefix += "<font ";
        switch (style) {
            case AND:
            case OR:
                prefix += "color='#00b5b4'";
                break;
            case SOME:
                prefix += "color='c300b8'";
                break;
            case PROPERTY:
            case CLASS:
                prefix += "color='black'";
                break;
        }
        prefix += ">";
        prefix += "<b>";

        String suffix = "";
        suffix += "</b>";
        suffix += "</font>";

        return prefix + text + suffix;
    }

    public static String render(OWLObjectPropertyExpression exp, Map<String, Object> objs) {
        objs.put(exp.toString(),exp);
        return applyStyle("<a href='_href_' style='text-decoration: none'>_name_</a>"
                              .replaceAll("_href_", exp.toString())
                              .replaceAll("_name_", ((OWLObjectPropertyImpl) exp).getIRI().getRemainder().get()),
                          TextStyle.PROPERTY);

    }

    public static String render(OWLClassExpression exp, Map<String, Object> objs) {
        objs.put(exp.toString(),exp);
        StringBuffer res = new StringBuffer();
        res.append("<font face=\"Helvetica Neue\" size=3>");
        if (exp instanceof OWLClassImpl) {
            OWLClassImpl o = ((OWLClassImpl) exp);
            String clsName = o.getIRI().getRemainder().get();
            res.append(applyStyle("<a href='_href_' style='text-decoration: none'>_name_</a>"
                                      .replaceAll("_href_", o.toString())
                                      .replaceAll("_name_", clsName),
                                  TextStyle.CLASS));
        } else if (exp instanceof OWLObjectIntersectionOf) {
            OWLObjectIntersectionOf oio = ((OWLObjectIntersectionOf) exp);
            List<OWLClassExpression> operands = new ArrayList<OWLClassExpression>(oio.getOperands());
            res.append("(");
            for (int i = 0; i < operands.size(); i++) {
                OWLClassExpression owlClassExpression = operands.get(i);
                res.append(render(owlClassExpression,objs));
                if (i < operands.size() - 1) res.append(applyStyle(" and ", TextStyle.AND));
            }
            res.append(")");
        } else if (exp instanceof OWLObjectUnionOf) {
            OWLObjectUnionOf ouo = ((OWLObjectUnionOf) exp);
            List<OWLClassExpression> operands = new ArrayList<OWLClassExpression>(ouo.getOperands());
            if (operands.size() > 1) res.append("(");
            for (int i = 0; i < operands.size(); i++) {
                OWLClassExpression owlClassExpression = operands.get(i);
                String html = render(owlClassExpression,objs);
                res.append(html);
                if (i < operands.size() - 1 && html.length() > 0) {
                    res.append(applyStyle(" or ", TextStyle.OR));
                }
            }
            if (operands.size() > 1) res.append(")");
        } else if (exp instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom osvf = ((OWLObjectSomeValuesFrom) exp);
            OWLClassExpression filler = osvf.getFiller();
            OWLObjectPropertyExpression prop = osvf.getProperty();
            res.append("(");
            res.append(render(prop,objs));
            res.append(applyStyle(" some ", TextStyle.SOME));
            String html = render(filler,objs);
            res.append(html);
            res.append(")");
        }
        res.append("</font>");
        return res.toString();
    }
}
