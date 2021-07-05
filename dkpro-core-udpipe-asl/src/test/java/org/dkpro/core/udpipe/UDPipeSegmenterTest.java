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

import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.asCopyableString;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class UDPipeSegmenterTest
{
    
    public static void assertForm(String[] aExpected, Collection<Token> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = toForms(aActual);

        System.out.printf("%-20s - Expected: %s%n", "Forms", asCopyableString(expected, false));
        System.out.printf("%-20s - Actual  : %s%n", "Forms", asCopyableString(actual, false));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    private static List<String> toForms(Collection<Token> tokenCollection) {
        List<String> text = new ArrayList<>();
        for  (Token token : tokenCollection) {
          text.add(token.getText());
        }
        return text;
    }
    
  
    @Test
    public void testNorwegian()
        throws Exception
    {
        runTest("no", null, "Storbritannia drøyer ikke. Storbritannia starter den formelle prosessen for utmelding av EU 29. mars, opplyser statsminister Theresa Mays kontor.",
                new String[] { 
                        "Storbritannia drøyer ikke.",
                        "Storbritannia starter den formelle prosessen for utmelding av EU 29. "
                        + "mars, opplyser statsminister Theresa Mays kontor." },
                new String[] { "Storbritannia", "drøyer", "ikke", ".", "Storbritannia", "starter",
                        "den", "formelle", "prosessen", "for", "utmelding", "av", "EU", "29.",
                        "mars", ",", "opplyser", "statsminister", "Theresa", "Mays", "kontor",
                        "." });

    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null,
                "Good morning Mr. President. I would love to welcome you to S.H.I.E.L.D. 2.0.",
                new String[] { "Good morning Mr. President.",
                        "I would love to welcome you to S.H.I.E.L.D. 2.0." },
                new String[] { "Good", "morning", "Mr.", "President", ".", "I", "would", "love",
                        "to", "welcome", "you", "to", "S.H.I.E.L.D.", "2.0", "." });

    }
        
    @Test
    public void testPortuguese()
        throws Exception
    {
        runTest("pt", null,
                "O território dentro das fronteiras atuais da República Portuguesa tem sido continuamente povoado desde os tempos pré-históricos. Ocupado por celtas, como os galaicos e os lusitanos, foi integrado na República Romana.",
                new String[] { "O território dentro das fronteiras atuais da República Portuguesa tem sido continuamente povoado desde os tempos pré-históricos.",
                        "Ocupado por celtas, como os galaicos e os lusitanos, foi integrado na República Romana." },
                new String[] { "O", "território", "dentro", "d", "as", "fronteiras", "atuais", "da", "República", "Portuguesa", "tem", "sido", "continuamente", "povoado", "desde", "os", "tempos", "pré-históricos", ".",
                        "Ocupado", "por", "celtas", ",", "como", "os", "galaicos", "e", "os", "lusitanos", ",", "foi", "integrado", "na", "República", "Romana", "." });

    }
        
    @Test(expected = AnalysisEngineProcessException.class)
    public void testPortugueseException()
        throws Exception
    {
        runTest("pt", null,
                "Eu foi à Lisboa",
                new String[] { "Eu foi à Lisboa" },
                new String[] { "Eu", "foi", "a", "a", "Lisboa" });

    }
        
    @Test
    public void testItalian()
        throws Exception
    {
        JCas jcas = runTest("it", null,
                "La prego di mantenere la calma e di rispondere alle mie domande e di non riagganciare la telefonata. "
                + "A Vicenza in Via Rossi all'altezza del sottopasso ferroviario. "
                + "La ringrazio della segnalazione e la ricontatterò se avessi bisogno di altre informazioni.",
                new String[] { "La prego di mantenere la calma e di rispondere alle mie domande e di non riagganciare la telefonata.",
                                "A Vicenza in Via Rossi all'altezza del sottopasso ferroviario.",
                                "La ringrazio della segnalazione e la ricontatterò se avessi bisogno di altre informazioni." },
                new String[] { "La", "prego", "di", "mantenere", "la", "calma", "e", "di", "rispondere", "a", "lle", "mie", "domande", "e", "di", "non", "riagganciare", "la", "telefonata", ".",
                                "A", "Vicenza", "in", "Via", "Rossi", "a", "ll'", "altezza", "d", "el", "sottopasso", "ferroviario", ".",
                                "La", "ringrazio", "d", "ella", "segnalazione", "e", "la", "ricontatterò", "se", "avessi", "bisogno", "di", "altre", "informazioni", "." });

        String[] expectedForms = new String[] { "La", "prego", "di", "mantenere", "la", "calma", "e", "di", "rispondere", "a", "le", "mie", "domande", "e", "di", "non", "riagganciare", "la", "telefonata", ".",
                                "A", "Vicenza", "in", "Via", "Rossi", "a", "l'", "altezza", "di", "il", "sottopasso", "ferroviario", ".",
                                "La", "ringrazio", "di", "la", "segnalazione", "e", "la", "ricontatterò", "se", "avessi", "bisogno", "di", "altre", "informazioni", "." };
        
        assertForm(expectedForms, select(jcas, Token.class));
    }


    private JCas runTest(String language, String aVariant, String testDocument, String[] sExpected,
            String[] tExpected)
        throws Exception
    {
        String variant = aVariant != null ? aVariant : "ud";
        AnalysisEngine engine = createEngine(UDPipeSegmenter.class,
                UDPipeSegmenter.PARAM_VARIANT, variant);
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(testDocument);

        engine.processAndOutputNewCASes(jcas);

        AssertAnnotations.assertSentence(sExpected, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tExpected, select(jcas, Token.class));
        
        return jcas;
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
