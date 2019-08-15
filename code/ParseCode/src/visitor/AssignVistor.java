package visitor;

import java.util.ArrayList;

import util.Util;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import entity.ClassName;
import entity.Identifier;
import entity.Variable;
import huyamin.menuHandles.HandleOneFile;
import relation.AssignInfo;

public class AssignVistor extends ASTVisitor {
	// used to get line numbers
    private CompilationUnit compilationUnit;
    // used to collect relation info
    private HandleOneFile globalVariables;
	public AssignVistor(CompilationUnit compilationUnit, HandleOneFile globalVariables) {
		super();
		this.compilationUnit = compilationUnit;
		this.globalVariables = globalVariables;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression left = node.getLeftHandSide();
		Expression right = node.getRightHandSide();
		
		ArrayList<Identifier> leftIdentifiers = Util.parseExpression(left);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(right);
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		AssignInfo assignInfo = new AssignInfo(line, leftIdentifiers, rightIdentifiers);
		globalVariables.relationBases.add(assignInfo);
		return super.visit(node);
	}
	
	// field declarations, local variable declarations, 
	// ForStatement initializers, and LambdaExpression parameters
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		AssignInfo assignInfo = getAssignInfo(node, compilationUnit);
		if (assignInfo != null) {
			globalVariables.relationBases.add(assignInfo);
		}
		return super.visit(node);
	}
	
	public static AssignInfo getAssignInfo(VariableDeclarationFragment node, CompilationUnit compilationUnit) {
		if (node.resolveBinding() == null) {
			System.err.println(node.toString());
			return null;
		}
		String id = node.resolveBinding().getKey();
		String name = node.getName().toString();
		ClassName type = new ClassName(node.resolveBinding().getType().getKey(), 
				node.resolveBinding().getType().getName().toString());
		Variable variable = new Variable(id, name, type);
		
		ArrayList<Identifier> leftIdentifiers = new ArrayList<>();
		leftIdentifiers.add(variable);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(node.getInitializer());
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		AssignInfo assignInfo = new AssignInfo(line, leftIdentifiers, rightIdentifiers);
		return assignInfo;
	}

	// formal parameter lists and catch clauses
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (node.resolveBinding() == null) {
			System.err.println(node.toString());
			return super.visit(node);
		}
		String id = node.resolveBinding().getKey();
		String name = node.getName().toString();
		if (node.getType().resolveBinding() == null) {
			System.err.println(node.toString());
			return super.visit(node);
		}
		ClassName type = new ClassName(node.getType().resolveBinding().getKey(), 
				node.getType().resolveBinding().getName());
		Variable variable = new Variable(id, name, type);
		
		ArrayList<Identifier> leftIdentifiers = new ArrayList<>();
		leftIdentifiers.add(variable);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(node.getInitializer());
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		AssignInfo assignInfo = new AssignInfo(line, leftIdentifiers, rightIdentifiers);
		
		globalVariables.relationBases.add(assignInfo);
		return super.visit(node);
	}
}
