import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public abstract class SimpleCompoundExpression implements CompoundExpression{
    protected CompoundExpression _parent;
    protected List<Expression> _children;
    /**
     * constructor for SimpleCompoundExpression that instantiates the _children list
     */
    public SimpleCompoundExpression(){
        _children = new ArrayList<Expression>();
    }
    public void replaceDeep(Expression n1, Expression n2){
        for (int i = 0; i < _children.size();i++){
            if (_children.get(i).equals(n1)){
                _children.set(i, n2);
                return;
            }
        }
        for (Expression x: _children){
            x.replaceDeep(n1,n2);
        }
    }
    public List<Expression> getChildren(){
        return _children;
    }
    /**
     * adds x to the list of children and sets x's parent to this
     * @param x the expression which will be made a child of this and have its parent set to this
     */
    public void addSubexpression(Expression x){
        x.setParent(this);
        _children.add(x);
    }
    /**
     * returns the parent CompoundExpression
     */
    public CompoundExpression getParent() {
        return _parent;
    }
    /**
     * sets the parent CompoundExpression
     */
    public void setParent(CompoundExpression parent) {
        _parent = parent;

    }
    public boolean equals(SimpleCompoundExpression x){
        List<Expression> childs = x.getChildren();
        if (childs.size() != _children.size()){
            return false;
        }
        for (int i = 0; i < _children.size();i++){
            if (!_children.get(i).equals(childs.get(i))){
                return false;
            }
        }
        return true;
    }
    /**
     * modifies this expression so that no multiplicative or additive expressions have children of the same type by moving the children of any such expressions up to be children of the highest such expression
     */
    public void flatten() {
        boolean broke = true;
        while (broke){
            for (Expression x: _children){
                x.flatten();
                if (x.getClass() == getClass()){
                    for (Expression y: ((SimpleCompoundExpression)x)._children){
                        addSubexpression(y);
                    }
                    _children.remove(x);
                    broke = true;
                    break;
                }
                broke = false;
            }
        }
    }
    protected String convertToString(int indentLevel, String operator) {
        String out = operator;
        for (int i = 0; i < indentLevel; i++){
            out = "\t" + out;
        }
        for (Expression e: _children){
            out += "\n" + e.convertToString(indentLevel+1);
        }
        if (indentLevel == 0)
            out += "\n";
        return out;
    }
    public Node getNode(String operator){
        HBox temp = new HBox(0);
        ArrayList<Node> childs = new ArrayList<Node>();
        for (int i = 0; i < _children.size()-1;i++){
            childs.add(_children.get(i).getNode());
            childs.add(new Label(operator));
        }
        childs.add(_children.get(_children.size()-1).getNode());
        temp.getChildren().addAll(childs);
        return temp;
    }
}
