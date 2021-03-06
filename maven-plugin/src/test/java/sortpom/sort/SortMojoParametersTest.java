package sortpom.sort;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import refutils.ReflectionHelper;
import sortpom.SortMojo;
import sortpom.SortPomImpl;
import sortpom.XmlOutputGenerator;
import sortpom.util.FileUtil;
import sortpom.util.WriterFactory;
import sortpom.wrapper.ElementWrapperCreator;
import sortpom.wrapper.TextWrapperCreator;
import sortpom.wrapper.WrapperFactoryImpl;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

class SortMojoParametersTest {
    private final File pomFile = mock(File.class);

    private SortPomImpl sortPomImpl;
    private FileUtil fileUtil;
    private SortMojo sortMojo;
    private ElementWrapperCreator elementWrapperCreator;
    private TextWrapperCreator textWrapperCreator;
    private XmlOutputGenerator xmlOutputGenerator;
    private WriterFactory writerFactory;

    @BeforeEach
    void setup() throws SecurityException, IllegalArgumentException {
        sortMojo = new SortMojo();
        new ReflectionHelper(sortMojo).setField("lineSeparator", "\n");
        new ReflectionHelper(sortMojo).setField("encoding", "UTF-8");

        sortPomImpl = new ReflectionHelper(sortMojo).getField(SortPomImpl.class);
        ReflectionHelper sortPomImplHelper = new ReflectionHelper(sortPomImpl);
        fileUtil = sortPomImplHelper.getField(FileUtil.class);
        xmlOutputGenerator = sortPomImplHelper.getField(XmlOutputGenerator.class);
        WrapperFactoryImpl wrapperFactoryImpl = sortPomImplHelper.getField(WrapperFactoryImpl.class);
        elementWrapperCreator = new ReflectionHelper(wrapperFactoryImpl).getField(ElementWrapperCreator.class);
        textWrapperCreator = new ReflectionHelper(wrapperFactoryImpl).getField(TextWrapperCreator.class);
        writerFactory = new ReflectionHelper(xmlOutputGenerator).getField(WriterFactory.class);
    }

    @Test
    void pomFileParameter() {
        testParameterMoveFromMojoToRestOfApplication("pomFile", pomFile, sortPomImpl, fileUtil);
    }

    @Test
    void createBackupFileParameter() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("createBackupFile", sortPomImpl);
    }

    @Test
    void keepTimestampParameter() {
    	testParameterMoveFromMojoToRestOfApplicationForBoolean("keepTimestamp", fileUtil);
    }

    @Test
    void backupFileExtensionParameter() {
        testParameterMoveFromMojoToRestOfApplication("backupFileExtension", ".gurka", sortPomImpl, fileUtil);
    }

    @Test
    void encodingParameter() {
        testParameterMoveFromMojoToRestOfApplication("encoding", "GURKA-2000", fileUtil, sortPomImpl, xmlOutputGenerator);
    }

    @Test
    void lineSeparatorParameter() {
        testParameterMoveFromMojoToRestOfApplication("lineSeparator", "\r");

        final Object lineSeparatorUtil = new ReflectionHelper(writerFactory)
                .getField("lineSeparatorUtil");

        assertThat(lineSeparatorUtil.toString(), is(equalTo("\r")));
    }

    @Test
    void parameterNrOfIndentSpaceShouldEndUpInXmlProcessor() {
        testParameterMoveFromMojoToRestOfApplication("nrOfIndentSpace", 6);

        final Object indentCharacters = new ReflectionHelper(xmlOutputGenerator)
                .getField("indentCharacters");

        assertThat(indentCharacters, is(equalTo("      ")));
    }

    @Test
    void expandEmptyElementsParameter() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("expandEmptyElements", xmlOutputGenerator);
    }

    @Test
    void spaceBeforeCloseEmptyElementParameter() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("spaceBeforeCloseEmptyElement", writerFactory);
    }

    @Test
    void predefinedSortOrderParameter() {
        testParameterMoveFromMojoToRestOfApplication("predefinedSortOrder", "tomatoSort", fileUtil);
    }

    @Test
    void parameterSortOrderFileShouldEndUpInFileUtil() {
        testParameterMoveFromMojoToRestOfApplication("sortOrderFile", "sortOrderFile.gurka");

        Object actual = new ReflectionHelper(fileUtil).getField("customSortOrderFile");

        assertThat(actual, is(equalTo("sortOrderFile.gurka")));
    }

    @Test
    void parameterSortDependenciesShouldEndUpInElementWrapperCreator() {
        testParameterMoveFromMojoToRestOfApplication("sortDependencies", "groupId,scope");

        Object sortDependencies = new ReflectionHelper(elementWrapperCreator).getField("sortDependencies");
        assertThat(sortDependencies.toString(), is("DependencySortOrder{childElementNames=[groupId, scope]}"));
    }

    @Test
    void parameterSortPluginsShouldEndUpInWrapperFactoryImpl() {
        testParameterMoveFromMojoToRestOfApplication("sortPlugins", "alfa,beta");

        Object sortDependencies = new ReflectionHelper(elementWrapperCreator).getField("sortPlugins");
        assertThat(sortDependencies.toString(), is("DependencySortOrder{childElementNames=[alfa, beta]}"));
    }

    @Test
    void parameterSortModulesShouldEndUpInWrapperFactoryImpl() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("sortModules", elementWrapperCreator);
    }

    @Test
    void parameterSortPropertiesShouldEndUpInWrapperFactoryImpl() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("sortProperties", elementWrapperCreator);
    }

    @Test
    void parameterKeepBlankLineShouldEndUpInXmlProcessor() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("keepBlankLines", textWrapperCreator);
    }

    @Test
    void parameterIndentBlankLineShouldEndUpInXmlProcessor() {
        testParameterMoveFromMojoToRestOfApplicationForBoolean("indentBlankLines", xmlOutputGenerator);
    }

    private void testParameterMoveFromMojoToRestOfApplication(String parameterName, Object parameterValue,
                                                              Object... whereParameterCanBeFound) {
        new ReflectionHelper(sortMojo).setField(parameterName, parameterValue);

        try {
            sortMojo.setup();
        } catch (MojoFailureException e) {
            throw new RuntimeException(e);
        }

        for (Object someInstanceThatContainparameter : whereParameterCanBeFound) {
            Object actual = new ReflectionHelper(someInstanceThatContainparameter).getField(parameterName);

            assertThat(actual, is(equalTo(parameterValue)));
        }
    }

    private void testParameterMoveFromMojoToRestOfApplicationForBoolean(String parameterName,
                                                                        Object... whereParameterCanBeFound) {
        new ReflectionHelper(sortMojo).setField(parameterName, true);

        try {
            sortMojo.setup();
        } catch (MojoFailureException e) {
            throw new RuntimeException(e);
        }

        for (Object someInstanceThatContainparameter : whereParameterCanBeFound) {
            Object actual = new ReflectionHelper(someInstanceThatContainparameter).getField(parameterName);

            assertThat(actual, is(equalTo(true)));
        }
    }

}
