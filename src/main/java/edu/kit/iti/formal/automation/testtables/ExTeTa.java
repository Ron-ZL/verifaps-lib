package edu.kit.iti.formal.automation.testtables;

/*-
 * #%L
 * geteta
 * %%
 * Copyright (C) 2016 Alexander Weigl
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.SymbExFacade;
import edu.kit.iti.formal.automation.st.ast.TopLevelElements;
import edu.kit.iti.formal.automation.testtables.builder.TableTransformation;
import edu.kit.iti.formal.automation.testtables.exception.GetetaException;
import edu.kit.iti.formal.automation.testtables.exception.IllegalExpressionException;
import edu.kit.iti.formal.automation.testtables.io.Report;
import edu.kit.iti.formal.automation.testtables.model.CounterExampleAnalyzer;
import edu.kit.iti.formal.automation.testtables.model.GeneralizedTestTable;
import edu.kit.iti.formal.automation.testtables.model.options.Mode;
import edu.kit.iti.formal.smv.ast.SMVModule;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ExTeTa {
    public static boolean DEBUG = true;

    public static void main(String[] args) {
        try {
            CommandLine cli = parse(args);
            run(cli);
        } catch (ParseException e) {
            Report.fatal(e.getMessage());
        } catch (JAXBException e) {
            Report.fatal(e.getLinkedException().getMessage());
        } catch (IOException e) {
            Report.fatal("IOExcepton: %s", e.getMessage());
        } catch (GetetaException e) {
            Report.fatal("%s: %s", e.getClass().getSimpleName(),
                    e.getMessage());

        }

    }

    public static void run(CommandLine cli)
            throws ParseException, JAXBException, IOException {
        // xml
        Report.XML_MODE = cli.hasOption("x");

        //
        Runtime.getRuntime().addShutdownHook(new Thread(Report::close));

        //
        String tableFilename = cli.getOptionValue("t");
        String codeFilename = cli.getOptionValue("c");


        if (tableFilename == null || codeFilename == null) {
            Report.fatal("No code or table file given.");
            System.exit(1);
        }

        //
        GeneralizedTestTable table = Facade.readTable(tableFilename);
        TopLevelElements code = Facade.readProgram(codeFilename);


        if (cli.getOptionValue('m') != null)
            switch (cli.getOptionValue('m')) {
                case "concrete-smv":
                    table.getOptions().setMode(Mode.CONCRETE_TABLE);
                    break;
                case "conformance":
                    table.getOptions().setMode(Mode.CONFORMANCE);
                    break;
                case "input-seq-exists":
                    table.getOptions().setMode(Mode.INPUT_SEQUENCE_EXISTS);
                    break;
            }


        //
        TableTransformation tt = new TableTransformation(table);
        SMVModule modTable = tt.transform();
        SMVModule modCode = SymbExFacade.evaluateProgram(code);
        SMVModule mainModule = Facade.glue(modTable, modCode, table.getOptions());

        List<SMVModule> modules = new LinkedList<>();
        modules.add(mainModule);
        modules.add(modTable);
        modules.add(modCode);
        modules.addAll(tt.getHelperModules());
        boolean b = Facade.runNuXMV(tableFilename, modules);

        if (!b) {
            CounterExampleAnalyzer cea = new CounterExampleAnalyzer(table,
                    Report.getMessage());
            cea.run();
        }
    }

    private static CommandLine parse(String[] args) throws ParseException {
        CommandLineParser clp = new DefaultParser();

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption("x", false, "enable XML mode");
        options.addOption("t", "table", true, "the xml file of the table");
        options.addOption("o", "output", true, "ouput");
        options.addOption("c", "code", true, "program files");
        options.addOption("m", "mode", true, "mode");

        return clp.parse(options, args, true);
    }

}
