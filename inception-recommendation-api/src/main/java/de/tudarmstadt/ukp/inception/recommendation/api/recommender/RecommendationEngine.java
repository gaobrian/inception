/*
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.inception.recommendation.api.recommender;

import static de.tudarmstadt.ukp.inception.recommendation.api.RecommendationService.FEATURE_NAME_IS_PREDICTION;
import static org.apache.uima.fit.util.CasUtil.getType;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;

import de.tudarmstadt.ukp.inception.recommendation.api.evaluation.DataSplitter;
import de.tudarmstadt.ukp.inception.recommendation.api.evaluation.EvaluationResult;
import de.tudarmstadt.ukp.inception.recommendation.api.model.Recommender;

public abstract class RecommendationEngine {

    protected final Recommender recommender;
    protected final String layerName;
    protected final String featureName;
    protected final int maxRecommendations;

    public RecommendationEngine(Recommender aRecommender)
    {
        recommender = aRecommender;

        layerName = aRecommender.getLayer().getName();
        featureName = aRecommender.getFeature().getName();
        maxRecommendations = aRecommender.getMaxRecommendations();
    }

// tag::methodDefinition[]
    /**
     * Given training data in {@code aCasses}, train a model. In order to save data between
     * runs, the {@code aContext} can be used.
     * This method must not mutate {@code aCasses} in any way.
     * @param aContext The context of the recommender
     * @param aCasses The training data
     */
    public abstract void train(RecommenderContext aContext, List<CAS> aCasses)
            throws RecommendationException;

    /**
     * Given text in {@code aCas}, predict target annotations. These should be written into
     * {@code aCas}. In order to restore data from e.g. previous training, the {@code aContext}
     * can be used.
     * @param aContext The context of the recommender
     * @param aCas The training data
     */
    public abstract void predict(RecommenderContext aContext, CAS aCas)
            throws RecommendationException;

    /**
     * Evaluates the performance of a recommender by splitting the data given in {@code aCasses} in
     * training and test sets by using {@code aDataSplitter}, training on the training set and
     * measuring performance on unseen data on the training set. This method must not mutate
     * {@code aCasses} in any way.
     * 
     * @param aCasses
     *            The CASses containing target annotations
     * @param aDataSplitter
     *            The splitter which determines which annotations belong to which set
     * @return Scores available through an EvaluationResult object measuring the performance
     *         of predicting on the test set
     */
    public abstract EvaluationResult evaluate(List<CAS> aCasses, DataSplitter aDataSplitter)
        throws RecommendationException;
// end::methodDefinition[]

    /**
     * Whether or not this engine supports training. If training is not supported, the call to
     * {@link #train} should be skipped and {@link #predict} should be called immediately. Note
     * that the engine cannot expect a model to be present in the {@link RecommenderContext} if
     * training is skipped - this is meant only for engines that use pre-trained models.
     */
    public boolean requiresTraining()
    {
        return true;
    }

    protected Type getPredictedType(CAS aCas)
    {
        return getType(aCas, layerName);
    }

    protected Feature getPredictedFeature(CAS aCas)
    {
        return getPredictedType(aCas).getFeatureByBaseName(featureName);
    }

    protected Feature getScoreFeature(CAS aCas)
    {
        String scoreFeatureName = featureName + "_score";
        return getPredictedType(aCas).getFeatureByBaseName(scoreFeatureName);
    }

    protected Feature getIsPredictionFeature(CAS aCas)
    {
        return getPredictedType(aCas).getFeatureByBaseName(FEATURE_NAME_IS_PREDICTION);
    }
}
