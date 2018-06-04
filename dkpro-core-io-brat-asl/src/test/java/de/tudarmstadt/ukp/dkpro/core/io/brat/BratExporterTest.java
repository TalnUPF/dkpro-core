/*
 * Copyright 2017
 * Joan Codina TALN-UPF 
 * 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.brat;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.brat.BratExporter;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import edu.upf.taln.uima.conceptScorer.ChunkerIndex;


//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
public class BratExporterTest
{
    @Test
    public void testExportWeights()
        throws Exception
    {

      
        final String[] spanTypes = { 
           //   "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token:lemma|value",
          //  "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS:PosValue",
           //   "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity:value", 
              "edu.upf.taln.uima.chunker.types.Concept:chunkValue:FF0000:domScore"
           //  "edu.upf.taln.uima.chunker.types.Concept:chunkValue:00FF00:score"
       };
    
            Set<String> relationTypes= new HashSet<>();
         //    relationTypes.add("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent:DependencyType");
             
         final String[] typeMappings= {
                        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.(\\w+) -> $1",
                        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_(\\w+) -> $1",
                        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.(\\w+) -> $1",
                        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.(\\w+) -> $1",
                        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.(\\w+) -> $1."
                };
         Set<String> excludedTypes = new HashSet<>(); 
              excludedTypes.add("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PUNCT");
              excludedTypes.add("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.PUNCT");
             
            AnalysisEngineDescription pipelineOut = 
                    createEngineDescription(BratExporter.class,
                            BratExporter.PARAM_TARGET_LOCATION,"output", // null for extract to string
      //                      BratExporter.PARAM_TARGET_LOCATION,null, 
                            BratExporter.PARAM_FILENAME_EXTENSION,".html",
                            BratExporter.PARAM_OVERWRITE,true,
                            BratExporter.PARAM_ENABLE_TYPE_MAPPINGS,true,
                            BratExporter.PARAM_SHORT_ATTRIBUTE_NAMES,false,
                            BratExporter.PARAM_STRIP_EXTENSION,true,
                            BratExporter.PARAM_EXCLUDE_TYPES,excludedTypes,
                            BratExporter.PARAM_TYPE_MAPPINGS,typeMappings,
                            BratExporter.PARAM_TEXT_ANNOTATION_TYPES,spanTypes,
                            BratExporter.PARAM_RELATION_TYPES,relationTypes,
                            BratExporter.PARAM_WRITE_RELATION_ATTRIBUTES,false,
                            BratExporter.PARAM_HTML_TEMPLANTE,"/home/joan/workspace/pipelineCheck/input/template.html"
                                                
                             );
            // create the uima Pipeline
    
         CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                XmiReader.PARAM_LANGUAGE, "en",
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi",
                XmiReader.PARAM_PATTERNS, "*.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/test/resources/xmi/TypeSystem.xml"
                );
                  
 

 
                  String DOMAIN="jihad";
                 // public static String DOMAIN="quran";
                 String REF_DOMAIN="news"; 
                 String SOLR_URL="http://ipatdoc.taln.upf.edu:8080/multisensor/use_cases_en";        
                 AnalysisEngineDescription scorer= createEngineDescription(
                                 ChunkerIndex.class,
                                 ChunkerIndex.PARAM_REFSOLR,SOLR_URL,
                                 ChunkerIndex.PARAM_SOLR,SOLR_URL,
                                 ChunkerIndex.PARAM_DOMAINFIELD,"useCase",
                                 ChunkerIndex.PARAM_REFDOMAIN,REF_DOMAIN,
                                 ChunkerIndex.PARAM_DOMAIN,DOMAIN,
                                 ChunkerIndex.PARAM_TEXTFIELD,"text",
                                 ChunkerIndex.PARAM_INDEX,false,
                                 ChunkerIndex.PARAM_SCORING,true);
      
                 
         SimplePipeline.runPipeline( reader,scorer,pipelineOut);
         
         
         System.out.println("documents processed");
            AnalysisEngine pipelineBratOut = createEngine(
                     createEngineDescription(BratExporter.class,
                             BratExporter.PARAM_TARGET_LOCATION,null, // null for extract to string
                             BratExporter.PARAM_FILENAME_EXTENSION,".json",
                             BratExporter.PARAM_OVERWRITE,true,
                             BratExporter.PARAM_ENABLE_TYPE_MAPPINGS,true,
                             BratExporter.PARAM_SHORT_ATTRIBUTE_NAMES,false,
                             BratExporter.PARAM_STRIP_EXTENSION,true,
                             BratExporter.PARAM_TYPE_MAPPINGS,typeMappings,
                             BratExporter.PARAM_TEXT_ANNOTATION_TYPES,spanTypes,
                             BratExporter.PARAM_RELATION_TYPES,relationTypes,
                             BratExporter.PARAM_WRITE_RELATION_ATTRIBUTES,false
                             ) );
                    
        
      
                
    }

   
}
