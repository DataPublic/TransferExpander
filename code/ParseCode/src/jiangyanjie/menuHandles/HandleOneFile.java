package jiangyanjie.menuHandles;

import java.util.ArrayList;

import entity.Argument;
import entity.ClassName;
import entity.Field;
import entity.Identifier;
import entity.MethodName;
import entity.Parameter;
import entity.Variable;
import expansion.AllExpansions;
import relation.AssignInfo;
import relation.ClassInfo;
import relation.CommentInfo;
import relation.MethodDeclarationInfo;
import relation.MethodInvocationInfo;
import relation.RelationBase;
import util.Config;
import util.Util;


public class HandleOneFile {
	// relationBase that are selected from visitors in Package visitor
	public ArrayList<RelationBase> relationBases = new ArrayList<>();

	public void parse() {
		for (int i = 0; i < relationBases.size(); i++) {
			RelationBase relationBase = relationBases.get(i);
			if (relationBase instanceof AssignInfo) {
				handleAssign((AssignInfo)relationBase);
			} else if (relationBase instanceof ClassInfo) {
				handleExtend((ClassInfo)relationBase);
			} else if (relationBase instanceof MethodDeclarationInfo) {
				handleMethodDeclaration((MethodDeclarationInfo)relationBase);
			} else if (relationBase instanceof MethodInvocationInfo) {
				handleMethodInvocation((MethodInvocationInfo)relationBase);
			} else if (relationBase instanceof CommentInfo) {
				handleComment((CommentInfo)relationBase);
			}
		}
	}

	private void handleComment(CommentInfo commentInfo) {
		int startLine = commentInfo.startLine;
		int endLine = commentInfo.line;

		boolean currentLine = true;
		// comment in current line
		for (int i = 0; i < relationBases.size(); i++) {
			if (relationBases.get(i).line == startLine && relationBases.get(i) != commentInfo) {
				currentLine = false;
				handleRelationBaseAndCommentInfo(relationBases.get(i), commentInfo);
			}
		}
		if (currentLine == false) {
			return;
		}
		// comment in previous line
		for (int i = 0; i < relationBases.size(); i++) {
			if (relationBases.get(i).line == (endLine + 1)) {
				handleRelationBaseAndCommentInfo(relationBases.get(i), commentInfo);
			}
		}
	}

	private void handleRelationBaseAndCommentInfo(RelationBase relationBase, CommentInfo commentInfo) {
		if (relationBase instanceof AssignInfo) {
			AssignInfo assignInfo = (AssignInfo) relationBase;
			ArrayList<Identifier> left = assignInfo.left;
			ArrayList<Identifier> right = assignInfo.right;
			for (int i = 0; i < left.size(); i++) {
				Util.putHashSet(AllExpansions.idToComments, left.get(i).id, commentInfo.content);
			}
			if (right != null) {
				for (int i = 0; i < right.size(); i++) {
					Util.putHashSet(AllExpansions.idToComments, right.get(i).id, commentInfo.content);
				}
			}
		} else if (relationBase instanceof ClassInfo) {
			ClassInfo extendInfo = (ClassInfo) relationBase;
			ClassName className = extendInfo.className;
			Util.putHashSet(AllExpansions.idToComments, className.id, commentInfo.content);

			ArrayList<ClassName> classNames = extendInfo.expans;
			for (int j = 0; j < classNames.size(); j++) {
				Util.putHashSet(AllExpansions.idToComments, classNames.get(j).id, commentInfo.content);
			}
		} else if (relationBase instanceof MethodDeclarationInfo) {
			MethodDeclarationInfo methodDeclarationInfo = (MethodDeclarationInfo) relationBase;
			Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.methodName.id, commentInfo.content);

			for (int j = 0; j < methodDeclarationInfo.parameters.size(); j++) {
				Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.parameters.get(j).id, commentInfo.content);
			}

