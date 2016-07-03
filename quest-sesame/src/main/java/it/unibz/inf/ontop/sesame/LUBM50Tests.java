package it.unibz.inf.ontop.sesame;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerEntity;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.util.Properties;

/***
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 * 
 * @author mariano
 * 
 */
public class LUBM50Tests {

	String driver = "com.mysql.jdbc.Driver";
//	String url = "jdbc:mysql://obdalin3.inf.unibz.it/lubmex20100?sessionVariables=sql_mode='ANSI'&useCursorFetch=true";
//	String username = "fish";
//	String password = "fish";
	String url = "jdbc:mysql://obdalin3.inf.unibz.it/lubmex20200?sessionVariables=sql_mode='ANSI'&useCursorFetch=true";
	String username = "fish";
	String password = "fish";

	String owlfile = "../quest-owlapi3/src/test/resources/test/lubm-ex-20-uni1/LUBM-ex-20.owl";

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OBDADataSource source;
	private final NativeQueryLanguageComponentFactory nativeQLFactory;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public static void main(String args[]) {
		try {
			LUBM50Tests t = new LUBM50Tests();

//			 t.test1Setup();
//			t.test2RestoringAndLoading();
//			t.test4mergeFiles();
			 t.test3InitializingQuest();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public LUBM50Tests() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Injector injector = Guice.createInjector(new OBDACoreModule(new QuestPreferences()));
		nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);

	}

	public void test1Setup() throws Exception {

		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);

			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);

			simanager.setupRepository(true);

		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}
	}

	public void test2RestoringAndLoading() throws Exception {

		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);

			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);

			simanager.restoreRepository();

			int insert = 0;
			
			for (int i = 0; i < 1; i++) {
				log.info("Started University {}", i);
				final int index = i;

//				File folder = new File("/Users/mariano/Downloads/lubm200/");
//
//				String[] datafiles = folder.list(new FilenameFilter() {
//
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.startsWith("fullUniversity" + index );
//					}
//				});

				
//				for (int j = 0; j < datafiles.length; j++) {
//					
//					String ntripleFile = "/Users/mariano/Downloads/lubm200/" + datafiles[j];
//					System.out.println(ntripleFile);
					insert += simanager.insertDataNTriple("/Users/mariano/Downloads/lubm200/fullUniversity"+i+".ttl", "", 500000, 100000);
//				}
				log.info("Total Inserts: {}", insert);
				

			}
			simanager.updateMetadata();
			log.info("Metadata updated");

			log.info("Done");

			// assertEquals(30033, inserts);
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}

	}

	public void test3InitializingQuest() throws Exception {
		Properties p = new Properties();
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.STORAGE_LOCATION, QuestConstants.JDBC);
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(OBDAProperties.JDBC_DRIVER, driver);
		p.setProperty(OBDAProperties.JDBC_URL, url);
		p.setProperty(OBDAProperties.DB_USER, username);
		p.setProperty(OBDAProperties.DB_PASSWORD, password);
		p.setProperty(QuestPreferences.REWRITE, "true");

        QuestOWLFactory fac = new QuestOWLFactory();
		QuestOWLConfiguration config = QuestOWLConfiguration.builder()
				.properties(p)
				.build();
		QuestOWL quest = fac.createReasoner(ontology, config);

		QuestOWLConnection qconn = quest.getConnection();

		QuestOWLStatement st = qconn.createStatement();

		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("../quest-owlapi3/src/test/resources/test/treewitness/LUBM-ex-20-q3.q");

		BufferedWriter out1 = new BufferedWriter(new FileWriter("/Users/mariano/Desktop/logLUBM200-queries2.txt"));
		BufferedWriter out2 = new BufferedWriter(new FileWriter("/Users/mariano/Desktop/queriesLUBM200-queries2.txt"));
		
		for (QueryControllerEntity e : qc.getElements()) {
			if (!(e instanceof QueryControllerQuery)) {
				continue;
			}
			
			
			
			long totaltime = 0;
			String querystr = "";
			
			QueryControllerQuery query = (QueryControllerQuery) e;

				
			log.debug("Executing query: {}", query.getID());
			log.debug("Query: \n{}", query.getQuery());
			int count = 0;
			long time = 0;
			long fetchtime = 0;
//			for (int j = 0; j < 1; j++) {
				long start = System.nanoTime();
/*				QuestOWLResultSet res;
				try {
					res = (QuestOWLResultSet) st.executeTuple(query.getQuery());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}
				long end = System.nanoTime();
				long time = (end - start);
				

				
				
				
				start = System.nanoTime();
				while (res.nextRow()) {
					count += 1;
				}
				end = System.nanoTime();
				long fetchtime = end-start; */
				out2.write("Query: " + query.getID() + "\n");
				out2.write(st.getExecutableQuery(query.getQuery())+ "\n\n+++++++++++++++++++++++++++\n\n");
				
				/*log.debug("Total result: {}", count);
				log.debug("Elapsed time: {} ms", time);
				res.close();*/
				
//			}
			out1.write("QUERY: " + query.getID() + "\n");
			out1.write("Execution time: " + time + "\n");
			out1.write("Results: " + count + "\n");
			out1.write("Time to fetch: " + fetchtime + "\n");
			out1.write("\n\n+++++++++++++++++++++++++++\n\n");

			out2.flush();
			out1.flush();

			log.debug("Average time for query: {}  is:  {} nanos", query.getID(), totaltime/1);

		}
		out2.flush();
		out2.close();
		out1.flush();
		out1.close();
	}
		
		
		public void test4mergeFiles() throws Exception {

			try {
							
				for (int i = 0; i < 200; i++) {
					log.info("Started University {}", i);
					final int index = i;

					File folder = new File("/Users/mariano/Downloads/lubm200/");

					String[] datafiles = folder.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith("University" + index + "_");
						}
					});

					BufferedWriter out = new BufferedWriter(new FileWriter(new File("/Users/mariano/Downloads/lubm200/fullUniversity" + i + ".ttl")));
					
					for (int j = 0; j < datafiles.length; j++) {
						BufferedReader in = new BufferedReader(new FileReader("/Users/mariano/Downloads/lubm200/" + datafiles[j]));
						String line = in.readLine();
						while (line != null) {						
							out.write(line + "\n");
							line = in.readLine();
						}
						in.close();
					}
					out.flush();
					out.close();
					

				}
				
				log.info("Done");

				// assertEquals(30033, inserts);
			} catch (Exception e) {
				throw e;
			} 
	}

}
