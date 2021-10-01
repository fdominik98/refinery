package org.eclipse.viatra.solver.language.mapping.tests

import com.google.inject.Inject
import org.eclipse.viatra.solver.language.mapping.PartialModelMapper
import org.eclipse.viatra.solver.language.model.problem.Problem
import org.eclipse.viatra.solver.language.tests.ProblemInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@ExtendWith(InjectionExtension)
@InjectWith(ProblemInjectorProvider)
class PartialModelMapperTest {
	@Inject
	ParseHelper<Problem> parseHelper

	PartialModelMapper mapper

	@BeforeEach
	def void beforeEach() {
		mapper = new PartialModelMapper
	}

	@Test
	@Disabled("Method not yet implemented")
	def void exampleTest() {
		val problem = parseHelper.parse('''
			class Person {
				Person[0..*] friend
			}
			
			friend(a, b).
		''')
		val model = mapper.transformProblem(problem)
		assertThat(model, notNullValue())
	}
}
