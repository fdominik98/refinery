/*
 * SPDX-FileCopyrightText: 2021-2024 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.language.ide.contentassist;

import com.google.inject.Inject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator;
import org.eclipse.xtext.ide.editor.contentassist.IdeCrossrefProposalProvider;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xtext.CurrentTypeFinder;
import org.jetbrains.annotations.Nullable;
import tools.refinery.language.documentation.DocumentationCommentParser;
import tools.refinery.language.model.problem.*;
import tools.refinery.language.naming.NamingUtil;
import tools.refinery.language.naming.ProblemQualifiedNameConverter;
import tools.refinery.language.resource.ProblemResourceDescriptionStrategy;
import tools.refinery.language.scoping.imports.ImportAdapterProvider;
import tools.refinery.language.scoping.imports.ImportCollector;
import tools.refinery.language.services.ProblemGrammarAccess;
import tools.refinery.language.utils.BuiltinSymbols;
import tools.refinery.language.utils.ProblemUtil;
import tools.refinery.language.validation.ReferenceCounter;

import java.util.*;

public class ProblemCrossrefProposalProvider extends IdeCrossrefProposalProvider {
	@Inject
	private CurrentTypeFinder currentTypeFinder;

	@Inject
	private ReferenceCounter referenceCounter;

	@Inject
	private ImportCollector importCollector;

	@Inject
	private ImportAdapterProvider importAdapterProvider;

	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;

	@Inject
	private IdeContentProposalCreator proposalCreator;

	private CrossReference importedModuleCrossReference;

	@Inject
	public void setGrammarAccess(ProblemGrammarAccess grammarAccess) {
		importedModuleCrossReference = grammarAccess.getImportStatementAccess()
				.getImportedModuleProblemCrossReference_1_0();
	}

	@Override
	protected Iterable<IEObjectDescription> queryScope(IScope scope, CrossReference crossReference,
													   ContentAssistContext context) {
		var eObjectDescriptionsByName = new HashMap<ProblemResourceDescriptionStrategy.ShadowingKey,
				List<IEObjectDescription>>();
		for (var candidate : super.queryScope(scope, crossReference, context)) {
			if (isExistingObject(candidate, crossReference, context)) {
				var shadowingKey = ProblemResourceDescriptionStrategy.getShadowingKey(candidate);
				var candidateList = eObjectDescriptionsByName.computeIfAbsent(shadowingKey,
						ignored -> new ArrayList<>());
				candidateList.add(candidate);
			}
		}
		var eObjectDescriptions = new ArrayList<IEObjectDescription>();
		for (var candidates : eObjectDescriptionsByName.values()) {
			if (candidates.size() == 1) {
				var candidate = candidates.getFirst();
				if (shouldBeVisible(candidate, crossReference, context)) {
					eObjectDescriptions.add(candidate);
				}
			}
		}
		if (Objects.equals(importedModuleCrossReference, crossReference)) {
			postProcessImportProposals(eObjectDescriptions, context);
		}
		return eObjectDescriptions;
	}

	@Override
	protected ContentAssistEntry createProposal(IEObjectDescription candidate, CrossReference crossRef,
												ContentAssistContext context) {
		return proposalCreator.createProposal(qualifiedNameConverter.toString(candidate.getName()), context, e -> {
			e.setSource(candidate);
			e.setDescription(getDescription(candidate));
			var documentation = candidate.getUserData(DocumentationCommentParser.DOCUMENTATION);
			if (documentation != null) {
				e.setDocumentation(documentation);
			}
			e.setKind(ContentAssistEntry.KIND_REFERENCE);
		});
	}

	private static String getDescription(IEObjectDescription candidate) {
		int arity = -1;
		var arityString = candidate.getUserData(ProblemResourceDescriptionStrategy.ARITY);
		try {
			arity = Integer.parseInt(arityString, 10);
		} catch (NumberFormatException e) {
			// Ignore parse error, omit arity.
		}
		var eClassDescription = getEClassDescription(candidate);
		if (arity < 0) {
			return eClassDescription;
		}
		if (eClassDescription == null) {
			return "/" + arity;
		}
		return "/" + arity + " " + eClassDescription;
	}

	private static String getEClassDescription(IEObjectDescription candidate) {
		var eClass = candidate.getEClass();
		if (eClass == null) {
			return null;
		}
		if (ProblemPackage.Literals.PROBLEM.isSuperTypeOf(eClass)) {
			return "module";
		}
		if (ProblemPackage.Literals.NODE.isSuperTypeOf(eClass)) {
			return "node";
		}
		if (ProblemPackage.Literals.PARAMETER.isSuperTypeOf(eClass)) {
			// Parameter must come before Variable, because it is a subclass of Variable.
			return "parameter";
		}
		if (ProblemPackage.Literals.VARIABLE.isSuperTypeOf(eClass)) {
			return "variable";
		}
		if (ProblemPackage.Literals.PREDICATE_DEFINITION.isSuperTypeOf(eClass)) {
			return getPredicateEClassDescription(candidate);
		}
		if (ProblemPackage.Literals.CLASS_DECLARATION.isSuperTypeOf(eClass)) {
			return "class";
		}
		if (ProblemPackage.Literals.REFERENCE_DECLARATION.isSuperTypeOf(eClass)) {
			// For predicates, there is no need to show the exact type of definition, since they behave
			// logically equivalently.
			return null;
		}
		if (ProblemPackage.Literals.ENUM_DECLARATION.isSuperTypeOf(eClass)) {
			return "enum";
		}
		if (ProblemPackage.Literals.RULE_DEFINITION.isSuperTypeOf(eClass)) {
			return "rule";
		}
		if (ProblemPackage.Literals.AGGREGATOR_DECLARATION.isSuperTypeOf(eClass)) {
			return "aggregator";
		}
		if (ProblemPackage.Literals.DATATYPE_DECLARATION.isSuperTypeOf(eClass)) {
			return "datatype";
		}
		if (ProblemPackage.Literals.ANNOTATION_DECLARATION.isSuperTypeOf(eClass)) {
			return "annotation";
		}
		return eClass.getName();
	}

	private static String getPredicateEClassDescription(IEObjectDescription candidate) {
		if (ProblemResourceDescriptionStrategy.ERROR_PREDICATE_TRUE.equals(
				candidate.getUserData(ProblemResourceDescriptionStrategy.ERROR_PREDICATE))) {
			return "error";
		}
		if (ProblemResourceDescriptionStrategy.SHADOW_PREDICATE_TRUE.equals(
				candidate.getUserData(ProblemResourceDescriptionStrategy.SHADOW_PREDICATE))) {
			return "shadow";
		}
		// For predicates, there is no need to show the exact type of definition, since they behave
		// logically equivalently.
		return null;
	}

	protected boolean isExistingObject(IEObjectDescription candidate, CrossReference crossRef,
									   ContentAssistContext context) {
		var rootModel = context.getRootModel();
		var eObjectOrProxy = candidate.getEObjectOrProxy();
		if (!Objects.equals(rootModel.eResource(), eObjectOrProxy.eResource())) {
			return true;
		}
		var currentValue = getCurrentValue(crossRef, context);
		if (currentValue == null) {
			return true;
		}
		var eObject = EcoreUtil.resolve(eObjectOrProxy, rootModel);
		if (!Objects.equals(currentValue, eObject)) {
			return true;
		}
		if (!ProblemUtil.isImplicit(eObject)) {
			return true;
		}
		if (rootModel instanceof Problem problem) {
			return referenceCounter.countReferences(problem, eObject) >= 2;
		}
		return true;
	}

	protected boolean shouldBeVisible(IEObjectDescription candidate, CrossReference crossReference,
									  ContentAssistContext context) {
		if (NamingUtil.isFullyQualified(candidate.getName()) &&
				!context.getPrefix().startsWith(ProblemQualifiedNameConverter.DELIMITER)) {
			// Do not propose names with a root prefix unless explicitly asked for.
			return false;
		}

		var errorPredicate = candidate.getUserData(ProblemResourceDescriptionStrategy.ERROR_PREDICATE);
		if (ProblemResourceDescriptionStrategy.ERROR_PREDICATE_TRUE.equals(errorPredicate)) {
			return false;
		}

		var eReference = getEReference(crossReference);
		if (eReference == null) {
			return true;
		}

		var shadowPredicate = candidate.getUserData(ProblemResourceDescriptionStrategy.SHADOW_PREDICATE);
		if (ProblemResourceDescriptionStrategy.SHADOW_PREDICATE_TRUE.equals(shadowPredicate) &&
				!(ProblemPackage.Literals.ATOM__RELATION.equals(eReference) &&
						ProblemUtil.mayReferToShadow(context.getCurrentModel()))) {
			return false;
		}

		var candidateEObjectOrProxy = candidate.getEObjectOrProxy();

		if (eReference.equals(ProblemPackage.Literals.REFERENCE_DECLARATION__OPPOSITE) &&
				candidateEObjectOrProxy instanceof ReferenceDeclaration candidateReferenceDeclaration) {
			return oppositeShouldBeVisible(candidateReferenceDeclaration, context);
		}

		if (eReference.equals(ProblemPackage.Literals.VARIABLE_OR_NODE_EXPR__ELEMENT)) {
			var assignedVariable = getAssignedVariable(context.getCurrentModel());
			if (assignedVariable != null && Objects.equals(assignedVariable, candidate.getEObjectOrProxy())) {
				return false;
			}
		}

		var builtinSymbols = importAdapterProvider.getBuiltinSymbols(context.getResource());

		return builtinSymbolAwareShouldBeVisible(candidate, context, eReference, builtinSymbols,
				candidateEObjectOrProxy);
	}

	private NamedElement getAssignedVariable(EObject context) {
		var assignmentExpr = EcoreUtil2.getContainerOfType(context, AssignmentExpr.class);
		if (assignmentExpr != null && assignmentExpr.getLeft() instanceof VariableOrNodeExpr variableOrNodeExpr) {
			return variableOrNodeExpr.getVariableOrNode();
		}
		return null;
	}

	private boolean importedModuleShouldBeVisible(IEObjectDescription candidate, ContentAssistContext context) {
		var moduleKind = candidate.getUserData(ProblemResourceDescriptionStrategy.MODULE_KIND);
		if (!ModuleKind.MODULE.getName().equals(moduleKind)) {
			return false;
		}
		var resource = context.getResource();
		var candidateResourceUri = candidate.getEObjectURI().trimFragment();
		if (candidateResourceUri.equals(resource.getURI())) {
			return false;
		}
		var imports = importCollector.getDirectImports(resource);
		return !imports.toUriSet().contains(candidateResourceUri);
	}

	private static boolean oppositeShouldBeVisible(ReferenceDeclaration candidateReferenceDeclaration,
												   ContentAssistContext context) {
		var referenceDeclaration = EcoreUtil2.getContainerOfType(context.getCurrentModel(),
				ReferenceDeclaration.class);
		if (referenceDeclaration == null) {
			return true;
		}
		var classDeclaration = EcoreUtil2.getContainerOfType(referenceDeclaration, ClassDeclaration.class);
		if (classDeclaration == null) {
			return true;
		}
		var oppositeType = candidateReferenceDeclaration.getReferenceType();
		if (oppositeType == null) {
			return true;
		}
		var resolvedOppositeType = EcoreUtil.resolve(oppositeType, candidateReferenceDeclaration);
		return classDeclaration.equals(resolvedOppositeType);
	}

	private boolean builtinSymbolAwareShouldBeVisible(
			IEObjectDescription candidate, ContentAssistContext context, EReference eReference,
			BuiltinSymbols builtinSymbols, EObject candidateEObjectOrProxy) {
		if (eReference.equals(ProblemPackage.Literals.REFERENCE_DECLARATION__REFERENCE_TYPE) &&
				context.getCurrentModel() instanceof ReferenceDeclaration referenceDeclaration &&
				(referenceDeclaration.getKind() == ReferenceKind.CONTAINMENT ||
						referenceDeclaration.getKind() == ReferenceKind.CONTAINER)) {
			// Containment or container references must have a class type.
			// We don't support {@code node} as a container or contained type.
			return ProblemPackage.Literals.CLASS_DECLARATION.isSuperTypeOf(candidate.getEClass()) &&
					!builtinSymbols.node().equals(candidateEObjectOrProxy);
		}

		if (eReference.equals(ProblemPackage.Literals.REFERENCE_DECLARATION__REFERENCE_TYPE) ||
				eReference.equals(ProblemPackage.Literals.PARAMETER__PARAMETER_TYPE) ||
				eReference.equals(ProblemPackage.Literals.TYPE_SCOPE__TARGET_TYPE)) {
			if (builtinSymbols.exists().equals(candidateEObjectOrProxy)) {
				return false;
			}
			return ProblemResourceDescriptionStrategy.TYPE_LIKE_TRUE.equals(
					candidate.getUserData(ProblemResourceDescriptionStrategy.TYPE_LIKE));
		}

		if (eReference.equals(ProblemPackage.Literals.REFERENCE_DECLARATION__SUPER_SETS) &&
				context.getCurrentModel() instanceof ReferenceDeclaration referenceDeclaration) {
			return supersetShouldBeVisible(candidate, 2, referenceDeclaration.getSuperSets(),
					referenceDeclaration, builtinSymbols, candidateEObjectOrProxy);
		}

		if (eReference.equals(ProblemPackage.Literals.PREDICATE_DEFINITION__SUPER_SETS) &&
				context.getCurrentModel() instanceof PredicateDefinition predicateDefinition) {
			return supersetShouldBeVisible(candidate, predicateDefinition.getParameters().size(),
					predicateDefinition.getSuperSets(), predicateDefinition, builtinSymbols, candidateEObjectOrProxy);
		}

		if (eReference.equals(ProblemPackage.Literals.CLASS_DECLARATION__SUPER_TYPES)) {
			return supertypeShouldBeVisible(candidate, context, builtinSymbols, candidateEObjectOrProxy);
		}

		return true;
	}

	private boolean supersetShouldBeVisible(
			IEObjectDescription candidate, int expectedArity, List<Relation> existingSupersets, EObject currentModel,
			BuiltinSymbols builtinSymbols, EObject candidateEObjectOrProxy) {
		if (ProblemPackage.Literals.DATATYPE_DECLARATION.isSuperTypeOf(candidate.getEClass()) ||
				builtinSymbols.node().equals(candidateEObjectOrProxy) ||
				builtinSymbols.exists().equals(candidateEObjectOrProxy) ||
				builtinSymbols.equals().equals(candidateEObjectOrProxy) ||
				currentModel.equals(candidateEObjectOrProxy)) {
			return false;
		}
		if (candidateEObjectOrProxy instanceof Relation candidateRelation &&
				existingSupersets.contains(candidateRelation)) {
			return false;
		}
		var arityString = candidate.getUserData(ProblemResourceDescriptionStrategy.ARITY);
		if (arityString == null) {
			return true;
		}
		int arity = Integer.parseInt(arityString, 10);
		return arity == expectedArity;
	}

	private boolean supertypeShouldBeVisible(IEObjectDescription candidate, ContentAssistContext context,
											 BuiltinSymbols builtinSymbols, EObject candidateEObjectOrProxy) {
		if (!ProblemPackage.Literals.CLASS_DECLARATION.isSuperTypeOf(candidate.getEClass()) ||
				builtinSymbols.node().equals(candidateEObjectOrProxy) ||
				builtinSymbols.container().equals(candidateEObjectOrProxy) ||
				builtinSymbols.contained().equals(candidateEObjectOrProxy)) {
			return false;
		}
		if (context.getCurrentModel() instanceof ClassDeclaration classDeclaration &&
				candidateEObjectOrProxy instanceof ClassDeclaration candidateClassDeclaration) {
			return !classDeclaration.equals(candidateClassDeclaration) &&
					!classDeclaration.getSuperTypes().contains(candidateClassDeclaration);
		}
		return true;
	}

	@Nullable
	private EReference getEReference(CrossReference crossReference) {
		var type = currentTypeFinder.findCurrentTypeAfter(crossReference);
		if (!(type instanceof EClass eClass)) {
			return null;
		}
		return GrammarUtil.getReference(crossReference, eClass);
	}

	protected EObject getCurrentValue(CrossReference crossRef, ContentAssistContext context) {
		var value = getCurrentValue(crossRef, context.getCurrentModel());
		if (value != null) {
			return value;
		}
		var currentNodeSemanticObject = NodeModelUtils.findActualSemanticObjectFor(context.getCurrentNode());
		return getCurrentValue(crossRef, currentNodeSemanticObject);
	}

	protected EObject getCurrentValue(CrossReference crossRef, EObject context) {
		if (context == null) {
			return null;
		}
		var eReference = GrammarUtil.getReference(crossRef, context.eClass());
		if (eReference == null || eReference.isMany()) {
			return null;
		}
		return (EObject) context.eGet(eReference);
	}

	protected void postProcessImportProposals(List<IEObjectDescription> descriptions, ContentAssistContext context) {
		var currentModel = context.getCurrentModel();
		if (currentModel == null) {
			return;
		}
		var suggestedLibraries = importAdapterProvider.getOrInstall(currentModel).getLibrary().getSuggestedLibraries();
		if (suggestedLibraries.isEmpty()) {
			return;
		}
		var suggestedSet = new LinkedHashSet<>(suggestedLibraries);
		for (var description : descriptions) {
			if (ProblemPackage.Literals.PROBLEM.isSuperTypeOf(description.getEClass())) {
				suggestedSet.remove(description.getQualifiedName());
			}
		}
		for (var suggestedName : suggestedSet) {
			var uri = URI.createURI("import://%s.%s".formatted(String.join("/", suggestedName.getSegments()),
					ProblemUtil.MODULE_EXTENSION));
			var proxy = ProblemFactory.eINSTANCE.createProblem();
			((InternalEObject) proxy).eSetProxyURI(uri);
			var description = new EObjectDescription(suggestedName, proxy, Map.of(
					ProblemResourceDescriptionStrategy.MODULE_KIND, ModuleKind.MODULE.getName()
			));
			descriptions.add(description);
		}
		// Delay removing modules that should not be visible until here, so that modules even modules that shouldn't
		// be visible can shadow suggested names.
		descriptions.removeIf(description -> !importedModuleShouldBeVisible(description, context));
	}
}
