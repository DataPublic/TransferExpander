package jiangyanjie.menuHandles;

import java.io.FileNotFoundException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;

import expansion.AllExpansions;
import util.Config;
import visitor.AssignVistor;
import visitor.ClassVisitor;
import visitor.CommentVisitor;
import visitor.MethodDeclarationVisitor;
import visitor.MethodInvocationVisitor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class Parse extends AbstractHandler {
	// all identifiers in the project
	// id of identifier to Expansions
//	public static HashMap<String, Expansions> expansionIdentifiers = new HashMap<>();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String commandID = event.getCommand().getId();
		try {
			handleCommand(commandID);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void handleCommand(String commandID) throws FileNotFoundException {
		System.err.println("start to parse!");

		
		if (commandID.equals("ParseCode.commands.sampleCommand")) {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				
				IProject project = projects[i];
				
				IJavaProject javaProject = JavaCore.create(project);
				try {
					IPackageFragment[] fragments = javaProject.getPackageFragments();
					for (int j = 0; j < fragments.length; j++) {
						
						IPackageFragment fragment = fragments[j];

						ICompilationUnit[] compilationUnits = fragment.getCompilationUnits();
						for (int k = 0; k < compilationUnits.length; k++) {
							ICompilationUnit compilationUnit = compilationUnits[k];
							System.err.println((i+1) + "/" + projects.length + ";" + (j+1) + "/" + fragments.length + ";" + (k+1) + "/" + fragments.length);
							
							IResource iResource = compilationUnit.getUnderlyingResource();
							if (iResource.getType() == IResource.FILE) {
								IFile ifile = (IFile) iResource;
							    String path = ifile.getRawLocation().toString();
							    Config.projectName = path;
							}
							
							//System.err.println(compilationUnit.getElementName());
							ASTParser astParser = ASTParser.newParser(AST.JLS8);  
							astParser.setKind(ASTParser.K_COMPILATION_UNIT);
							astParser.setResolveBindings(true);
							astParser.setBindingsRecovery(true);
							astParser.setSource(compilationUnit);  
							CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));

							HandleOneFile resultOfOneFile = new HandleOneFile();
							unit.accept(new ClassVisitor(unit, resultOfOneFile));
							unit.accept(new MethodDeclarationVisitor(unit, resultOfOneFile));
							unit.accept(new MethodInvocationVisitor(unit, resultOfOneFile));
							unit.accept(new AssignVistor(unit, resultOfOneFile));
							// comment
							for (Object object: unit.getCommentList()) {
								Comment comment = (Comment) object;
								String[] temp = compilationUnit.getSource().split("\n");
								comment.accept(new CommentVisitor(unit, temp, resultOfOneFile));
							}
							resultOfOneFile.parse();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		AllExpansions.postprocess();
		System.err.println("END");
	}
}
