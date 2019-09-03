import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;

public class MultiplicativeExpression extends SimpleCompoundExpression{
    /**
     * returns an expression with identical data values and operator to the this that does not point to this in any way
     *@return a deep copy of this expression
     */
    public Expression deepCopy() {
        CompoundExpression copy = new MultiplicativeExpression();
        for (Expression e: _children){
            copy.addSubexpression(e.deepCopy());
        }
        return copy;
    }
    /**
     * returns a formated string showing this expression.
     * @param indentLevel the amount of tabs placed before each line of the string
     * @return            a string representing this expression
     */
    public String convertToString(int indentLevel) {
        return super.convertToString(indentLevel,"*");
    }
    public Node getNode(){
        return super.getNode("*");
    }
}
