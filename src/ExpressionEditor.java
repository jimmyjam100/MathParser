import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class ExpressionEditor extends Application {
	public static void main (String[] args) {
		launch(args);
	}

	/**
	 * Mouse event handler for the entire pane that constitutes the ExpressionEditor
	 */
	private static class MouseEventHandler implements EventHandler<MouseEvent> {
		private Node nFocus;
		private Expression eFocus;
		private Node nRoot;
		private Expression eRoot;
		private Map<Node,Expression> nodeToExpression;
		private boolean hasBeenDragged;
		private double startX;
		private double startY;
		private Object[][] treeArray;
		private double leftX;
		private double currentX;
		private double rightX;
		Node notGhostCopy = null;
		MouseEventHandler (Pane pane_, CompoundExpression rootExpression_) {
			nFocus = pane_.getChildren().get(0);
			nRoot = nFocus;
			eRoot = rootExpression_;
			nodeToExpression = new HashMap();
			createMap(nRoot,eRoot);
			hasBeenDragged = false;
			startX = 0;
			startY = 0;
		}
		/**
		 *Fills the nodeToExpression HashMap with nodes to there corresponding expression
		 *(node input and expression input must correspond)
		 */
		private void createMap(Node node, Expression ex){
			nodeToExpression.put(node, ex);
			if (ex instanceof SimpleCompoundExpression){
				int nodeIndex = 0;
				for(int i = 0; i<((SimpleCompoundExpression)ex).getChildren().size(); i++){
					while(!(((Parent)node).getChildrenUnmodifiable().get(nodeIndex) instanceof HBox)){
						nodeIndex++;
					}
					createMap(((Parent)node).getChildrenUnmodifiable().get(nodeIndex), ((SimpleCompoundExpression)ex).getChildren().get(i));
					nodeIndex++;

				}
			}
		}
		/**
		 *Set each label node within a node to a set color
		 */
		public void turnColor(Node n,Color color){
			if (n instanceof Label){
				((Label) n).setTextFill(color);
			}
			else{
				for (Node x: ((Parent)n).getChildrenUnmodifiable())
					turnColor(x,color);
			}
		}
		/**
		 * returns the point equal to a nodes total x and y value added to the accumulator
		 */
		private Point2D getPoint(Node node, Point2D acc){
			if (node.getParent() instanceof HBox){
				return getPoint(node.getParent(), acc.add(new Point2D(node.getLayoutX(), node.getLayoutY())));
			}

			return acc.add(new Point2D(node.getLayoutX(), node.getLayoutY()));
		}
		/**
		 * returns the width of a node
		 */
		private double widthOfNode(Node n) {
			if (n instanceof Label) {
				return ((Label)n).getWidth();
			}
			double sum = 0.0;
			for (Node x : ((Parent)n).getChildrenUnmodifiable()) {
				sum += widthOfNode(x);
			}
			return sum;
		}
		/**
		 *Returns true if the two nodes have identical leaf nodes
		 */
		private boolean nodeEquals(Node n1, Node n2){
			if ((n1 instanceof Label) ^ (n2 instanceof Label)){
				return false;
			}
			if ((n1 instanceof Label)){
				return ((Label)n1).getText().equals(((Labeled) n2).getText());
			}
			else{
				if (((Parent)n1).getChildrenUnmodifiable().size() != ((Parent)n2).getChildrenUnmodifiable().size()){
					return false;
				}
				for (int i = 0; i < ((Parent) n1).getChildrenUnmodifiable().size();i++){
					if (!nodeEquals(((Parent)n1).getChildrenUnmodifiable().get(i),((Parent)n2).getChildrenUnmodifiable().get(i))){
						return false;
					}
				}
			}
			return true;
		}
		/**
		 * Returns the x value of the node if it were swapped with the node to its left.
		 * If there is no node to the left returns NaN
		 */
		private double getXOfLeft(Node node) {
			Node parent = node.getParent();
			for (int i=1; i<((Parent)parent).getChildrenUnmodifiable().size(); i++) {
				if (nodeEquals(node, ((Parent)parent).getChildrenUnmodifiable().get(i))) {
					for (int k = i-1; k>=0; k--) {
						if (((Parent)parent).getChildrenUnmodifiable().get(k) instanceof HBox){
							return getPoint(((Parent)parent).getChildrenUnmodifiable().get(i-2), new Point2D(0,0)).getX();
						}
					}
				}
			}
			return Double.NaN;
		}
		/**
		 * Returns the x value of the node if it were swapped with the node to its right.
		 * If there is no node to the right returns NaN.
		 */
		private double getXOfRight(Node node) {
			Node parent = node.getParent();
			for (int i=0; i<((Parent)parent).getChildrenUnmodifiable().size()-1; i++) {
				if (nodeEquals(node, ((Parent)parent).getChildrenUnmodifiable().get(i))) {
					for(int k = i+1; k<((Parent)parent).getChildrenUnmodifiable().size(); k++) {
						if (((Parent)parent).getChildrenUnmodifiable().get(k) instanceof HBox){
							return getPoint(((Parent)parent).getChildrenUnmodifiable().get(k), new Point2D(0,0)).getX()
									+ widthOfNode(((Parent)parent).getChildrenUnmodifiable().get(k))
									- widthOfNode(node);
						}
					}
				}
			}
			return Double.NaN;
		}
		/**
		 * Moves the currently focused node to the left if isLeft is true.
		 * Moves the currently focused node to the right if isLeft is false.
		 */
		private void moveTree (boolean isLeft) {
			int eIndex = 0;
			for (int i = 0; i<((SimpleCompoundExpression)eFocus.getParent()).getChildren().size(); i++) {
				if (eFocus.equals(((SimpleCompoundExpression)eFocus.getParent()).getChildren().get(i))) {
					eIndex = i;
				}
			}

			ArrayList<Expression> movedList = new ArrayList<Expression>();
			for (int i = 0; i<((SimpleCompoundExpression)eFocus.getParent()).getChildren().size(); i++) {
				if (i == eIndex-1 && isLeft) {
					movedList.add(((SimpleCompoundExpression)eFocus.getParent()).getChildren().get(eIndex).deepCopy());
				}
				if (i != eIndex) {
					movedList.add(((SimpleCompoundExpression)eFocus.getParent()).getChildren().get(i).deepCopy());
				}
				if (i == eIndex+1 && !isLeft) {
					movedList.add(((SimpleCompoundExpression)eFocus.getParent()).getChildren().get(eIndex).deepCopy());
				}
			}
			SimpleCompoundExpression temp;
			if (eFocus.getParent().equals(eRoot)) {
				if (eFocus.getParent() instanceof AdditiveExpression) {
					temp = new AdditiveExpression();
				}
				else if (eFocus.getParent() instanceof MultiplicativeExpression) {
					temp = new MultiplicativeExpression();
				}
				else {
					temp = new ParentheticalExpression();
				}
				for(Expression x : movedList) {
					temp.addSubexpression(x);
				}
				eRoot = temp;
			}
			else {
				if (eFocus.getParent() instanceof AdditiveExpression) {
					temp = new AdditiveExpression();
				}
				else if (eFocus.getParent() instanceof MultiplicativeExpression) {
					temp = new MultiplicativeExpression();
				}
				else {
					temp = new ParentheticalExpression();
				}
				for(Expression x : movedList) {
					temp.addSubexpression(x);
				}
				temp.setParent(eFocus.getParent().getParent());
				eRoot.replaceDeep(eFocus.getParent(),temp);
			}
			Pane daPane = (Pane)nRoot.getParent();
			daPane.getChildren().remove(nRoot);
			nRoot = eRoot.getNode();
			daPane.getChildren().add(nRoot);
			nodeToExpression = new HashMap();
			createMap(nRoot,eRoot);
			if (isLeft) {
					eFocus = temp.getChildren().get(eIndex-1);
			}
			else {
					eFocus = temp.getChildren().get(eIndex+1);
			}
			for (Node n: nodeToExpression.keySet()) {
				if (nodeToExpression.get(n).equals(eFocus))
					nFocus  = n;
			}

			((Pane)nFocus).setBorder(Expression.RED_BORDER);
			turnColor(nFocus, Expression.GHOST_COLOR);
			leftX = getXOfLeft(nFocus);
			currentX = getPoint(nFocus, new Point2D(0,0)).getX();
			rightX = getXOfRight(nFocus);
			
			System.out.println(eRoot.convertToString(0));
		}
		public void handle (MouseEvent event) {
			double x = event.getSceneX();
			double y = event.getSceneY();
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				if (!(nFocus == nRoot)){
					if (!hasBeenDragged && nFocus.contains(nFocus.sceneToLocal(x,y))){
						hasBeenDragged = true;
						notGhostCopy = nodeToExpression.get(nFocus).deepCopy().getNode();
						Point2D startPos = getPoint(nFocus, new Point2D(0,0));
						notGhostCopy.relocate(startPos.getX(),startPos.getY());
						turnColor(nFocus, Expression.GHOST_COLOR);
						startX = x;
						startY = y;
						((Pane)(nRoot.getParent())).getChildren().add(notGhostCopy);
						leftX = getXOfLeft(nFocus);
						currentX = getPoint(nFocus, new Point2D(0,0)).getX();
						rightX = getXOfRight(nFocus);
					}
					if (hasBeenDragged){
						notGhostCopy.setTranslateX(x - startX);
						notGhostCopy.setTranslateY(y - startY);
						if (!Double.isNaN(leftX)) {
							if(Math.abs((notGhostCopy.getLayoutX() + notGhostCopy.getTranslateX()) - leftX) < Math.abs((notGhostCopy.getLayoutX() + notGhostCopy.getTranslateX()) - currentX)) {
								moveTree(true);
								
							}
						}
						if (!Double.isNaN(rightX)) {
							if(Math.abs((notGhostCopy.getLayoutX() + notGhostCopy.getTranslateX()) - rightX) < Math.abs((notGhostCopy.getLayoutX() + notGhostCopy.getTranslateX()) - currentX)) {
								moveTree(false);
							}
						}
						leftX = getXOfLeft(nFocus);
						currentX = getPoint(nFocus, new Point2D(0,0)).getX();
						rightX = getXOfRight(nFocus);
					}
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				if (hasBeenDragged){
					((Pane)(nRoot.getParent())).getChildren().remove(notGhostCopy);
					turnColor(nFocus,Color.BLACK);
					hasBeenDragged = false;
				}else{
					((Pane)nFocus).setBorder(Expression.NO_BORDER);
					boolean changed = false;
					for (Node n:((Parent) nFocus).getChildrenUnmodifiable()){
						if (n.contains(n.sceneToLocal(x, y)) && n instanceof HBox){
							nFocus = n;
							eFocus = nodeToExpression.get(nFocus);
							changed = true;
						}
					}
					if (!changed){
						nFocus = nRoot;
					}
					if (nFocus != nRoot){
						((Pane)nFocus).setBorder(Expression.RED_BORDER);
					}
				}
			}
		}
	}

	/**
	 * Size of the GUI
	 */
	private static final int WINDOW_WIDTH = 500, WINDOW_HEIGHT = 250;

	/**
	 * Initial expression shown in the textbox
	 */
	private static final String EXAMPLE_EXPRESSION = "2*x+3*y+4*z+(7+6*z)";

	/**
	 * Parser used for parsing expressions.
	 */
	private final ExpressionParser expressionParser = new SimpleExpressionParser();

	@Override
	public void start (Stage primaryStage) {
		primaryStage.setTitle("Expression Editor");

		// Add the textbox and Parser button
		final Pane queryPane = new HBox();
		final TextField textField = new TextField(EXAMPLE_EXPRESSION);
		final Button button = new Button("Parse");
		queryPane.getChildren().add(textField);

		final Pane expressionPane = new Pane();

		// Add the callback to handle when the Parse button is pressed	
		button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle (MouseEvent e) {
				// Try to parse the expression
				try {
					// Success! Add the expression's Node to the expressionPane
					final Expression expression = expressionParser.parse(textField.getText(), true);
					//System.out.println(expression.convertToString(0));
					expressionPane.getChildren().clear();
					expressionPane.getChildren().add(expression.getNode());//.getNode();
					expression.getNode().setLayoutX(WINDOW_WIDTH/4);
					expression.getNode().setLayoutY(WINDOW_HEIGHT/2);

					// If the parsed expression is a CompoundExpression, then register some callbacks
					if (expression instanceof CompoundExpression) {
						((Pane) expression.getNode()).setBorder(Expression.NO_BORDER);
						final MouseEventHandler eventHandler = new MouseEventHandler(expressionPane, (CompoundExpression) expression);
						expressionPane.setOnMousePressed(eventHandler);
						expressionPane.setOnMouseDragged(eventHandler);
						expressionPane.setOnMouseReleased(eventHandler);
					}
				} catch (ExpressionParseException epe) {
					// If we can't parse the expression, then mark it in red
					textField.setStyle("-fx-text-fill: red");
				}
			}
		});
		queryPane.getChildren().add(button);

		// Reset the color to black whenever the user presses a key
		textField.setOnKeyPressed(e -> textField.setStyle("-fx-text-fill: black"));

		final BorderPane root = new BorderPane();
		root.setTop(queryPane);
		root.setCenter(expressionPane);

		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		primaryStage.show();
	}
}
