/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.core.api.segmentation.SegmenterBase;
import org.dkpro.core.udpipe.internal.UDPipeUtils;

import cz.cuni.mff.ufal.udpipe.InputFormat;
import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.MultiwordToken;
import cz.cuni.mff.ufal.udpipe.MultiwordTokens;
import cz.cuni.mff.ufal.udpipe.ProcessingError;
import cz.cuni.mff.ufal.udpipe.Token;
import cz.cuni.mff.ufal.udpipe.Words;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Tokenizer and sentence splitter using UDPipe.
 */
@ResourceMetaData(name = "UDPipe Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class UDPipeSegmenter
    extends SegmenterBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;
    
    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     * 
     * <p>The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set
     * the variant parameter to match the artifact. If the artifact contains the model in
     * a non-default location, you  also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.</p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;
    
    private ModelProviderBase<Model> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Model>()
        {
            {
                setContextObject(UDPipeSegmenter.this);
                setDefault(LOCATION, "classpath:/org/dkpro/core/udpipe/lib/" +
                        "segmenter-${language}-${variant}.properties");
                setDefault(VARIANT, "ud");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected Model produceResource(URL aUrl)
                throws IOException
            {
                UDPipeUtils.init();
                
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);
                return Model.load(modelFile.getAbsolutePath());
            }
        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        super.process(aJCas);
    }

    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        
        InputFormat inputFormat = modelProvider.getResource()
                .newTokenizer(Model.getDEFAULT() + ";ranges");
        inputFormat.setText(aJCas.getDocumentText());

        cz.cuni.mff.ufal.udpipe.Sentence sentence = new cz.cuni.mff.ufal.udpipe.Sentence();
        ProcessingError error = new ProcessingError();

        int sentenceIdx = 0;
        while (inputFormat.nextSentence(sentence, error)) {
            if (error.occurred()) {
                throw new AnalysisEngineProcessException(
                        new IllegalStateException(error.getMessage()));
            }
            
            Integer sentenceStart = null;
            int sentenceEnd = -1;

            int mIdx = 0;
            MultiwordToken multiword = null;
            
            Words words = sentence.getWords();
            MultiwordTokens multiwords = sentence.getMultiwordTokens();
            for (int idx = 1; idx < words.size(); idx++) {
                
                // Retrieve a multiword from the list, if any
                if (multiword == null && mIdx < multiwords.size()) {
                    multiword = multiwords.get(mIdx);
                    mIdx++;
                }
                
                Token token = words.get(idx);

                int wordStart;
                int wordEnd;                        
                if (multiword != null && idx >= multiword.getIdFirst()
                    && idx <= multiword.getIdLast()) {
                    
                    int componentIdx = idx - multiword.getIdFirst();

                    wordStart = (int) multiword.getTokenRangeStart() + componentIdx;
                    wordEnd = wordStart + 1;
                    
                    if (wordEnd > (int) multiword.getTokenRangeEnd()) {
                        int start = (int) multiword.getTokenRangeStart();
                        int end = (int) multiword.getTokenRangeEnd();
                        throw new AnalysisEngineProcessException(new Exception("A splitted contraction could not be fitted into the word span! (" + (start + aZoneBegin)  + ", " + (end + aZoneBegin) + ")"));
                    }
                    
                    if (idx == multiword.getIdLast()) {
                        wordEnd =  (int) multiword.getTokenRangeEnd();
                        multiword = null;
                    }

                } else {
                    wordStart = (int) token.getTokenRangeStart();
                    wordEnd = (int) token.getTokenRangeEnd();
                }

                if (sentenceStart == null) {
                    sentenceStart = wordStart;
                }
                sentenceEnd = wordEnd;
                if (wordStart < 0 || wordEnd < 0 || wordStart > wordEnd) {
                    throw new AnalysisEngineProcessException(
                            new IllegalStateException("Invalid word range! (sentence idx: " + sentenceIdx + ", word index: " + idx + ", start: " + wordStart + ", end: " + wordEnd + ")"));
                } else {
                    de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token dkproToken;
                    dkproToken = createToken(aJCas, wordStart + aZoneBegin, wordEnd + aZoneBegin);
                    if (token.getForm() != null) {                        
                        dkproToken.setText(token.getForm());
                    }
                }
            }

            if (sentenceStart == null || sentenceEnd < 0 || sentenceStart > sentenceEnd) {
                throw new AnalysisEngineProcessException(
                        new IllegalStateException("Invalid sentence range! (sentence idx: " + sentenceIdx + ", start: " + sentenceStart + ", end: " + sentenceEnd + ")"));
            } else {
                createSentence(aJCas, sentenceStart + aZoneBegin, sentenceEnd + aZoneBegin);
            }
            sentence = new cz.cuni.mff.ufal.udpipe.Sentence();
            error = new ProcessingError();
            sentenceIdx++;
        }
    }
}
