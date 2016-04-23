/* scenarioo-server
 * Copyright (C) 2014, scenarioo.org Development Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.scenarioo.business.diffViewer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.scenarioo.api.ScenarioDocuReader;
import org.scenarioo.dao.diffViewer.DiffWriter;
import org.scenarioo.model.configuration.ComparisonAlias;
import org.scenarioo.model.configuration.Configuration;
import org.scenarioo.model.diffViewer.StructureDiffInfo;
import org.scenarioo.model.docu.entities.Scenario;
import org.scenarioo.repository.RepositoryLocator;

/**
 * Test cases for the scenario comparator with mocked docu data.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScenarioComparatorTest {

	private static final File ROOT_DIRECTORY = new File("tmpDiffViewerUnitTestFiles");
	private static final String BASE_BRANCH_NAME = "baseBranch";
	private static final String BASE_BUILD_NAME = "baseBuild";
	private static final String COMPARISON_BRANCH_NAME = "comparisonBranch";
	private static final String COMPARISON_BUILD_NAME = "comparisonBuild";
	private static final String COMPARISON_NAME = "comparisonName";
	private static final String USE_CASE_NAME = "useCase";
	private static final String SCENARIO_NAME_1 = "scenario_1";
	private static final String SCENARIO_NAME_2 = "scenario_2";
	private static final String SCENARIO_NAME_3 = "scenario_3";

	@Mock
	private ScenarioDocuReader scenarioDocuReader;

	@Mock
	private DiffWriter diffWriter;

	@Mock
	private StepComparator stepComparator;

	@InjectMocks
	private ScenarioComparator scenarioComparator = new ScenarioComparator(BASE_BRANCH_NAME, BASE_BUILD_NAME,
			COMPARISON_NAME);

	@BeforeClass
	public static void setUpClass() {
		ROOT_DIRECTORY.mkdirs();
		RepositoryLocator.INSTANCE.initializeConfigurationRepositoryForUnitTest(ROOT_DIRECTORY);
		RepositoryLocator.INSTANCE.getConfigurationRepository().getDiffViewerDirectory().mkdirs();
		RepositoryLocator.INSTANCE.getConfigurationRepository().updateConfiguration(getTestConfiguration());
	}

	@AfterClass
	public static void tearDownClass() {
		try {
			FileUtils.deleteDirectory(ROOT_DIRECTORY);
		} catch (IOException e) {
			throw new RuntimeException("Could not delete test data directory", e);
		}
	}

	@Test
	public void testCompareBuildsEqual() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		assertEquals(0, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(0, actualDiffInfo.getAdded());
		assertEquals(0, actualDiffInfo.getChanged());
		assertEquals(0, actualDiffInfo.getRemoved());
	}

	@Test
	public void testCompareOneScenarioAdded() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		double expectedChangeRate = 100.0 / 3.0;
		assertEquals(expectedChangeRate, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(1, actualDiffInfo.getAdded());
		assertEquals(0, actualDiffInfo.getChanged());
		assertEquals(0, actualDiffInfo.getRemoved());
	}

	@Test
	public void testCompareMultipleScenariosAdded() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_2);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		double expectedChangeRate = 200.0 / 3.0;
		assertEquals(expectedChangeRate, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(2, actualDiffInfo.getAdded());
		assertEquals(0, actualDiffInfo.getChanged());
		assertEquals(0, actualDiffInfo.getRemoved());
	}

	@Test
	public void testCompareScenarioChangedTo50Percentage() {
		double changeRatePerScenario = 50.0;
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(changeRatePerScenario, 1, 1, 1);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		double expectedChangeRate = changeRatePerScenario;
		assertEquals(expectedChangeRate, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(0, actualDiffInfo.getAdded());
		assertEquals(3, actualDiffInfo.getChanged());
		assertEquals(0, actualDiffInfo.getRemoved());
	}

	@Test
	public void testCompareOneScenarioRemoved() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		double expectedChangeRate = 100.0 / 2.0;
		assertEquals(expectedChangeRate, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(0, actualDiffInfo.getAdded());
		assertEquals(0, actualDiffInfo.getChanged());
		assertEquals(1, actualDiffInfo.getRemoved());
	}

	@Test
	public void testCompareMultipleScenariosRemoved() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_2);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		StructureDiffInfo actualDiffInfo = scenarioComparator.compare(USE_CASE_NAME);

		double expectedChangeRate = 200.0 / 3.0;
		assertEquals(expectedChangeRate, actualDiffInfo.getChangeRate(), 0.0);
		assertEquals(0, actualDiffInfo.getAdded());
		assertEquals(0, actualDiffInfo.getChanged());
		assertEquals(2, actualDiffInfo.getRemoved());
	}

	@Test(expected = RuntimeException.class)
	public void testCompareEmptyBaseScenarioName() {
		List<Scenario> baseScenarios = getScenarios(SCENARIO_NAME_1, null);
		List<Scenario> comparisonScenarios = getScenarios(SCENARIO_NAME_1, SCENARIO_NAME_2, SCENARIO_NAME_3);
		StructureDiffInfo useCaseDiffInfo = getScenarioDiffInfo(0, 0, 0, 0);

		initMocks(baseScenarios, comparisonScenarios, useCaseDiffInfo);

		scenarioComparator.compare(USE_CASE_NAME);
	}

	private void initMocks(List<Scenario> baseScenarios, List<Scenario> comparisonScenarios,
			StructureDiffInfo scenarioDiffInfo) {
		when(scenarioDocuReader.loadScenarios(BASE_BRANCH_NAME, BASE_BUILD_NAME, USE_CASE_NAME)).thenReturn(
				baseScenarios);
		when(scenarioDocuReader.loadScenarios(COMPARISON_BRANCH_NAME, COMPARISON_BUILD_NAME, USE_CASE_NAME))
				.thenReturn(comparisonScenarios);
		when(stepComparator.compare(anyString(), anyString())).thenReturn(scenarioDiffInfo);
	}

	public List<Scenario> getScenarios(String... names) {
		List<Scenario> scenarios = new LinkedList<Scenario>();
		for (String name : names) {
			Scenario scenario = new Scenario();
			scenario.setName(name);
			scenarios.add(scenario);
		}
		return scenarios;
	}

	private StructureDiffInfo getScenarioDiffInfo(double changeRate, int added, int changed, int removed) {
		StructureDiffInfo useCaseDiffInfo = new StructureDiffInfo();
		useCaseDiffInfo.setChangeRate(changeRate);
		useCaseDiffInfo.setAdded(added);
		useCaseDiffInfo.setChanged(changed);
		useCaseDiffInfo.setRemoved(removed);
		return useCaseDiffInfo;
	}

	private static Configuration getTestConfiguration() {

		ComparisonAlias comparisonAlias = new ComparisonAlias();
		comparisonAlias.setBaseBranchName(BASE_BRANCH_NAME);
		comparisonAlias.setComparisonBranchName(COMPARISON_BRANCH_NAME);
		comparisonAlias.setComparisonBuildName(COMPARISON_BUILD_NAME);
		comparisonAlias.setComparisonName(COMPARISON_NAME);

		List<ComparisonAlias> comparisonAliases = new LinkedList<ComparisonAlias>();
		comparisonAliases.add(comparisonAlias);

		Configuration configuration = RepositoryLocator.INSTANCE.getConfigurationRepository().getConfiguration();
		configuration.setComparisonAliases(comparisonAliases);

		return configuration;
	}
}