			// not constructor
			if (methodDeclarationInfo.methodName.type != null) {
				Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.methodName.type.id, commentInfo.content);
			}

		} else if (relationBase instanceof MethodInvocationInfo) {
			MethodInvocationInfo methodInvocationInfo = (MethodInvocationInfo) relationBase;
			Util.putHashSet(AllExpansions.idToComments, methodInvocationInfo.methodName.type.id, commentInfo.content);

			for (int j = 0; j < methodInvocationInfo.arguments.size(); j++) {
				Argument argument = methodInvocationInfo.arguments.get(j);
				for (int k = 0; k < argument.identifiers.size(); k++) {
					Util.putHashSet(AllExpansions.idToComments, argument.identifiers.get(k).id, commentInfo.content);
				}
			}
		}
	}

	private void handleMethodInvocation(MethodInvocationInfo methodInvocationInfo) {

		Util.putHashSet(AllExpansions.idToFiles, methodInvocationInfo.methodName.id, Config.projectName);
		AllExpansions.idToMethodName.put(methodInvocationInfo.methodName.id, methodInvocationInfo.methodName);
		
		Util.putHashSet(AllExpansions.idToFiles, methodInvocationInfo.methodName.type.id, Config.projectName);
		AllExpansions.idToClassName.put(methodInvocationInfo.methodName.type.id, methodInvocationInfo.methodName.type);	

		ArrayList<Argument> arguments = methodInvocationInfo.arguments;
		for (Argument argument : arguments) {
			ClassName type = argument.type;
			
			Util.putHashSet(AllExpansions.idToFiles, type.id, Config.projectName);
			AllExpansions.idToClassName.put(type.id, type);	

			ArrayList<Identifier> identifiers = argument.identifiers;
			
			for (Identifier identifier : identifiers) {
				Util.putHashSet(AllExpansions.idToFiles, identifier.id, Config.projectName);
				AllExpansions.idToIdentifier.put(identifier.id, identifier);

				if (identifier instanceof Parameter) {
					Parameter parameter = (Parameter) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof Field) {
					Field parameter = (Field) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof Variable) {
					Variable parameter = (Variable) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof MethodName) {
					MethodName parameter = (MethodName) identifier;
					if (parameter.type != null) {
						Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
						AllExpansions.idToClassName.put(parameter.type.id, parameter.type);
					}
				}
			}
		}
		
		AllExpansions.methodInvocationInfos.add(methodInvocationInfo);
	}

	private void handleMethodDeclaration(MethodDeclarationInfo methodDeclarationInfo) {
		AllExpansions.methodDeclarationInfos.add(methodDeclarationInfo);
		Util.putHashSet(AllExpansions.idToFiles, methodDeclarationInfo.methodName.id, Config.projectName);
		AllExpansions.idToMethodName.put(methodDeclarationInfo.methodName.id, methodDeclarationInfo.methodName);

		if (methodDeclarationInfo.methodName.type != null) {
			Util.putHashSet(AllExpansions.idToFiles, methodDeclarationInfo.methodName.type.id, Config.projectName);
			AllExpansions.idToClassName.put(methodDeclarationInfo.methodName.type.id, methodDeclarationInfo.methodName.type);	
		}

		ArrayList<Parameter> parameters = methodDeclarationInfo.parameters;
		for (int i = 0; i < parameters.size(); i++) {
			Util.putHashSet(AllExpansions.idToFiles, parameters.get(i).id, Config.projectName);
			AllExpansions.idToParameter.put(parameters.get(i).id, parameters.get(i));	

			Util.putHashSet(AllExpansions.idToFiles, parameters.get(i).type.id, Config.projectName);
			AllExpansions.idToClassName.put(parameters.get(i).type.id, parameters.get(i).type);
		}
		
		ArrayList<Identifier> identifiers = methodDeclarationInfo.identifiers;
		
		for (Identifier identifier : identifiers) {
			Util.putHashSet(AllExpansions.idToFiles, identifier.id, Config.projectName);
			AllExpansions.idToIdentifier.put(identifier.id, identifier);

			if (identifier instanceof Parameter) {
				Parameter parameter = (Parameter) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Field) {
				Field parameter = (Field) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Variable) {
				Variable parameter = (Variable) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof MethodName) {
				MethodName parameter = (MethodName) identifier;
				if (parameter.type != null) {
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);
				}
			}
		}
		
	}

	private void handleExtend(ClassInfo classInfo) {
		AllExpansions.classInfos.add(classInfo);

		ClassName className = classInfo.className;
		Util.putHashSet(AllExpansions.idToFiles, className.id, Config.projectName);
		AllExpansions.idToClassName.put(className.id, className);

		ArrayList<ClassName> classNames = classInfo.expans;
		for (int i = 0; i < classNames.size(); i++) {
			Util.putHashSet(AllExpansions.idToFiles, classNames.get(i).id, Config.projectName);
			AllExpansions.idToClassName.put(classNames.get(i).id, classNames.get(i));
			Util.put(AllExpansions.childToParent, className.id, classNames.get(i).id);
			Util.put(AllExpansions.parentToChild, classNames.get(i).id, className.id);
		}

		ArrayList<Field> fields = classInfo.fields;
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			Util.putHashSet(AllExpansions.idToFiles, field.id, Config.projectName);
			AllExpansions.idToField.put(field.id, field);

			Util.putHashSet(AllExpansions.idToFiles, field.type.id, Config.projectName);
			AllExpansions.idToClassName.put(field.type.id, field.type);
		}
		
		ArrayList<MethodName> methodNames = classInfo.methodNames;
		for (int i = 0; i < methodNames.size(); i++) {
			MethodName methodName = methodNames.get(i);
			Util.putHashSet(AllExpansions.idToFiles, methodName.id, Config.projectName);
			AllExpansions.idToMethodName.put(methodName.id, methodName);

			if (methodName.type != null) {
				Util.putHashSet(AllExpansions.idToFiles, methodName.type.id, Config.projectName);
				AllExpansions.idToClassName.put(methodName.type.id, methodName.type);	
			}
		}
		ArrayList<Identifier> identifiers = classInfo.identifiers;
		
		for (Identifier identifier : identifiers) {
			Util.putHashSet(AllExpansions.idToFiles, identifier.id, Config.projectName);
			AllExpansions.idToIdentifier.put(identifier.id, identifier);

			if (identifier instanceof Parameter) {
				Parameter parameter = (Parameter) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Field) {
				Field parameter = (Field) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Variable) {
				Variable parameter = (Variable) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof MethodName) {
				MethodName parameter = (MethodName) identifier;
				if (parameter.type != null) {
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);
				}
			}
		}
	}

	private void handleAssign(AssignInfo assignInfo) {
		AllExpansions.assignInfos.add(assignInfo);

		ArrayList<Identifier> left = assignInfo.left;
		ArrayList<Identifier> right = assignInfo.right;
		for (int i = 0; i < left.size(); i++) {
			Util.putHashSet(AllExpansions.idToFiles, left.get(i).id, Config.projectName);
			AllExpansions.idToIdentifier.put(left.get(i).id, left.get(i));

			Identifier identifier = left.get(i);
			if (identifier instanceof Parameter) {
				Parameter parameter = (Parameter) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Field) {
				Field parameter = (Field) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof Variable) {
				Variable parameter = (Variable) identifier;
				Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
				AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

			} else if (identifier instanceof MethodName) {
				MethodName parameter = (MethodName) identifier;
				if (parameter.type != null) {
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);
				}
			}
		}
		if (right != null) {
			for (int i = 0; i < right.size(); i++) {
				Util.putHashSet(AllExpansions.idToFiles, right.get(i).id, Config.projectName);
				AllExpansions.idToIdentifier.put(right.get(i).id, right.get(i));

				Identifier identifier = right.get(i);
				if (identifier instanceof Parameter) {
					Parameter parameter = (Parameter) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof Field) {
					Field parameter = (Field) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof Variable) {
					Variable parameter = (Variable) identifier;
					Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
					AllExpansions.idToClassName.put(parameter.type.id, parameter.type);

				} else if (identifier instanceof MethodName) {
					MethodName parameter = (MethodName) identifier;
					if (parameter.type != null) {
						Util.putHashSet(AllExpansions.idToFiles, parameter.type.id, Config.projectName);
						AllExpansions.idToClassName.put(parameter.type.id, parameter.type);
					}
				}
			}
		}
	}
}
