import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ParentheticalExpression extends SimpleCompoundExpression{
    /**
     * returns an expression with identical data values and operator to the this that does not point to this in any way
     *@return a deep copy of this expression
     */
    public Expression deepCopy() {
        CompoundExpression copy = new ParentheticalExpression();
        for (Expression e: _children){
            copy.addSubexpression(e.deepCopy());
        }
        return copy;
    }
    /**
     * modifies this expression so that no multiplicative or additive expressions have children of the same type by moving the children of any such expressions up to be children of the highest such expression
     */
    public void flatten(){
        _children.get(0).flatten();
    }
    /**
     * returns a formated string showing this expression.
     * @param indentLevel the amount of tabs placed before each line of the string
     * @return            a string representing this expression
     */
    public String convertToString(int indentLevel) {
        return super.convertToString(indentLevel,"()");
    }
    public Node getNode(){
        HBox h = new HBox(1);
        h.getChildren().addAll(new Label("("), _children.get(0).getNode(),new Label(")"));
        return h;
    }
}
