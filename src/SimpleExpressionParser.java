import javafx.scene.Node;

/**
 * Starter code to implement an ExpressionParser. Your parser methods should use the following grammar:
 * E := A | X
 * A := A+M | M
 * M := M*M | X
 * X := (E) | L
 * L := [0-9]+ | [a-z]
 */
public class SimpleExpressionParser implements ExpressionParser {
	/*
	 * Attempts to create an expression tree -- flattened as much as possible -- from the specified String.
         * Throws a ExpressionParseException if the specified string cannot be parsed.
	 * @param str the string to parse into an expression tree
	 * @param withJavaFXControls you can just ignore this variable for R1
	 * @return the Expression object representing the parsed expression tree
	 */
	public Expression parse (String str, boolean withJavaFXControls) throws ExpressionParseException {
		// Remove spaces -- this simplifies the parsing logic
		str = str.replaceAll(" ", "");
		Expression expression = parseExpression(str);
		if (expression == null) {
			// If we couldn't parse the string, then raise an error
			throw new ExpressionParseException("Cannot parse expression: " + str);
		}

		// Flatten the expression before returning
		expression.flatten();
		return expression;
	}
	/**
	 * Parses the given string and returns an Expression representing it.  Returns null if expression is invalid
	 * @param str the string to be parsed
	 * @return    the Expression parsed from the given string
	 */
	protected Expression parseExpression (String str) {
	    if (str.length() == 0){
	        return null;
	    }
		int searchFrom = 0;
		int index = str.indexOf("+", 0);
		while ( index > 0){
		    Expression sub1 = parseExpression(str.substring(0,index));
		    Expression sub2 = parseExpression(str.substring(index+1));
		    if (!(sub1 == null || sub2 == null)){
		        CompoundExpression expression = new AdditiveExpression();
		        expression.addSubexpression(sub1);
		        expression.addSubexpression(sub2);
		        return expression;
		    }
		    searchFrom = index;
		    index = str.indexOf("+", searchFrom+1);
		}
		searchFrom = 0;
	    index = str.indexOf("*", 0);
	    while ( index > 0){
            Expression sub1 = parseExpression(str.substring(0,index));
            Expression sub2 = parseExpression(str.substring(index+1));
            if (!(sub1 == null || sub2 == null)){
                CompoundExpression expression = new MultiplicativeExpression();
                expression.addSubexpression(sub1);
                expression.addSubexpression(sub2);
                return expression;
            }
            searchFrom = index;
            index = str.indexOf("*", searchFrom+1);
        }
	    if (str.charAt(0) == '(' && str.length() > 1 && str.charAt(str.length()-1) == ')'){
	        Expression sub  = parseExpression(str.substring(1,str.length()-1));
	        if (sub != null){
	            CompoundExpression expression = new ParentheticalExpression();
                expression.addSubexpression(sub);
                return expression;
	        }
	    }
	    if (str.length() == 1){
	        char c = str.charAt(0);
	        if (c >= 'a' && c <= 'z'){
	            return new LiteralExpression(str);
	        }
	    }
	    boolean num = false;
	    try {
	        int x = Integer.parseInt(str);
	        num = true;
	    } catch(Exception e){
	        num = false;
	    }finally{
	        return num?(new LiteralExpression(str)):null;
	    }
	}
}
