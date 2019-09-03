import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LiteralExpression implements Expression{
    CompoundExpression _parent;
    String _data;
    public LiteralExpression(String data){
        _data = data;
    }
    /**
     * returns the parent CompoundExpression
     * @return the parent CompoundExpression
     */
    public String getData(){
        return _data;
    }
    public CompoundExpression getParent() {
        return _parent;
    }
    /**
     * sets the parent CompoundExpression
     * @param parent the CompoundExpression to make the parent of this expression
     */
    public void setParent(CompoundExpression parent) {
        _parent = parent;
        
    }
    /**
     * returns a copy of this expression untied to this expression
     * @return a copy of this expression
     */
    public Expression deepCopy() {
        LiteralExpression copy = new LiteralExpression(_data);
        return copy;
    }
    /**
     * does nothing
     */
    public void flatten() {}
    /**
     * returns a formated string showing this expression.
     * @param indentLevel the amount of tabs placed before each line of the string
     * @return            a string representing this expression
     */
    public String convertToString(int indentLevel) {
        String out = _data;
        for (int i = 0; i < indentLevel; i++){
            out = "\t" + out;
        }
        return out;
    }
    public Node getNode(){
        HBox h = new HBox(2);
        h.getChildren().add(new Label(_data));
        return h;
    }
    public boolean equals(LiteralExpression x){
        return _data == x.getData();
    }
    public void replaceDeep(Expression n1, Expression n2){
        
    }
}
